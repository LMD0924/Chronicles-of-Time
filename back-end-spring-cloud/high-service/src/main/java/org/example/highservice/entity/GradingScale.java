package org.example.highservice.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("hs_grading_scale")
public class GradingScale {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long subjectId;
    private String subjectName;
    private String gradeLevel;
    private BigDecimal percentageTop;
    private BigDecimal percentageBottom;
    private Integer assignedScoreMin;
    private Integer assignedScoreMax;
    private BigDecimal rawScoreMin;
    private BigDecimal rawScoreMax;
    private String academicYear;
    private Boolean isActive;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}