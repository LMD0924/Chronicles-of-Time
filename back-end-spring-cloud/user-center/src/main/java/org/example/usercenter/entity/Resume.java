/**
 * 文件说明：拾光记微服务后端用户中心数据实体源码，负责数据实体相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.usercenter.entity;

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
 * @Date:2026/3/27
 * @Description:
 */
/**
 * 类说明：当前类是数据实体模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resume {

    @TableId(type = IdType.ASSIGN_ID)  // 使用雪花算法生成ID
    private Long id; //简历id
    private Long userId; // 用户id
    @TableField("real_name")
    private String name; // 姓名
    private String gender; // 性别
    private String birthDate; // 生日
    private String phone; // 电话
    private String email; // 邮箱
    private String address; // 地址
    @TableField("avatar_url")
    private String avatar; // 头像
    private String jobTitle; //求职岗位
    private String jobStatus; // 求职状态
    @TableField(exist = false)
    private Integer expectedSalary; // 期望薪资
    private Integer workYears; // 工作年限
    private String selfEvaluation; // 自我评价
    private Integer isPublic; // 是否公开 0:不公开 1:公开
    private Long viewCount; // 浏览量
    @TableField("created_at")
    private LocalDateTime createTime; // 创建时间
    @TableField("updated_at")
    private LocalDateTime updateTime; // 更新时间
}
