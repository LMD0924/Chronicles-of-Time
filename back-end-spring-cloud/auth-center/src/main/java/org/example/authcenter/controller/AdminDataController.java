package org.example.authcenter.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.authcenter.mapper.AuthMapper;
import org.example.authcenter.service.AdminDataService;
import org.example.commoncore.auth.RoleCodes;
import org.example.commoncore.utils.JwtUtil;
import org.example.commondb.utils.RestBean;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/admin/data")
@RequiredArgsConstructor
public class AdminDataController {

    private final AdminDataService adminDataService;
    private final JwtUtil jwtUtil;
    private final AuthMapper authMapper;

    @GetMapping("/dashboard")
    public RestBean<Map<String, Object>> dashboard(HttpServletRequest request) {
        RestBean<Map<String, Object>> forbidden = requireAdmin(request);
        return forbidden == null ? RestBean.success(adminDataService.dashboard()) : forbidden;
    }

    @GetMapping("/modules")
    public RestBean<Map<String, Object>> module(@RequestParam String moduleKey,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String status,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int pageSize,
                                                HttpServletRequest request) {
        RestBean<Map<String, Object>> forbidden = requireAdmin(request);
        if (forbidden != null) return forbidden;
        try {
            return RestBean.success(adminDataService.list(moduleKey, keyword, status, page, pageSize));
        } catch (IllegalArgumentException exception) {
            return RestBean.fail(400, exception.getMessage());
        }
    }

    @GetMapping("/modules/{moduleKey}/{id}")
    public RestBean<Map<String, Object>> detail(@PathVariable String moduleKey, @PathVariable String id,
                                                HttpServletRequest request) {
        RestBean<Map<String, Object>> forbidden = requireAdmin(request);
        if (forbidden != null) return forbidden;
        return execute(() -> adminDataService.detail(moduleKey, id));
    }

    @PostMapping("/modules/{moduleKey}")
    public RestBean<Map<String, Object>> create(@PathVariable String moduleKey,
                                                @RequestBody(required = false) Map<String, Object> payload,
                                                HttpServletRequest request) {
        RestBean<Map<String, Object>> forbidden = requireAdmin(request);
        if (forbidden != null) return forbidden;
        return execute(() -> {
            String id = adminDataService.create(moduleKey, payload, adminUserId(request), request.getRequestURI(),
                    clientIp(request), request.getHeader("User-Agent"));
            return Map.of("id", id);
        });
    }

    @PutMapping("/modules/{moduleKey}/{id}")
    public RestBean<Map<String, Object>> update(@PathVariable String moduleKey, @PathVariable String id,
                                                @RequestBody(required = false) Map<String, Object> payload,
                                                HttpServletRequest request) {
        RestBean<Map<String, Object>> forbidden = requireAdmin(request);
        if (forbidden != null) return forbidden;
        return execute(() -> {
            adminDataService.update(moduleKey, id, payload, adminUserId(request), request.getRequestURI(),
                    clientIp(request), request.getHeader("User-Agent"));
            return Map.of("id", id);
        });
    }

    @DeleteMapping("/modules/{moduleKey}/{id}")
    public RestBean<Map<String, Object>> delete(@PathVariable String moduleKey, @PathVariable String id,
                                                HttpServletRequest request) {
        RestBean<Map<String, Object>> forbidden = requireAdmin(request);
        if (forbidden != null) return forbidden;
        return execute(() -> {
            adminDataService.delete(moduleKey, id, adminUserId(request), request.getRequestURI(),
                    clientIp(request), request.getHeader("User-Agent"));
            return Map.of("id", id);
        });
    }

    private RestBean<Map<String, Object>> execute(AdminOperation operation) {
        try {
            return RestBean.success(operation.run());
        } catch (IllegalArgumentException exception) {
            return RestBean.fail(400, exception.getMessage());
        } catch (DataAccessException exception) {
            String message = exception.getMostSpecificCause() == null ? exception.getMessage()
                    : exception.getMostSpecificCause().getMessage();
            return RestBean.fail(400, "数据操作失败：" + message);
        }
    }

    private long adminUserId(HttpServletRequest request) {
        return jwtUtil.getUserIdFromToken(request.getHeader(HttpHeaders.AUTHORIZATION).substring(7));
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }

    @FunctionalInterface
    private interface AdminOperation {
        Map<String, Object> run();
    }
    private <T> RestBean<T> requireAdmin(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return RestBean.fail(401, "未登录或Token无效");
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token) || !jwtUtil.isAccessToken(token)) {
            return RestBean.fail(401, "登录状态已失效");
        }
        String sessionId = jwtUtil.getTokenId(token);
        if (sessionId == null || sessionId.isBlank()
                || authMapper.countActiveTokenSessions(jwtUtil.getUserIdFromToken(token), sessionId) == 0) {
            return RestBean.fail(401, "登录状态已失效");
        }
        List<String> roles = jwtUtil.getRolesFromToken(token);
        if (!roles.contains(RoleCodes.SUPER_ADMIN) && !roles.contains(RoleCodes.ADMIN)) {
            return RestBean.fail(403, "无后台数据访问权限");
        }
        return null;
    }
}
