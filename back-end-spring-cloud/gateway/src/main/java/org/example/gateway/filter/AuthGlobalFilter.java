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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
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

    @Value("${auth.white-list:/api/auth/login,/api/auth/register,/api/auth/refresh,/api/actuator/health}")
    private String whiteListRaw;

    @Value("${auth.verify-url:http://localhost:8080/api/auth/verify}")
    private String verifyUrl;

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
        if (!jwtUtil.validateToken(token) || !jwtUtil.isAccessToken(token)) {
            return unauthorized(exchange, "Token无效或已过期");
        }

        return isTokenSessionActive(token).flatMap(active -> {
            if (!active) {
                return unauthorized(exchange, "Token已失效");
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);
            String roles = String.join(",", jwtUtil.getRolesFromToken(token));
            String permissions = String.join(",", jwtUtil.getPermissionsFromToken(token));

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
    }

    private Mono<Boolean> isTokenSessionActive(String token) {
        return WebClient.create(verifyUrl)
                .get()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(TokenVerificationResponse.class)
                .map(response -> response.code() == 200)
                .onErrorReturn(false);
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
        if ("/api/ws/chat".equals(request.getURI().getPath())) {
            return request.getQueryParams().getFirst("access_token");
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

    private record TokenVerificationResponse(int code) {
    }
}
