package org.example.generalservice.vo.content;

/*
 * @Author:总会落叶
 * @Date:2026/4/10
 * @Description:
 */

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 完整文章知识图谱
 */
@Data
public class ContentKnowledgeGraph {
    private List<ContentKnowledgeNode> nodes;
    private List<ContentKnowledgeEdge> edges;
    private Map<String, Object> statistics;
}
