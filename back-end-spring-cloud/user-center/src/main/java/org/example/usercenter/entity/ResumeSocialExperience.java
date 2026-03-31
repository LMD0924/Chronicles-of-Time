package org.example.usercenter.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/*
 * @Author:总会落叶
 * @Date:2026/3/27
 * @Description: 社会/校园经历实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("resume_social_experience")
public class ResumeSocialExperience {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("resume_id")
    private Long resumeId;

    @TableField("experience_type")
    private String experienceType;

    private String title;

    @TableField("start_date")
    private LocalDate startDate;

    @TableField("end_date")
    private LocalDate endDate;

    private String description;

    private String achievements;

    @TableField("sort_order")
    private Integer sortOrder;
}