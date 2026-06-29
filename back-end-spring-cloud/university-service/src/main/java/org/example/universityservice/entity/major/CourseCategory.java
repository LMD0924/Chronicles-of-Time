package org.example.universityservice.entity.major;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("course_category")
public class CourseCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long majorId;

    private String name;

    private Long parentId;

    private Integer sortOrder;

    private String icon;

    private String color;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}