/**
 * 文件说明：拾光记微服务后端用户中心数据实体源码，负责数据实体相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.usercenter.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @Author:总会落叶
 * @Date:2026/3/27
 * @Description: 技能特长实体类
 */
/**
 * 类说明：当前类是数据实体模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("resume_skill")
public class ResumeSkill {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("resume_id")
    private Long resumeId;

    @TableField("skill_name")
    private String skillName;

    @TableField("skill_level")
    private String skillLevel;

    @TableField("years_experience")
    private Integer yearsExperience;

    @TableField("sort_order")
    private Integer sortOrder;
}