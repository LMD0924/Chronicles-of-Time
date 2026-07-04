/**
 * 文件说明：拾光记微服务后端大学服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.universityservice.entity.paper;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 修改意见表
 */
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@TableName("thesis_suggestion")
public class Suggestion {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;                // 主键ID

    private Long paperId;           // 关联的论文ID

    @TableField("suggestion_content")
    private String content;         // 修改意见内容

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt; // 意见提出时间
}