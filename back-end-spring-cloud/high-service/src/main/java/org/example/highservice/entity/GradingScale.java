/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 等级赋分表实体类
 */
package org.example.highservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("grading_scale")
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

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}