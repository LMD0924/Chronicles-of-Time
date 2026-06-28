package org.example.universityservice.entity.major;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("course")
public class Course {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long majorId;

    private Long categoryId;

    private String courseCode;

    private String name;

    private BigDecimal credit;

    private Integer totalHours;

    private Integer theoryHours;

    private Integer labHours;

    private String courseType;

    private Integer term;

    private String examType;

    private String description;

    private String prerequisite;

    private Integer sortOrder;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}