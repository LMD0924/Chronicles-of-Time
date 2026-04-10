package org.example.generalservice.vo.question;

/*
 * @Author:总会落叶
 * @Date:2026/4/10
 * @Description:
 */

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 完整知识图谱
 */
@Data
public class KnowledgeGraph {
    private List<KnowledgeNode> nodes;
    private List<KnowledgeEdge> edges;
    private Map<String, Object> statistics; // 整体统计信息
}
