package org.example.authcenter.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @Author:总会落叶
 * @Date:2026/3/19
 * @Description: 认证请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthDTO {

    @NotEmpty(message = "用户名不能为空")
    private String username;

    @NotEmpty(message = "密码不能为空")
    private String password;

    // 可选：验证码字段
    private String captcha;

    // 可选：记住我（延长Token有效期）
    private Boolean rememberMe = false;
}