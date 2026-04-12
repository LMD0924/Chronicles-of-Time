package org.example.universityservice.entity.paper;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 论文信息表
 */
@Data
@TableName("paper")
public class Paper {

    @TableId(type = IdType.AUTO)
    private Long id;                // 主键ID

    private Long userId;            // 用户ID，关联用户表

    private String title;           // 论文题目

    private String supervisor;      // 导师姓名

    private String direction;       // 研究方向

    private String content;         // 论文内容（正文）

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt; // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt; // 更新时间
}