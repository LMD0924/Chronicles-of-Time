package org.example.universityservice.entity.major;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("student_course")
public class StudentCourse {

    @TableId(type = IdType.AUTO)
    private Long id;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotNull(message = "专业ID不能为空")
    private Long majorId;

    @NotNull(message = "修读学期不能为空")
    private String semester;

    private BigDecimal score;

    private BigDecimal gradePoint;

    private Integer isPassed;

    private Integer isRetake;

    private Integer retakeCount;

    private String status;

    private String notes;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}