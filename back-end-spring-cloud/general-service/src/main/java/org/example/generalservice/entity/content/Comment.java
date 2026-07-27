/**
 * 文件说明：拾光记微服务后端通用内容服务内容社区源码，负责内容社区相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.generalservice.entity.content;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论实体类
 *
 * @author 总会落叶
 * @date 2026-04-06
 */
/**
 * 类说明：当前类是内容社区模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("content_comment")
public class Comment {


    /**
     * 评论ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 评论用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 内容ID
     */
    @TableField("content_id")
    private Long contentId;

    /**
     * 父评论ID（0表示顶级评论）
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 回复的用户ID
     */
    @TableField("reply_to_user_id")
    private Long replyToUserId;

    /**
     * 评论内容
     */
    @TableField("comment_text")
    private String content;

    /**
     * 点赞数
     */
    @TableField("like_count")
    private Long likesCount;

    /**
     * 状态：0-已删除，1-正常
     */
    @TableField("status")
    private Integer status;

    /**
     * 评论时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    // ==================== 以下为非数据库字段 ====================

    /**
     * 评论用户名称（非数据库字段）
     */
    @TableField(exist = false)
    private String userName;

    /**
     * 评论用户头像（非数据库字段）
     */
    @TableField(exist = false)
    private String userAvatar;

    /**
     * 回复的用户名称（非数据库字段）
     */
    @TableField(exist = false)
    private String replyToUserName;

    /**
     * 子评论列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<Comment> children;

    @TableField(exist = false)
    private Boolean liked;
}