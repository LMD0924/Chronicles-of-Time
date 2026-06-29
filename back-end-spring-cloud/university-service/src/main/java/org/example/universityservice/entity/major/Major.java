package org.example.universityservice.entity.major;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("major")
public class Major {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String code;

    private Integer totalCredits;

    private Integer compulsoryCredits;

    private Integer electiveCredits;

    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}