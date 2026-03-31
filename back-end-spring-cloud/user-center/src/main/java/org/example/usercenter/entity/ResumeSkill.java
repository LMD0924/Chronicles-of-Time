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