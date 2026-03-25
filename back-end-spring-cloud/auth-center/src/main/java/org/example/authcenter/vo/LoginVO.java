package org.example.authcenter.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @Author:总会落叶
 * @Date:2026/3/25
 * @Description: 登录响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    private String accessToken;      // 访问令牌
    private String refreshToken;     // 刷新令牌
    private String tokenType;        // 令牌类型，默认 Bearer
    private Long expiresIn;          // 过期时间（秒）
    private UserVO userInfo;         // 用户信息
}