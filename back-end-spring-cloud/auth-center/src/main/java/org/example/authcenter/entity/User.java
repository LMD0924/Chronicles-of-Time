/**
 * 文件说明：拾光记微服务后端认证中心数据实体源码，负责数据实体相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.authcenter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
 * @Author:总会落叶
 * @Date:2026/3/19
 * @Description:
 */
/**
 * 类说明：当前类是数据实体模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("iam_user")
public class User {

    @TableId(type = IdType.ASSIGN_ID)  // 使用雪花算法生成ID
    private Long id; //用户id
    private String username; //用户名
    @TableField("password_hash")
    private String password; //密码
    @TableField("display_name")
    private String name; //姓名
    private String email;           // 邮箱
    private String phone;           // 手机号
    @TableField(exist = false)
    private String role;            // 角色
    @TableField("avatar_url")
    private String avatar;          // 头像
    private String introduction;    // 简介
    @TableField("user_type")
    private Integer userType;       // 1-user, 2-admin
    private Integer status;         // 状态：0-禁用，1-启用
    @TableField("register_channel")
    private String registerChannel;
    @TableField("created_at")
    private LocalDateTime createTime;
    @TableField("updated_at")
    private LocalDateTime updateTime;
    @TableField("last_login_at")
    private LocalDateTime lastLoginTime;
}
