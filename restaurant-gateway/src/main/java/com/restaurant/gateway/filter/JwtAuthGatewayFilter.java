package com.restaurant.gateway.filter;

import com.restaurant.common.utils.JwtUtils;
import com.restaurant.common.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JwtAuthGatewayFilter implements GlobalFilter {

    private static final List<String> WHITELIST = Arrays.asList(
            "/api/auth/login/wechat",
            "/api/auth/login/password",
            "/api/admin/auth/login",
            "/api/menu/categories",
            "/api/menu/dishes"
    );

    @Resource
    private RedisUtils redisUtils;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isWhitelist(path)) {
            return chain.filter(exchange);
        }

        // Also allow OPTIONS for CORS
        if ("OPTIONS".equalsIgnoreCase(request.getMethodValue())) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "未提供认证令牌");
        }

        String token = authHeader.substring(7);

        // Check blacklist
        if (redisUtils.hasKey("token:blacklist:" + token)) {
            return unauthorized(exchange, "令牌已失效");
        }

        if (!JwtUtils.validateToken(token)) {
            return unauthorized(exchange, "令牌无效或已过期");
        }

        Long userId = JwtUtils.getUserId(token);
        String role = JwtUtils.getUserRole(token);

        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Id", String.valueOf(userId))
                .header("X-User-Role", role)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private boolean isWhitelist(String path) {
        for (String w : WHITELIST) {
            if (path.startsWith(w)) return true;
        }
        // Allow static resources and dish detail with ID
        if (path.matches("^/api/menu/dishes/\\d+$")) return true;
        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        String body = "{\"code\":401,\"message\":\"" + message + "\"}";
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }
}
