package org.example.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.commoncore.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

/*
 * @Author:总会落叶
 * @Date:2026/3/25
 * @Description: 网关认证过滤器 - 使用common模块的JwtUtil
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;  // ✅ 注入common模块的JwtUtil
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    /**
     * 支持两种配置写法：
     * 1) 逗号分隔字符串：/api/auth/login,/api/auth/refresh,/actuator/health
     * 2) YAML 列表：
     *    - /api/auth/login
     *    - /api/auth/refresh
     *    - /actuator/health
     */
    @Value("${auth.white-list:/api/auth/login,/api/auth/refresh,/api/actuator/health}")
    private String whiteListRaw;

    private List<String> parseWhiteList() {
        // 兼容 Spring 将 YAML list 转成形如 [a, b, c] 的情况
        String raw = whiteListRaw;
        if (raw == null) {
            return Collections.emptyList();
        }
        raw = raw.replace("[", "").replace("]", "");
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private static final String ACCESS_TOKEN_PREFIX = "access_token:";
    private static final String TOKEN_BLACKLIST_PREFIX = "blacklist:";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. 白名单放行
        if (isWhiteListed(path)) {
            log.debug("白名单路径: {}", path);
            return chain.filter(exchange);
        }

        // 2. 获取 Token
        String token = extractToken(request);
        if (token == null) {
            log.warn("未提供Token: {}", path);
            return unauthorized(exchange, "未提供认证Token");
        }

        // 3. 检查 Token 格式
        if (token.chars().filter(ch -> ch == '.').count() != 2) {
            log.warn("Token格式错误: {}", token);
            return unauthorized(exchange, "Token格式错误");
        }

        // 4. 验证 Token ✅ 使用common模块的JwtUtil
        if (!jwtUtil.validateToken(token)) {
            log.warn("Token无效: {}", token);
            return unauthorized(exchange, "Token无效或已过期");
        }

        // 4. 检查黑名单
        String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
        return redisTemplate.hasKey(blacklistKey)
                .flatMap(isBlacklisted -> {
                    if (Boolean.TRUE.equals(isBlacklisted)) {
                        log.warn("Token已被注销: {}", token);
                        return unauthorized(exchange, "Token已被注销");
                    }

                    // 5. 检查 Redis 缓存
                    String accessKey = ACCESS_TOKEN_PREFIX + token;
                    return redisTemplate.hasKey(accessKey)
                            .flatMap(exists -> {
                                if (Boolean.FALSE.equals(exists)) {
                                    log.warn("Token已失效: {}", token);
                                    return unauthorized(exchange, "Token已失效");
                                }

                                // 6. 从 Token 中提取用户信息 ✅ 使用common模块的JwtUtil
                                Long userId = jwtUtil.getUserIdFromToken(token);
                                String username = jwtUtil.getUsernameFromToken(token);
                                String role = jwtUtil.getRoleFromToken(token);

                                // 7. 将用户信息添加到请求头中，传递给下游服务
                                ServerHttpRequest mutatedRequest = request.mutate()
                                        .header("X-User-Id", String.valueOf(userId))
                                        .header("X-Username", username)
                                        .header("X-User-Role", role)
                                        .header("X-Auth-Token", token)
                                        .build();

                                log.debug("认证通过: userId={}, username={}, role={}, path={}",
                                        userId, username, role, path);

                                return chain.filter(exchange.mutate().request(mutatedRequest).build());
                            });
                });
    }

    /**
     * 检查是否为白名单路径
     */
    private boolean isWhiteListed(String path) {
        List<String> whiteList = parseWhiteList();
        return whiteList.stream().anyMatch(path::startsWith);
    }

    /**
     * 从请求中提取 Token
     */
    private String extractToken(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        List<String> authHeaders = headers.get(HttpHeaders.AUTHORIZATION);

        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        }
        return null;
    }

    /**
     * 返回未授权响应
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        String body = String.format("{\"code\":401,\"message\":\"%s\"}", message);
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(body.getBytes()))
        );
    }

    @Override
    public int getOrder() {
        return -100; // 优先级最高
    }
}