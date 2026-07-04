/**
 * 文件说明：拾光记微服务后端通用内容服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.generalservice.vo.question;

import lombok.Data;

import java.util.Map;

/*
 * @Author:总会落叶
 * @Date:2026/4/10
 * @Description:知识图谱节点
 */
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
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
