package org.example.authcenter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")
public class User {

    @TableId(type = IdType.ASSIGN_ID)  // 使用雪花算法生成ID
    private Long id; //用户id
    private String username; //用户名
    private String password; //密码
    private String name; //姓名
    private String email;           // 邮箱
    private String phone;           // 手机号
    private String role;            // 角色
    private String avatar;          // 头像
    private Integer status;         // 状态：0-禁用，1-启用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime lastLoginTime;
}