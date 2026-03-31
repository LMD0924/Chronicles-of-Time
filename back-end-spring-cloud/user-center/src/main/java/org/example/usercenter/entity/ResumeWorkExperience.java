package org.example.usercenter.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/*
 * @Author:总会落叶
 * @Date:2026/3/27
 * @Description: 工作经历实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("resume_work_experience")
public class ResumeWorkExperience {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("resume_id")
    private Long resumeId;

    @TableField("company_name")
    private String companyName;

    private String position;

    @TableField("start_date")
    private LocalDate startDate;

    @TableField("end_date")
    private LocalDate endDate;

    @TableField("is_current")
    private Boolean isCurrent;

    private String description;

    private String achievements;

    @TableField("sort_order")
    private Integer sortOrder;
}