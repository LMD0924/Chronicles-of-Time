package org.example.generalservice.vo.question;

import lombok.Data;

import java.util.Map;

/*
 * @Author:总会落叶
 * @Date:2026/4/10
 * @Description:知识图谱节点
 */
@Data
public class KnowledgeNode {
    private String id;           // 节点ID
    private String name;         // 节点名称
    private String type;         // 节点类型：subject(科目)、category(分类层级)、knowledge_point(知识点)
    private Integer totalCount;  // 总答题数
    private Integer correctCount;// 正确答题数
    private Double correctRate;  // 正确率
    private Integer wrongCount;  // 错误答题数
    private Map<String, Object> extra; // 扩展信息
}
