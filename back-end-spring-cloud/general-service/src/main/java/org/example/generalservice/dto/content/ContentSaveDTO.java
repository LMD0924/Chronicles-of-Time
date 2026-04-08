package org.example.generalservice.dto.content;

import lombok.Data;
import java.util.List;

/**
 * 内容保存DTO
 *
 * @author 总会落叶
 * @date 2026-04-06
 */
@Data
public class ContentSaveDTO {

    /**
     * 内容ID（更新时传入）
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 内容类型：article-文章，diary-日记，essay-随笔，note-笔记，story-故事
     */
    private String contentType;

    /**
     * 封面图片URL
     */
    private String coverImage;

    /**
     * 图片URL列表
     */
    private List<String> images;

    /**
     * 地点
     */
    private String location;

    /**
     * 天气
     */
    private String weather;

    /**
     * 心情
     */
    private String mood;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 分类
     */
    private String category;

    /**
     * 是否公开：0-仅自己，1-公开，2-仅好友
     */
    private Integer isPublic;

    /**
     * 状态：0-草稿，1-已发布
     */
    private Integer status;
}