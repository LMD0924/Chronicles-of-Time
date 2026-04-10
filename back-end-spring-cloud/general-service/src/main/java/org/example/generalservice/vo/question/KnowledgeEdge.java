package org.example.generalservice.vo.question;

/*
 * @Author:总会落叶
 * @Date:2026/4/10
 * @Description:
 */

import lombok.Data;

/**
 * 知识图谱关系边
 */
@Data
public class KnowledgeEdge {
    private String source;       // 源节点ID
    private String target;       // 目标节点ID
    private String relation;     // 关系类型：belongs_to(属于), related_to(相关)
    private Integer weight;      // 权重（关联强度）
    private Double correctRate;  // 该路径上的正确率
}