/**
 * 文件说明：拾光记微服务后端认证中心接口控制器源码，负责接口控制器相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.authcenter.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.authcenter.dto.AuthDTO;
import org.example.authcenter.service.AuthService;
import org.example.authcenter.vo.LoginVO;
import org.example.commondb.enums.ResultCodeEnum;
import org.springframework.dao.DuplicateKeyException;
import org.example.commondb.utils.RestBean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

/*
 * @Author:总会落叶
 * @Date:2026/3/19
 * @Description:
 */
/**
 * 类说明：当前类是接口控制器模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
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
    public RestBean<LoginVO> login(@RequestBody(required = false) AuthDTO authDTO) {
        log.info("========== 收到登录请求 ==========");  // ✅ 添加日志
        log.info("用户名: {}", authDTO == null ? null : authDTO.getUsername());
        try {
            return RestBean.success("登录成功", authService.login(authDTO));
        } catch (RuntimeException e) {
            String message = e.getMessage();
            if ("用户不存在".equals(message)) {
                return RestBean.fail(ResultCodeEnum.NOT_FOUND.getCode(), message);
            }
            if ("密码错误".equals(message)) {
                return RestBean.fail(ResultCodeEnum.LOGIN_FAIL.getCode(), "账号或密码错误");
            }
            if ("用户已被禁用".equals(message)) {
                return RestBean.fail(ResultCodeEnum.USER_DISABLED.getCode(), message);
            }
            if ("用户名或密码不能为空".equals(message)) {
                return RestBean.fail(ResultCodeEnum.PARAM_ERROR.getCode(), message);
            }
            log.error("用户登录失败", e);
            return RestBean.fail(ResultCodeEnum.SERVER_ERROR.getCode(), "登录服务暂时不可用，请稍后重试");
        }
    }
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public RestBean<String> register(@RequestBody AuthDTO authDTO) {
        try {
            if (authService.register(authDTO) > 0) {
                return RestBean.success("注册成功");
            }
            return RestBean.fail("注册失败");
        } catch (DuplicateKeyException e) {
            return RestBean.fail(ResultCodeEnum.REGISTER_FAIL.getCode(), "用户名已存在");
        } catch (RuntimeException e) {
            return RestBean.fail(ResultCodeEnum.REGISTER_FAIL.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("用户注册失败", e);
            return RestBean.fail("注册失败，请稍后重试");
        }
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
    public RestBean<Object> verify(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return RestBean.fail(401, "无效的访问令牌");
        }
        return authService.verifyToken(authHeader.substring(7));
    }
}
