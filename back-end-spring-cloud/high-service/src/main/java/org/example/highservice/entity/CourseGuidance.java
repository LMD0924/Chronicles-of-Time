/**
 * 文件说明：拾光记微服务后端高中服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
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

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@TableName("hs_course_guidance")
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