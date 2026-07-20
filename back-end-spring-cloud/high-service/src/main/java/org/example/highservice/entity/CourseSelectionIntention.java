package org.example.highservice.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("hs_selection_intention")
public class CourseSelectionIntention {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private String userName;
    private String grade;
    private String firstSubjectIntention;

    @TableField("second_subject_intention_1")
    private String secondSubjectIntention1;

    @TableField("second_subject_intention_2")
    private String secondSubjectIntention2;

    @TableField("second_subject_backup_1")
    private String secondSubjectBackup1;

    @TableField("second_subject_backup_2")
    private String secondSubjectBackup2;

    @TableField("reason")
    private String intentionReason;

    private String targetMajor;
    private String targetUniversity;
    private String strengthSubjects;
    private String weakSubjects;
    private String careerInterest;
    private String adminFeedback;
    private String additionalFeedback;
    private Integer status;
    private LocalDateTime submitTime;
    private LocalDateTime evaluateTime;
    private String evaluateBy;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}