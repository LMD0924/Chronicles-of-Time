package org.example.universityservice.entity.paper;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 修改意见表
 */
@Data
@TableName("suggestion")
public class Suggestion {

    @TableId(type = IdType.AUTO)
    private Long id;                // 主键ID

    private Long paperId;           // 关联的论文ID

    private String content;         // 修改意见内容

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt; // 意见提出时间
}