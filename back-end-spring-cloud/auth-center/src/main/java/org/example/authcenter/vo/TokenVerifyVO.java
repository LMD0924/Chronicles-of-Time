package org.example.authcenter.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @Author:总会落叶
 * @Date:2026/3/25
 * @Description: Token验证响应VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenVerifyVO {

    private Boolean valid;           // 是否有效
    private Long userId;             // 用户ID
    private String username;         // 用户名
    private String role;             // 角色
    private String message;          // 错误信息
}