/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 选科指导记录实体类
 */
package org.example.highservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("course_guidance")
public class CourseGuidance {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long studentId;
    private String studentName;
    private LocalDate guidanceDate;
    private String guidanceType;
    private String guidanceContent;
    private String suggestedCombination;
    private String suggestedMajor;
    private String strengthAnalysis;
    private String weaknessAnalysis;
    private String opportunityAnalysis;
    private String threatAnalysis;
    private String actionPlan;
    private String advisorName;
    private String advisorPosition;
    private String studentFeedback;
    private String parentFeedback;
    private LocalDate followUpDate;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}