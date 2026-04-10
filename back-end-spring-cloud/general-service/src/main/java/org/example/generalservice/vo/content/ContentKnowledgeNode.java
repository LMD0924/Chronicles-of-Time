package org.example.generalservice.vo.content;

/*
 * @Author:总会落叶
 * @Date:2026/4/10
 * @Description:
 */

import lombok.Data;

import java.util.Map;

/**
 * 文章知识图谱节点
 */
@Data
public class ContentKnowledgeNode {
    private String id;           // 节点ID
    private String name;         // 节点名称
    private String type;         // 节点类型：tag(标签)、category(分类)、user(用户)
    private Integer count;       // 出现次数
    private Long contentCount;   // 关联文章数
    private Map<String, Object> extra; // 扩展信息
}