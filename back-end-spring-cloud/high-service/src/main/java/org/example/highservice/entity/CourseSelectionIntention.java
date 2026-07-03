/**
 * 文件说明：拾光记微服务后端高中服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 学生选课意向实体类（100%匹配数据库，已修复字段下划线问题）
 */
package org.example.highservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@TableName("hs_selection_intention")
public class CourseSelectionIntention {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long studentId;
    private String studentName;
    private String grade;
    private String firstSubjectIntention;

    // ====================== 【核心修复：手动绑定数据库字段】======================
    @TableField("second_subject_intention_1")
    private String secondSubjectIntention1;

    @TableField("second_subject_intention_2")
    private String secondSubjectIntention2;

    @TableField("second_subject_backup_1")
    private String secondSubjectBackup1;

    @TableField("second_subject_backup_2")
    private String secondSubjectBackup2;
    // ==========================================================================

    private String intentionReason;
    private String targetMajor;
    private String targetUniversity;
    private String strengthSubjects;
    private String weakSubjects;
    private String careerInterest;
    private String teacherFeedback;
    private String parentFeedback;
    private Integer status;
    private LocalDateTime submitTime;
    private LocalDateTime evaluateTime;
    private String evaluateBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}