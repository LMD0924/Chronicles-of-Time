package org.example.authcenter.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/*
 * @Author:总会落叶
 * @Date:2026/3/24
 * @Description: 用户信息VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVO implements Serializable {

    private Long id;                    // 用户ID
    private String username;            // 用户账号
    private String name;                // 用户昵称
    private String email;               // 邮箱
    private String phone;               // 手机号
    private String role;                // 角色
    private String avatar;              // 头像URL
    private String introduction;        // 个人简介
    private Integer status;             // 状态：0-禁用，1-启用
    private LocalDateTime createTime;   // 创建时间
    private LocalDateTime lastLoginTime; // 最后登录时间
}