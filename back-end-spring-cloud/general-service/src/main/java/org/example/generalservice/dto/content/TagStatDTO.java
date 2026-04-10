package org.example.generalservice.dto.content;

/*
 * @Author:总会落叶
 * @Date:2026/4/10
 * @Description:
 */

import lombok.Data;

/**
 * 标签统计DTO
 */
@Data
public class TagStatDTO {
    private String tag;
    private Long count;
    private Long contentCount;
}