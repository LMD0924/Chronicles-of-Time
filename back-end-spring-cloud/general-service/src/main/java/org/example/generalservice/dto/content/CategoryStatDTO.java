package org.example.generalservice.dto.content;

/*
 * @Author:总会落叶
 * @Date:2026/4/10
 * @Description:
 */

import lombok.Data;

/**
 * 分类统计DTO
 */
@Data
public class CategoryStatDTO {
    private String category;
    private Long count;
    private Long contentCount;
}