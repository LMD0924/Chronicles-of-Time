/**
 * 文件说明：拾光记微服务后端通用内容服务内容社区源码，负责内容社区相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.generalservice.service.content;

import org.example.generalservice.vo.content.ContentKnowledgeGraph;

import java.util.List;
import java.util.Map;

/**
 * 文章知识图谱服务接口
 */
/**
 * 类说明：当前类是内容社区模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
public interface IContentKnowledgeGraphService {

    /**
     * 获取文章知识图谱（基于标签和分类）
     * @param userId 当前用户ID（可选，用于个性化）
     * @param limit 节点数量限制
     * @return 知识图谱数据
     */
    ContentKnowledgeGraph getContentKnowledgeGraph(Long userId, Integer limit);

    /**
     * 获取标签云数据
     * @param limit 标签数量限制
     * @return 标签列表及权重
     */
    List<Map<String, Object>> getTagCloud(Long userId, Integer limit);
    /**
     * 获取分类统计
     * @return 分类统计数据
     */
    List<Map<String, Object>> getCategoryStatistics(Long userId);

    /**
     * 获取标签共现网络（关联图谱）
     * @param limit 限制数量
     * @return 共现关系数据
     */
    ContentKnowledgeGraph getTagCooccurrenceGraph(Long userId, Integer limit);

    /**
     * 根据标签推荐相关内容
     * @param tag 标签名称
     * @param limit 推荐数量
     * @return 推荐文章列表
     */
    List<Map<String, Object>> getRelatedContentsByTag(Long userId, String tag, Integer limit);

    /**
     * 获取用户的内容主题分布（基于用户发布的文章标签）
     * @param userId 用户ID
     * @return 主题分布数据
     */
    Map<String, Object> getUserContentTopics(Long userId);
}