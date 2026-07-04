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

import java.util.Map;

/**
 * 文章知识图谱节点
 */
/**
 * 类说明：当前类是内容社区模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
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