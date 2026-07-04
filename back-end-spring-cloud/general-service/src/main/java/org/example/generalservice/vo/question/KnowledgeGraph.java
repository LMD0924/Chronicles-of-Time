/**
 * 文件说明：拾光记微服务后端通用内容服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
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
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
public class KnowledgeGraph {
    private List<KnowledgeNode> nodes;
    private List<KnowledgeEdge> edges;
    private Map<String, Object> statistics; // 整体统计信息
}
