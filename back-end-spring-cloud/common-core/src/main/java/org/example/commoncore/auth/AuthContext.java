/**
 * 文件说明：拾光记微服务后端公共核心认证与登录源码，负责认证与登录相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.commoncore.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
/**
 * 类说明：当前类是认证与登录模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */

public final class AuthContext {

    private AuthContext() {
    }

    public static AuthUser currentUser(HttpServletRequest request) {
        Long userId = parseLong(request.getHeader("X-User-Id"));
        String username = request.getHeader("X-Username");
        Set<String> roles = splitHeader(request.getHeader("X-User-Roles"));
        String legacyRole = request.getHeader("X-User-Role");
        if (StringUtils.hasText(legacyRole)) {
            roles.add(legacyRole.trim());
        }

        return AuthUser.builder()
                .userId(userId)
                .username(username)
                .roles(roles)
                .permissions(splitHeader(request.getHeader("X-User-Permissions")))
                .build();
    }

    private static Set<String> splitHeader(String value) {
        if (!StringUtils.hasText(value)) {
            return new java.util.HashSet<>();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(java.util.HashSet::new));
    }

    private static Long parseLong(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
