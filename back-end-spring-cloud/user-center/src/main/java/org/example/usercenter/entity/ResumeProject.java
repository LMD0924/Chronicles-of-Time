package org.example.usercenter.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/*
 * @Author:总会落叶
 * @Date:2026/3/27
 * @Description: 项目经验实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("resume_project")
public class ResumeProject {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("resume_id")
    private Long resumeId;

    @TableField("project_name")
    private String projectName;

    @TableField("project_role")
    private String projectRole;

    @TableField("start_date")
    private LocalDate startDate;

    @TableField("end_date")
    private LocalDate endDate;

    private String description;

    private String responsibilities;

    @TableField("tech_stack")
    private String techStack;

    @TableField("project_url")
    private String projectUrl;

    @TableField("sort_order")
    private Integer sortOrder;
}