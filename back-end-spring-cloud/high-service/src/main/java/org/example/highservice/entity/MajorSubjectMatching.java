package org.example.highservice.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("hs_major_subject_match")
public class MajorSubjectMatching {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String majorCode;
    private String majorName;
    private Long subjectId;
    private String subjectName;
    private Integer importanceLevel;
    private Integer matchingScore;
    private String description;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}