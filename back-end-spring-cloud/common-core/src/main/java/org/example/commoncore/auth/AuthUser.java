/**
 * 文件说明：拾光记微服务后端公共核心认证与登录源码，负责认证与登录相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.commoncore.auth;

import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 类说明：当前类是认证与登录模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@Builder
public class AuthUser {

    private Long userId;
    private String username;
    @Builder.Default
    private Set<String> roles = new HashSet<>();
    @Builder.Default
    private Set<String> permissions = new HashSet<>();

    public boolean isLogin() {
        return userId != null;
    }

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    public boolean hasAnyRole(String... roleCodes) {
        if (roleCodes == null || roles == null) {
            return false;
        }
        for (String role : roleCodes) {
            if (roles.contains(role)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasPermission(String permission) {
        return hasRole(RoleCodes.SUPER_ADMIN) || (permissions != null && permissions.contains(permission));
    }

    public boolean canManageUserResource(Long ownerUserId) {
        return hasAnyRole(RoleCodes.SUPER_ADMIN, RoleCodes.ADMIN) || (userId != null && userId.equals(ownerUserId));
    }

    public Set<String> safeRoles() {
        return roles == null ? Collections.emptySet() : roles;
    }

    public Set<String> safePermissions() {
        return permissions == null ? Collections.emptySet() : permissions;
    }
}
