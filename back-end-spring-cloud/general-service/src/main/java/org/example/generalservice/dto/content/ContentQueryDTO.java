package org.example.generalservice.dto.content;

import lombok.Data;
import java.util.List;

/**
 * 内容查询DTO
 *
 * @author 总会落叶
 * @date 2026-04-06
 */
@Data
public class ContentQueryDTO {

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 分类
     */
    private String category;

    /**
     * 内容类型
     */
    private String contentType;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 搜索关键词
     */
    private String keyword;
}