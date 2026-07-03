/**
 * 文件说明：拾光记微服务后端通用内容服务内容社区源码，负责内容社区相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
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
/**
 * 类说明：当前类是内容社区模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
public class ContentKnowledgeEdge {
    private String source;       // 源节点ID
    private String target;       // 目标节点ID
    private String relation;     // 关系类型：has_tag(有标签), belongs_to(属于分类), related_to(相关)
    private Integer weight;      // 权重（关联强度）
}