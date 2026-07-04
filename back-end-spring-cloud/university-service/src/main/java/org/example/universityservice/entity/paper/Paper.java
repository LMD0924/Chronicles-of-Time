/**
 * 文件说明：拾光记微服务后端大学服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.universityservice.entity.paper;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 论文信息表
 */
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@TableName("thesis_paper")
public class Paper {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;                // 主键ID

    private Long userId;            // 用户ID，关联用户表

    private String title;           // 论文题目

    private String supervisor;      // 导师姓名

    @TableField("research_direction")
    private String direction;       // 研究方向

    @TableField("content_md")
    private String content;         // 论文内容（正文）

    /** 论文阶段：topic / proposal / draft / defense / done */
    private String stage;

    /** 状态：draft / in_progress / submitted / passed */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt; // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt; // 更新时间
}