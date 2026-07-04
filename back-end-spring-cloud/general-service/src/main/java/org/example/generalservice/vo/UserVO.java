/**
 * 文件说明：拾光记微服务后端通用内容服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.generalservice.vo;

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
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
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