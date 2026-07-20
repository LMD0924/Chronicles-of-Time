package org.example.highservice.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("hs_course_guidance")
public class CourseGuidance {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private String userName;
    private LocalDate guidanceDate;
    private String guidanceType;

    @TableField("content")
    private String guidanceContent;

    @TableField("recommended_combination_name")
    private String suggestedCombination;

    private String suggestedMajor;
    private String strengthAnalysis;
    private String weaknessAnalysis;
    private String opportunityAnalysis;
    private String threatAnalysis;
    private String actionPlan;
    private String advisorName;
    private String advisorPosition;
    private String userFeedback;
    private String additionalFeedback;
    private LocalDate followUpDate;
    private Integer status;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}