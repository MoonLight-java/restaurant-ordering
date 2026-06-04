package com.restaurant.auth.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.auth.config.WechatConfig;
import com.restaurant.auth.dto.LoginResultDTO;
import com.restaurant.auth.entity.AuthUser;
import com.restaurant.auth.mapper.AuthUserMapper;
import com.restaurant.auth.service.AuthService;
import com.restaurant.common.enums.UserRoleEnum;
import com.restaurant.common.exception.BusinessException;
import com.restaurant.common.utils.JwtUtils;
import com.restaurant.common.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private AuthUserMapper authUserMapper;
    @Resource
    private WechatConfig wechatConfig;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private RestTemplate restTemplate;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String WECHAT_CODE2SESSION_URL =
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    @PostConstruct
    public void initDefaultAdmin() {
        AuthUser admin = authUserMapper.selectOne(
                new LambdaQueryWrapper<AuthUser>().eq(AuthUser::getUsername, "admin"));
        if (admin == null) {
            admin = new AuthUser();
            admin.setId(1L);
            admin.setUsername("admin");
            admin.setPasswordHash(BCrypt.hashpw("admin123"));
            admin.setNickname("超级管理员");
            admin.setRole(UserRoleEnum.SUPER_ADMIN.getCode());
            admin.setStatus(1);
            authUserMapper.insert(admin);
            log.info("Default admin user created: admin/admin123");
        } else if (admin.getPasswordHash() == null || admin.getPasswordHash().length() < 10) {
            admin.setPasswordHash(BCrypt.hashpw("admin123"));
            authUserMapper.updateById(admin);
        }
    }

    @Override
    public LoginResultDTO loginByWechat(String code, String encryptedData, String iv) {
        String openid = getOpenidFromWechat(code);
        String sessionKey = IdUtil.fastSimpleUUID();

        AuthUser user = authUserMapper.selectOne(
                new LambdaQueryWrapper<AuthUser>().eq(AuthUser::getOpenid, openid));

        if (user == null) {
            user = new AuthUser();
            user.setOpenid(openid);
            user.setSessionKey(sessionKey);
            user.setNickname("微信用户" + System.currentTimeMillis() % 100000);
            user.setAvatar("");
            user.setRole(UserRoleEnum.USER.getCode());
            user.setStatus(1);
            user.setLastLoginTime(LocalDateTime.now());
            authUserMapper.insert(user);
        } else {
            user.setSessionKey(sessionKey);
            user.setLastLoginTime(LocalDateTime.now());
            authUserMapper.updateById(user);
        }

        return buildLoginResult(user);
    }

    private String getOpenidFromWechat(String code) {
        String url = String.format(WECHAT_CODE2SESSION_URL,
                wechatConfig.getAppId(), wechatConfig.getAppSecret(), code);
        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode json = OBJECT_MAPPER.readTree(response);
            if (json.has("errcode") && json.get("errcode").asInt() != 0) {
                throw new BusinessException("微信登录失败：" + json.path("errmsg").asText("未知错误"));
            }
            return json.get("openid").asText();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用微信jscode2session失败", e);
            throw new BusinessException("微信登录失败，请稍后重试");
        }
    }

    @Override
    public LoginResultDTO loginByPassword(String username, String password) {
        AuthUser user = authUserMapper.selectOne(
                new LambdaQueryWrapper<AuthUser>().eq(AuthUser::getUsername, username));

        if (user == null || user.getStatus() == 0) {
            throw new BusinessException("用户不存在或已被禁用");
        }

        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new BusinessException("密码错误");
        }

        user.setLastLoginTime(LocalDateTime.now());
        authUserMapper.updateById(user);

        return buildLoginResult(user);
    }

    @Override
    public LoginResultDTO adminLogin(String username, String password) {
        AuthUser user = authUserMapper.selectOne(
                new LambdaQueryWrapper<AuthUser>().eq(AuthUser::getUsername, username));

        if (user == null || user.getStatus() == 0) {
            throw new BusinessException("用户不存在或已被禁用");
        }
        if (user.getRole() < UserRoleEnum.ADMIN.getCode()) {
            throw new BusinessException("无管理员权限");
        }
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new BusinessException("密码错误");
        }

        user.setLastLoginTime(LocalDateTime.now());
        authUserMapper.updateById(user);

        return buildLoginResult(user);
    }

    @Override
    public void logout(String token) {
        redisUtils.set("token:blacklist:" + token, 1, 24, TimeUnit.HOURS);
    }

    @Override
    public LoginResultDTO getCurrentUser(Long userId) {
        AuthUser user = authUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return buildLoginResult(user);
    }

    private LoginResultDTO buildLoginResult(AuthUser user) {
        String roleName = "USER";
        if (user.getRole() >= UserRoleEnum.SUPER_ADMIN.getCode()) {
            roleName = "SUPER_ADMIN";
        } else if (user.getRole() >= UserRoleEnum.ADMIN.getCode()) {
            roleName = "ADMIN";
        }

        String token = JwtUtils.createToken(user.getId(), roleName);

        return LoginResultDTO.builder()
                .token(token)
                .userId(user.getId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .role(roleName)
                .build();
    }
}
