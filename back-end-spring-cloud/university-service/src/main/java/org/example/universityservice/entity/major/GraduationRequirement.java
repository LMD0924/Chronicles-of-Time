package org.example.universityservice.entity.major;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("graduation_requirement")
public class GraduationRequirement {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long majorId;

    private Integer totalCreditsRequired;

    private Integer totalCreditsEarned;

    private Integer compulsoryCreditsRequired;

    private Integer compulsoryCreditsEarned;

    private Integer electiveCreditsRequired;

    private Integer electiveCreditsEarned;

    private BigDecimal gpa;

    private Integer progressPercent;

    private LocalDate expectedGraduationDate;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}