package org.example.highservice.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.Year;

@Data
@TableName("gaokao_major_requirement")
public class MajorRequirement {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String majorCode;
    private String majorName;
    private String category;
    private String firstSubjectRequired;
    private String secondSubjectRequired;
    private String requirementDetail;
    private String universityName;
    private String universityLevel;

    @TableField(exist = false)
    private Double avgMatchingScore;

    @TableField(exist = false)
    private String matchedSubjects;

    private String province;
    private Year admissionYear;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}