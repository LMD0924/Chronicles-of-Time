package org.example.highservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("hs_subject")
public class Subject {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("subject_code")
    private String code;

    @TableField("subject_name")
    private String name;

    @TableField("subject_type")
    private Integer category;

    @TableField(exist = false)
    private String categoryName;

    @TableField("status")
    private Boolean isActive;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("description")
    private String description;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}