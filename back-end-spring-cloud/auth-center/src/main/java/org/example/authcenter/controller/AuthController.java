package org.example.authcenter.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.authcenter.dto.AuthDTO;
import org.example.authcenter.service.AuthService;
import org.example.authcenter.vo.LoginVO;
import org.example.commondb.utils.RestBean;
import org.springframework.web.bind.annotation.*;

/*
 * @Author:总会落叶
 * @Date:2026/3/19
 * @Description:
 */
@Slf4j
@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public RestBean<LoginVO> login(@RequestBody AuthDTO authDTO) {
        log.info("========== 收到登录请求 ==========");  // ✅ 添加日志
        log.info("用户名: {}", authDTO.getUsername());
        return RestBean.success("登录成功",authService.login(authDTO));
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public RestBean<String> register(@RequestBody AuthDTO authDTO) {
        if(authService.register(authDTO) > 0){
            return RestBean.success("注册成功");
        }
        return RestBean.fail("注册失败");
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public RestBean<LoginVO> refresh(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return RestBean.fail(401, "无效的刷新令牌");
        }
        String refreshToken = authHeader.substring(7);
        LoginVO loginVO = authService.refreshToken(refreshToken);
        return RestBean.success("刷新成功", loginVO);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public RestBean<String> logout(@RequestHeader("Authorization") String authHeader,
                                   @RequestParam Long userId) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return RestBean.fail(401, "无效的访问令牌");
        }
        String accessToken = authHeader.substring(7);
        authService.logout(accessToken, userId);
        return RestBean.success("登出成功");
    }

    /**
     * 验证Token（供网关调用）
     */
    @GetMapping("/verify")
    public RestBean<Object> verify(@RequestParam String token) {
        return authService.verifyToken(token);
    }
}
