/**
 * 文件说明：拾光记微服务后端网关网关过滤源码，负责网关过滤相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 类说明：当前类是网关过滤模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    @Value("${auth.white-list:/api/auth/login,/api/auth/register,/api/auth/refresh,/api/actuator/health}")
    private String whiteListRaw;

    private static final String ACCESS_TOKEN_PREFIX = "access_token:";
    private static final String TOKEN_BLACKLIST_PREFIX = "blacklist:";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isWhiteListed(path)) {
            return chain.filter(exchange);
        }


        String token = extractToken(request);
        if (token == null) {
            return unauthorized(exchange, "未提供认证Token");
        }
        if (token.chars().filter(ch -> ch == '.').count() != 2) {
            return unauthorized(exchange, "Token格式错误");
        }
        if (!jwtUtil.validateToken(token)) {
            return unauthorized(exchange, "Token无效或已过期");
        }



        // Redis 同时承担黑名单和在线 token 校验，支持服务端主动踢下线和注销后即时失效。
        return redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token)
                .flatMap(isBlacklisted -> {
                    if (Boolean.TRUE.equals(isBlacklisted)) {
                        return unauthorized(exchange, "Token已被注销");
                    }
                    return redisTemplate.hasKey(ACCESS_TOKEN_PREFIX + token)
                            .flatMap(exists -> {
                                if (Boolean.FALSE.equals(exists)) {
                                    return unauthorized(exchange, "Token已失效");
                                }

                                Long userId = jwtUtil.getUserIdFromToken(token);
                                String username = jwtUtil.getUsernameFromToken(token);
                                String role = jwtUtil.getRoleFromToken(token);
                                String roles = String.join(",", jwtUtil.getRolesFromToken(token));
                                String permissions = String.join(",", jwtUtil.getPermissionsFromToken(token));



                                // 将用户身份透传给下游服务，下游可通过请求头构造 AuthContext。
                                ServerHttpRequest mutatedRequest = request.mutate()
                                        .header("X-User-Id", String.valueOf(userId))
                                        .header("X-Username", username)
                                        .header("X-User-Role", role)
                                        .header("X-User-Roles", roles)
                                        .header("X-User-Permissions", permissions)
                                        .header("X-Auth-Token", token)
                                        .build();
                                log.debug("认证通过: userId={}, username={}, roles={}, path={}", userId, username, roles, path);
                                return chain.filter(exchange.mutate().request(mutatedRequest).build());
                            });
                });
    }

    private boolean isWhiteListed(String path) {
        return parseWhiteList().stream().anyMatch(path::startsWith);
    }

    private List<String> parseWhiteList() {
        if (whiteListRaw == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(whiteListRaw.replace("[", "").replace("]", "").split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private String extractToken(ServerHttpRequest request) {
        List<String> authHeaders = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        }
        return null;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        String body = String.format("{\"code\":401,\"msg\":\"%s\"}", message);
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8)))
        );
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
