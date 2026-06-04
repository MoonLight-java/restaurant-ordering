package com.restaurant.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wechat")
public class WechatConfig {
    private String appId = "wx1234567890abcdef";
    private String appSecret = "your_app_secret_here";
}
