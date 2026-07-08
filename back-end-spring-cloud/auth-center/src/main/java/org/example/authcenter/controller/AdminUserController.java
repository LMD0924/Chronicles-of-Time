package org.example.authcenter.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.authcenter.dto.AdminUserDTO;
import org.example.authcenter.service.UserService;
import org.example.authcenter.vo.PageVO;
import org.example.authcenter.vo.UserVO;
import org.example.commoncore.auth.RoleCodes;
import org.example.commoncore.utils.JwtUtil;
import org.example.commondb.utils.RestBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.List;

/**
 * Admin APIs for personal account management.
 */
@RestController
@RequestMapping("api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String ACCESS_TOKEN_PREFIX = "access_token:";
    private static final String TOKEN_BLACKLIST_PREFIX = "blacklist:";

    @GetMapping
    public RestBean<PageVO<UserVO>> pageUsers(@RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) Integer status,
                                              @RequestParam(required = false) Integer userType,
                                              @RequestParam(defaultValue = "1") Long page,
                                              @RequestParam(defaultValue = "10") Long pageSize,
                                              HttpServletRequest request) {
        RestBean<PageVO<UserVO>> forbidden = requireAdmin(request);
        if (forbidden != null) {
            return forbidden;
        }
        return RestBean.success(userService.pageAdminUsers(keyword, status, userType, page, pageSize));
    }

    @PostMapping
    public RestBean<UserVO> createUser(@RequestBody AdminUserDTO dto, HttpServletRequest request) {
        RestBean<UserVO> forbidden = requireAdmin(request);
        if (forbidden != null) {
            return forbidden;
        }
        return RestBean.success("创建成功", userService.createAdminUser(dto));
    }

    @PutMapping("/{id}")
    public RestBean<UserVO> updateUser(@PathVariable Long id,
                                       @RequestBody AdminUserDTO dto,
                                       HttpServletRequest request) {
        RestBean<UserVO> forbidden = requireAdmin(request);
        if (forbidden != null) {
            return forbidden;
        }
        return RestBean.success("更新成功", userService.updateAdminUser(id, dto));
    }

    @PatchMapping("/{id}/status")
    public RestBean<String> updateStatus(@PathVariable Long id,
                                         @RequestBody Map<String, Integer> body,
                                         HttpServletRequest request) {
        RestBean<String> forbidden = requireAdmin(request);
        if (forbidden != null) {
            return forbidden;
        }
        Integer status = body == null ? null : body.get("status");
        return userService.updateAdminUserStatus(id, status)
                ? RestBean.success("状态已更新", null)
                : RestBean.fail("状态更新失败");
    }

    @PatchMapping("/{id}/password")
    public RestBean<String> resetPassword(@PathVariable Long id,
                                          @RequestBody Map<String, String> body,
                                          HttpServletRequest request) {
        RestBean<String> forbidden = requireAdmin(request);
        if (forbidden != null) {
            return forbidden;
        }
        String password = body == null ? null : body.get("password");
        return userService.resetAdminUserPassword(id, password)
                ? RestBean.success("密码已重置", null)
                : RestBean.fail("密码重置失败");
    }

    @DeleteMapping("/{id}")
    public RestBean<String> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        RestBean<String> forbidden = requireAdmin(request);
        if (forbidden != null) {
            return forbidden;
        }
        return userService.deleteAdminUser(id)
                ? RestBean.success("删除成功", null)
                : RestBean.fail("删除失败");
    }

    private <T> RestBean<T> requireAdmin(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return RestBean.fail(401, "未登录或Token无效");
        }
        if (Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token))
                || Boolean.FALSE.equals(redisTemplate.hasKey(ACCESS_TOKEN_PREFIX + token))) {
            return RestBean.fail(401, "登录状态已失效");
        }
        List<String> roles = jwtUtil.getRolesFromToken(token);
        if (!roles.contains(RoleCodes.SUPER_ADMIN) && !roles.contains(RoleCodes.ADMIN)) {
            return RestBean.fail(403, "无后台用户管理权限");
        }
        return null;
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
