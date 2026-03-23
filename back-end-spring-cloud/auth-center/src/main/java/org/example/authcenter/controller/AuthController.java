package org.example.authcenter.controller;

import lombok.RequiredArgsConstructor;
import org.example.authcenter.dto.AuthDTO;
import org.example.authcenter.service.AuthService;
import org.example.commoncore.utils.RestBean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * @Author:总会落叶
 * @Date:2026/3/19
 * @Description:
 */
@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public RestBean<String> login(@RequestBody AuthDTO authDTO) {
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
}
