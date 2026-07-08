/**
 * 文件说明：拾光记微服务后端认证中心请求数据传输源码，负责请求数据传输相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
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
/**
 * 类说明：当前类是请求数据传输模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthDTO {

    @NotEmpty(message = "用户名不能为空")
    private String username;

    @NotEmpty(message = "密码不能为空")
    private String password;

    // Optional display name collected by the register page.
    private String name;

    // Backward-compatible alias for clients that submit displayName.
    private String displayName;

    private String email;

    private String phone;

    // 可选：验证码字段
    private String captcha;

    // 可选：记住我（延长Token有效期）
    private Boolean rememberMe = false;
}
