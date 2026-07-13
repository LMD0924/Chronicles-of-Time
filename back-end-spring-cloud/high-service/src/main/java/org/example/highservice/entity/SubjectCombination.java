package org.example.highservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("hs_subject_combination")
public class SubjectCombination {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("combination_code")
    private String code;

    @TableField("combination_name")
    private String name;

    @TableField("first_subject_id")
    private Long firstSubjectId;

    @TableField("second_subject_1_id")
    private Long secondSubjectId1;

    @TableField("second_subject_2_id")
    private Long secondSubjectId2;

    @TableField("description")
    private String description;

    @TableField("status")
    private Boolean isActive;

    @TableField(exist = false)
    private Integer popularityRank;

    @TableField(exist = false)
    private Subject firstSubject;

    @TableField(exist = false)
    private Subject secondSubject1;

    @TableField(exist = false)
    private Subject secondSubject2;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}