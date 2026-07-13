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
import org.example.generalservice.vo.UserVO;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 内容实体类
 * 统一管理文章、日记、随笔等内容
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
@TableName("content_article")
public class Content {

    /**
     * 内容ID（主键，使用bigint支持长位数）
     */
    @JsonSerialize(using = ToStringSerializer.class) // 加这行
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 作者ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 标题
     */
    @TableField("title")
    private String title;

    /**
     * 摘要
     */
    @TableField("summary")
    private String summary;

    /**
     * 内容（支持长文本）
     */
    @TableField("content_md")
    private String content;

    /**
     * 内容类型：article-文章，diary-日记，essay-随笔，note-笔记，story-故事
     */
    @TableField("content_type")
    private String contentType;

    /**
     * 封面图片URL
     */
    @TableField("cover_image_url")
    private String coverImage;

    /**
     * 多张图片JSON数组（存储为JSON格式）
     */
    @TableField(exist = false)
    private String images;

    /**
     * 地点
     */
    @TableField("location")
    private String location;

    /**
     * 天气
     */
    @TableField("weather")
    private String weather;

    /**
     * 心情
     */
    @TableField("mood")
    private String mood;

    /**
     * 标签（用户直接输入，逗号分隔）
     */
    @TableField(exist = false)
    private String tags;

    /**
     * 分类（用户直接输入）
     */
    /**
     * 分类ID
     */
    @TableField("category_id")
    private Long categoryId;

    @TableField(exist = false)
    private String category;

    /**
     * 是否公开：0-仅自己，1-公开，2-仅好友
     */
    @TableField("visibility")
    private Integer isPublic;

    /**
     * 是否置顶：0-否，1-是
     */
    @TableField("is_top")
    private Integer isTop;

    /**
     * 是否精华：0-否，1-是
     */
    @TableField("is_essence")
    private Integer isEssence;

    /**
     * 状态：0-草稿，1-已发布，2-已删除
     */
    @TableField("status")
    private Integer status;

    /**
     * 浏览量
     */
    @TableField("view_count")
    private Long views;

    /**
     * 点赞数
     */
    @TableField("like_count")
    private Long likesCount;

    /**
     * 收藏数
     */
    @TableField("favorite_count")
    private Long favoritesCount;

    /**
     * 评论数
     */
    @TableField("comment_count")
    private Long commentsCount;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    /**
     * 发布时间
     */
    @TableField("publish_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime publishTime;

    // ==================== 以下为非数据库字段 ====================

    /**
     * 作者名称（非数据库字段）
     */
    @TableField(exist = false)
    private String authorName;

    /**
     * 作者头像（非数据库字段）
     */
    @TableField(exist = false)
    private String authorAvatar;

    /**
     * 图片列表（非数据库字段，用于前端展示）
     */
    @TableField(exist = false)
    private List<String> imageList;

    /**
     * 标签列表（非数据库字段，用于前端展示）
     */
    @TableField(exist = false)
    private List<String> tagList;

    /**
     * 当前用户是否已点赞（非数据库字段）
     */
    @TableField(exist = false)
    private Boolean isLiked;

    /**
     * 当前用户是否已收藏（非数据库字段）
     */
    @TableField(exist = false)
    private Boolean isFavorited;

    @TableField(exist = false)
    private UserVO author;
}