package org.example.generalservice.service.content.Impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.generalservice.entity.content.Content;
import org.example.generalservice.mapper.content.ContentMapper;
import org.example.generalservice.service.content.IContentKnowledgeGraphService;
import org.example.generalservice.vo.content.*;
import org.springframework.stereotype.Service;
import cn.hutool.core.util.StrUtil;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@DS("futurestack")
public class ContentKnowledgeGraphServiceImpl implements IContentKnowledgeGraphService {

    private final ContentMapper contentMapper;

    /**
     * 获取用户自己的文章知识图谱
     */
    @Override
    public ContentKnowledgeGraph getContentKnowledgeGraph(Long userId, Integer limit) {
        log.info("构建用户文章知识图谱: userId={}, limit={}", userId, limit);

        if (userId == null) {
            log.warn("用户未登录，无法获取个性化知识图谱");
            return buildEmptyGraph();
        }

        if (limit == null || limit <= 0) {
            limit = 50;
        }

        ContentKnowledgeGraph graph = new ContentKnowledgeGraph();

        // ✅ 关键：只查询当前用户的数据，返回类型是 List<Map<String, Object>>
        List<Map<String, Object>> tags = contentMapper.selectUserTagsWithCount(userId, limit);
        if (tags == null) tags = new ArrayList<>();

        List<Map<String, Object>> categories = contentMapper.selectUserCategoriesWithCount(userId);
        if (categories == null) categories = new ArrayList<>();

        if (tags.isEmpty() && categories.isEmpty()) {
            return buildEmptyGraph();
        }

        // 构建节点
        List<ContentKnowledgeNode> nodes = new ArrayList<>();
        Map<String, ContentKnowledgeNode> nodeMap = new HashMap<>();

        // 添加标签节点 - 使用 Map 取值
        for (Map<String, Object> tag : tags) {
            ContentKnowledgeNode node = new ContentKnowledgeNode();
            String tagName = (String) tag.get("tag");
            node.setId("tag_" + tagName);
            node.setName(tagName);
            node.setType("tag");
            // 从 Map 中获取 count 和 content_count
            Number count = (Number) tag.get("count");
            Number contentCount = (Number) tag.get("content_count");
            node.setCount(count != null ? count.intValue() : 0);
            node.setContentCount(contentCount != null ? contentCount.longValue() : 0L);
            nodes.add(node);
            nodeMap.put("tag_" + tagName, node);
        }

        // 添加分类节点 - 使用 Map 取值
        for (Map<String, Object> category : categories) {
            ContentKnowledgeNode node = new ContentKnowledgeNode();
            String categoryName = (String) category.get("category");
            node.setId("category_" + categoryName);
            node.setName(categoryName);
            node.setType("category");
            Number count = (Number) category.get("count");
            Number contentCount = (Number) category.get("content_count");
            node.setCount(count != null ? count.intValue() : 0);
            node.setContentCount(contentCount != null ? contentCount.longValue() : 0L);
            nodes.add(node);
            nodeMap.put("category_" + categoryName, node);
        }

        // 构建关系边（用户自己的文章中的标签-分类关系）
        List<ContentKnowledgeEdge> edges = new ArrayList<>();
        Map<String, Integer> edgeWeights = new HashMap<>();

        for (Map<String, Object> tag : tags) {
            String tagName = (String) tag.get("tag");
            // ✅ 只查询当前用户关联的文章
            List<Long> contentIds = contentMapper.selectUserContentIdsByTag(userId, tagName);
            if (contentIds.isEmpty()) continue;

            // 查询这些文章的分类
            LambdaQueryWrapper<Content> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(Content::getId, contentIds)
                    .eq(Content::getUserId, userId)
                    .select(Content::getCategory)
                    .groupBy(Content::getCategory);
            List<Content> contents = contentMapper.selectList(wrapper);

            for (Content content : contents) {
                if (StrUtil.isBlank(content.getCategory())) continue;
                String key = "tag_" + tagName + "->category_" + content.getCategory();
                edgeWeights.put(key, edgeWeights.getOrDefault(key, 0) + 1);
            }
        }

        // 构建边对象
        for (Map.Entry<String, Integer> entry : edgeWeights.entrySet()) {
            String[] parts = entry.getKey().split("->");
            if (parts.length == 2 && nodeMap.containsKey(parts[0]) && nodeMap.containsKey(parts[1])) {
                ContentKnowledgeEdge edge = new ContentKnowledgeEdge();
                edge.setSource(parts[0]);
                edge.setTarget(parts[1]);
                edge.setRelation("belongs_to");
                edge.setWeight(entry.getValue());
                edges.add(edge);
            }
        }

        // 添加标签之间的共现关系（用户自己的文章）
        List<Map<String, Object>> cooccurrence = contentMapper.selectUserTagCooccurrence(userId, limit * 2);
        for (Map<String, Object> co : cooccurrence) {
            String tag1 = "tag_" + co.get("tag1");
            String tag2 = "tag_" + co.get("tag2");
            Integer weight = ((Number) co.get("cooccurrence")).intValue();

            if (nodeMap.containsKey(tag1) && nodeMap.containsKey(tag2)) {
                ContentKnowledgeEdge edge = new ContentKnowledgeEdge();
                edge.setSource(tag1);
                edge.setTarget(tag2);
                edge.setRelation("related_to");
                edge.setWeight(weight);
                edges.add(edge);
            }
        }

        graph.setNodes(nodes);
        graph.setEdges(edges);
        graph.setStatistics(buildUserStatistics(tags, categories, userId));

        return graph;
    }

    /**
     * 获取用户自己的标签云
     */
    @Override
    public List<Map<String, Object>> getTagCloud(Long userId, Integer limit) {
        log.info("获取用户标签云: userId={}, limit={}", userId, limit);

        if (userId == null) return new ArrayList<>();

        List<Map<String, Object>> tags = contentMapper.selectUserTagsWithCount(userId, limit);
        if (tags == null) tags = new ArrayList<>();

        // 计算字体大小范围
        long maxCount = tags.stream()
                .mapToLong(t -> ((Number) t.get("count")).longValue())
                .max().orElse(1);
        long minCount = tags.stream()
                .mapToLong(t -> ((Number) t.get("count")).longValue())
                .min().orElse(1);

        List<Map<String, Object>> tagCloud = new ArrayList<>();
        for (Map<String, Object> tag : tags) {
            Map<String, Object> item = new HashMap<>();
            String tagName = (String) tag.get("tag");
            long count = ((Number) tag.get("count")).longValue();
            item.put("name", tagName);
            item.put("value", count);
            int fontSize = 12 + (int) ((count - minCount) * 36.0 / Math.max(1, maxCount - minCount));
            item.put("size", fontSize);
            tagCloud.add(item);
        }

        return tagCloud;
    }

    /**
     * 获取用户自己的分类统计
     */
    @Override
    public List<Map<String, Object>> getCategoryStatistics(Long userId) {
        log.info("获取用户分类统计: userId={}", userId);

        if (userId == null) return new ArrayList<>();

        List<Map<String, Object>> categories = contentMapper.selectUserCategoriesWithCount(userId);
        if (categories == null) categories = new ArrayList<>();

        long totalCount = categories.stream()
                .mapToLong(c -> ((Number) c.get("count")).longValue())
                .sum();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> category : categories) {
            Map<String, Object> item = new HashMap<>();
            String categoryName = (String) category.get("category");
            long count = ((Number) category.get("count")).longValue();
            item.put("name", categoryName);
            item.put("value", count);
            item.put("percentage", totalCount > 0 ? (double) count / totalCount * 100 : 0);
            result.add(item);
        }

        return result;
    }

    /**
     * 获取用户自己的标签共现网络
     */
    @Override
    public ContentKnowledgeGraph getTagCooccurrenceGraph(Long userId, Integer limit) {
        log.info("获取用户标签共现网络: userId={}, limit={}", userId, limit);

        if (userId == null) return buildEmptyGraph();

        if (limit == null || limit <= 0) {
            limit = 30;
        }

        ContentKnowledgeGraph graph = new ContentKnowledgeGraph();

        // ✅ 只查询当前用户的标签共现关系
        List<Map<String, Object>> cooccurrence = contentMapper.selectUserTagCooccurrence(userId, limit);

        // 收集所有出现的标签
        Set<String> tagSet = new HashSet<>();
        for (Map<String, Object> co : cooccurrence) {
            tagSet.add((String) co.get("tag1"));
            tagSet.add((String) co.get("tag2"));
        }

        // 获取用户标签使用次数
        List<Map<String, Object>> tagStats = contentMapper.selectUserTagsWithCount(userId, null);
        Map<String, Long> tagCountMap = new HashMap<>();
        for (Map<String, Object> stat : tagStats) {
            String tagName = (String) stat.get("tag");
            Long count = ((Number) stat.get("count")).longValue();
            tagCountMap.put(tagName, count);
        }

        // 构建节点
        List<ContentKnowledgeNode> nodes = new ArrayList<>();
        for (String tag : tagSet) {
            ContentKnowledgeNode node = new ContentKnowledgeNode();
            node.setId("tag_" + tag);
            node.setName(tag);
            node.setType("tag");
            node.setCount(tagCountMap.getOrDefault(tag, 0L).intValue());
            nodes.add(node);
        }

        // 构建边
        List<ContentKnowledgeEdge> edges = new ArrayList<>();
        for (Map<String, Object> co : cooccurrence) {
            ContentKnowledgeEdge edge = new ContentKnowledgeEdge();
            edge.setSource("tag_" + co.get("tag1"));
            edge.setTarget("tag_" + co.get("tag2"));
            edge.setRelation("cooccurrence");
            edge.setWeight(((Number) co.get("cooccurrence")).intValue());
            edges.add(edge);
        }

        graph.setNodes(nodes);
        graph.setEdges(edges);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalTags", tagSet.size());
        statistics.put("totalRelations", edges.size());
        graph.setStatistics(statistics);

        return graph;
    }

    /**
     * 根据标签推荐相关内容（当前用户自己的文章）
     */
    @Override
    public List<Map<String, Object>> getRelatedContentsByTag(Long userId, String tag, Integer limit) {
        log.info("根据标签推荐文章: userId={}, tag={}, limit={}", userId, tag, limit);

        if (userId == null) return new ArrayList<>();

        if (limit == null || limit <= 0) {
            limit = 10;
        }

        // ✅ 只查询当前用户自己的文章
        List<Long> contentIds = contentMapper.selectUserContentIdsByTag(userId, tag);
        if (contentIds.isEmpty()) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<Content> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Content::getId, contentIds)
                .eq(Content::getUserId, userId)
                .eq(Content::getStatus, 1)
                .orderByDesc(Content::getViews)
                .orderByDesc(Content::getCreateTime)
                .last("LIMIT " + limit);

        List<Content> contents = contentMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Content content : contents) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", content.getId());
            item.put("title", content.getTitle());
            item.put("views", content.getViews());
            item.put("likesCount", content.getLikesCount());
            item.put("commentsCount", content.getCommentsCount());
            item.put("createTime", content.getCreateTime());
            result.add(item);
        }

        return result;
    }

    /**
     * 获取用户自己的内容主题分布
     */
    @Override
    public Map<String, Object> getUserContentTopics(Long userId) {
        log.info("获取用户内容主题分布: userId={}", userId);

        if (userId == null) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("userId", null);
            empty.put("totalContents", 0);
            empty.put("topTags", new ArrayList<>());
            empty.put("topCategories", new ArrayList<>());
            return empty;
        }

        LambdaQueryWrapper<Content> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Content::getUserId, userId)
                .eq(Content::getStatus, 1)
                .isNotNull(Content::getTags)
                .ne(Content::getTags, "");

        List<Content> contents = contentMapper.selectList(wrapper);

        Map<String, Integer> tagCount = new HashMap<>();
        for (Content content : contents) {
            if (StrUtil.isNotBlank(content.getTags())) {
                String[] tags = content.getTags().split(",");
                for (String tag : tags) {
                    String trimmedTag = tag.trim();
                    if (!trimmedTag.isEmpty()) {
                        tagCount.put(trimmedTag, tagCount.getOrDefault(trimmedTag, 0) + 1);
                    }
                }
            }
        }

        Map<String, Integer> categoryCount = new HashMap<>();
        for (Content content : contents) {
            if (StrUtil.isNotBlank(content.getCategory())) {
                categoryCount.put(content.getCategory(),
                        categoryCount.getOrDefault(content.getCategory(), 0) + 1);
            }
        }

        List<Map<String, Object>> topTags = tagCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", entry.getKey());
                    item.put("value", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        List<Map<String, Object>> topCategories = categoryCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", entry.getKey());
                    item.put("value", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("totalContents", contents.size());
        result.put("topTags", topTags);
        result.put("topCategories", topCategories);
        result.put("tagDistribution", tagCount);

        return result;
    }

    /**
     * 构建用户统计信息 - 参数类型改为 List<Map<String, Object>>
     */
    private Map<String, Object> buildUserStatistics(List<Map<String, Object>> tags,
                                                    List<Map<String, Object>> categories,
                                                    Long userId) {
        Map<String, Object> statistics = new HashMap<>();

        long totalTagUsage = tags.stream()
                .mapToLong(t -> ((Number) t.get("count")).longValue())
                .sum();
        long totalCategoryUsage = categories.stream()
                .mapToLong(c -> ((Number) c.get("count")).longValue())
                .sum();

        statistics.put("totalTags", tags.size());
        statistics.put("totalTagUsage", totalTagUsage);
        statistics.put("totalCategories", categories.size());
        statistics.put("totalCategoryUsage", totalCategoryUsage);

        // 用户总文章数
        Long totalArticles = contentMapper.selectUserContentCount(userId);
        statistics.put("totalUserArticles", totalArticles != null ? totalArticles : 0L);

        // 最热门的标签
        if (!tags.isEmpty()) {
            String topTag = (String) tags.get(0).get("tag");
            Long topTagCount = ((Number) tags.get(0).get("count")).longValue();
            statistics.put("topTag", topTag);
            statistics.put("topTagCount", topTagCount);
        }

        // 最热门的分类
        if (!categories.isEmpty()) {
            String topCategory = (String) categories.get(0).get("category");
            Long topCategoryCount = ((Number) categories.get(0).get("count")).longValue();
            statistics.put("topCategory", topCategory);
            statistics.put("topCategoryCount", topCategoryCount);
        }

        return statistics;
    }

    private ContentKnowledgeGraph buildEmptyGraph() {
        ContentKnowledgeGraph graph = new ContentKnowledgeGraph();
        graph.setNodes(new ArrayList<>());
        graph.setEdges(new ArrayList<>());

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalTags", 0);
        statistics.put("totalCategories", 0);
        statistics.put("totalUserArticles", 0);
        graph.setStatistics(statistics);

        return graph;
    }
}