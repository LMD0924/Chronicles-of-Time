package org.example.generalservice.vo.content;

/*
 * @Author:总会落叶
 * @Date:2026/4/10
 * @Description:
 */

import lombok.Data;

/**
 * 文章知识图谱关系边
 */
@Data
public class ContentKnowledgeEdge {
    private String source;       // 源节点ID
    private String target;       // 目标节点ID
    private String relation;     // 关系类型：has_tag(有标签), belongs_to(属于分类), related_to(相关)
    private Integer weight;      // 权重（关联强度）
}