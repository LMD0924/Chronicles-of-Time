package org.example.generalservice.service.question.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.generalservice.entity.AnswerRecords;
import org.example.generalservice.entity.QuestionBank;
import org.example.generalservice.mapper.question.AnswerRecordsMapper;
import org.example.generalservice.mapper.question.QuestionBankMapper;
import org.example.generalservice.service.question.QuestionBankService;
import org.example.generalservice.vo.question.KnowledgeEdge;
import org.example.generalservice.vo.question.KnowledgeGraph;
import org.example.generalservice.vo.question.KnowledgeNode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/*
 * @Author:总会落叶
 * @Date:2026/4/3
 * @Description: 题库Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionBankServiceImpl extends ServiceImpl<QuestionBankMapper, QuestionBank> implements QuestionBankService {

    private final QuestionBankMapper questionBankMapper;
    private final AnswerRecordsMapper answerRecordsMapper;

    @Override
    public Boolean addQuestion(QuestionBank questionBank) {
        log.info("添加题目: subjectName={}, questionType={}",
                questionBank.getSubjectName(), questionBank.getQuestionType());
        questionBank.setUseCount(0);
        questionBank.setMistakeCount(0);
        questionBank.setMistakeRate(java.math.BigDecimal.ZERO);
        return save(questionBank);
    }

    @Override
    public List<QuestionBank> getRandomQuestions(String categoryLevel, String subjectName, String questionType, Integer limit) {
        log.info("随机获取题目: categoryLevel={}, subjectName={}, questionType={}, limit={}", categoryLevel, subjectName, questionType, limit);
        return questionBankMapper.getRandomQuestions(categoryLevel, subjectName, questionType, limit);
    }

    @Override
    public List<QuestionBank> getHighMistakeRateQuestions(Integer limit) {
        log.info("获取高频错题: limit={}", limit);
        return questionBankMapper.getHighMistakeRateQuestions(limit);
    }

    @Override
    public Boolean recordQuestionUse(Integer questionId) {
        log.info("记录题目使用: questionId={}", questionId);
        return questionBankMapper.incrementUseCount(questionId) > 0;
    }

    @Override
    public Boolean recordQuestionMistake(Integer questionId) {
        log.info("记录题目答错: questionId={}", questionId);
        return questionBankMapper.incrementMistakeCount(questionId) > 0;
    }

    @Override
    public List<Map<String, Object>> getQuestionStatistics() {
        log.info("获取题目统计");
        // 按分类层级和题型统计
        return null;
    }

    @Override
    public Map<String, Object> getFilters() {
        log.info("获取筛选条件");
        Map<String, Object> result = new java.util.HashMap<>();
        
        // 获取所有科目列表
        List<String> subjects = questionBankMapper.getDistinctSubjects();
        result.put("subjects", subjects);
        
        // 获取所有题型列表
        List<String> questionTypes = questionBankMapper.getDistinctQuestionTypes();
        result.put("questionTypes", questionTypes);
        
        // 获取所有知识点列表
        List<String> knowledgePoints = questionBankMapper.getDistinctKnowledgePoints();
        result.put("knowledgePoints", knowledgePoints);
        
        return result;
    }

    @Override
    public void recordAnswerBatch(List<Map<String, Object>> records) {
        log.info("批量记录答题结果: 共 {} 条", records.size());
        questionBankMapper.batchInsertAnswerRecords(records);
    }

    @Override
    public List<Map<String, Object>> getAnswerRecords(Long userId, String subjectName, String questionType, Integer isCorrect, String knowledgePoint, String startDate, String endDate, Integer pageNum, Integer pageSize) {
        log.info("获取答题记录: userId={}, subjectName={}, questionType={}, isCorrect={}, knowledgePoint={}, startDate={}, endDate={}, pageNum={}, pageSize={}", 
                userId, subjectName, questionType, isCorrect, knowledgePoint, startDate, endDate, pageNum, pageSize);
        int offset = (pageNum - 1) * pageSize;
        return questionBankMapper.getAnswerRecords(userId, subjectName, questionType, isCorrect, knowledgePoint, startDate, endDate, offset, pageSize);
    }


    // ========== 知识图谱相关方法实现 ==========

    @Override
    public KnowledgeGraph getUserKnowledgeGraph(Long userId, String categoryLevel, String subjectName) {
        log.info("构建用户知识图谱: userId={}, categoryLevel={}, subjectName={}", userId, categoryLevel, subjectName);

        // 查询用户的答题记录
        LambdaQueryWrapper<AnswerRecords> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnswerRecords::getUserId, userId);
        if (StringUtils.hasText(categoryLevel)) {
            wrapper.eq(AnswerRecords::getCategoryLevel, categoryLevel);
        }
        if (StringUtils.hasText(subjectName)) {
            wrapper.eq(AnswerRecords::getSubjectName, subjectName);
        }
        wrapper.orderByDesc(AnswerRecords::getCreatedAt);

        List<AnswerRecords> records = answerRecordsMapper.selectList(wrapper);

        if (records.isEmpty()) {
            return buildEmptyGraph();
        }

        // 构建知识图谱
        KnowledgeGraph graph = new KnowledgeGraph();

        // 1. 构建节点
        List<KnowledgeNode> nodes = buildNodes(records);
        graph.setNodes(nodes);

        // 2. 构建关系边
        List<KnowledgeEdge> edges = buildEdges(records);
        graph.setEdges(edges);

        // 3. 构建统计信息
        Map<String, Object> statistics = buildStatistics(records);
        graph.setStatistics(statistics);

        return graph;
    }

    @Override
    public Map<String, Object> getKnowledgeHeatmap(Long userId, String categoryLevel) {
        log.info("获取知识点掌握热力图: userId={}, categoryLevel={}", userId, categoryLevel);

        LambdaQueryWrapper<AnswerRecords> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnswerRecords::getUserId, userId);
        if (StringUtils.hasText(categoryLevel)) {
            wrapper.eq(AnswerRecords::getCategoryLevel, categoryLevel);
        }

        List<AnswerRecords> records = answerRecordsMapper.selectList(wrapper);

        // 按科目分组，再按知识点分组
        Map<String, Map<String, Map<String, Object>>> heatmapData = new LinkedHashMap<>();

        for (AnswerRecords record : records) {
            String subject = record.getSubjectName();
            String knowledgePoint = record.getKnowledgePoint();
            if (knowledgePoint == null || knowledgePoint.isEmpty()) {
                knowledgePoint = "未分类";
            }

            heatmapData.putIfAbsent(subject, new LinkedHashMap<>());
            heatmapData.get(subject).putIfAbsent(knowledgePoint, new HashMap<String, Object>() {{
                put("total", 0);
                put("correct", 0);
                put("wrong", 0);
            }});

            Map<String, Object> stats = heatmapData.get(subject).get(knowledgePoint);
            stats.put("total", (Integer) stats.get("total") + 1);
            if (record.getIsCorrect() == 1) {
                stats.put("correct", (Integer) stats.get("correct") + 1);
            } else {
                stats.put("wrong", (Integer) stats.get("wrong") + 1);
            }
        }

        // 计算正确率
        for (Map<String, Map<String, Object>> subjectMap : heatmapData.values()) {
            for (Map<String, Object> stats : subjectMap.values()) {
                int total = (Integer) stats.get("total");
                int correct = (Integer) stats.get("correct");
                stats.put("correctRate", total > 0 ? (double) correct / total : 0.0);
                stats.put("wrongRate", total > 0 ? 1.0 - (double) correct / total : 0.0);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("heatmapData", heatmapData);
        result.put("totalRecords", records.size());

        // 计算整体掌握率
        if (!records.isEmpty()) {
            long correctCount = records.stream().filter(r -> r.getIsCorrect() == 1).count();
            result.put("overallCorrectRate", (double) correctCount / records.size());
        } else {
            result.put("overallCorrectRate", 0.0);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getLearningPath(Long userId, String categoryLevel) {
        log.info("获取学习路径推荐: userId={}, categoryLevel={}", userId, categoryLevel);

        LambdaQueryWrapper<AnswerRecords> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnswerRecords::getUserId, userId);
        if (StringUtils.hasText(categoryLevel)) {
            wrapper.eq(AnswerRecords::getCategoryLevel, categoryLevel);
        }

        List<AnswerRecords> records = answerRecordsMapper.selectList(wrapper);

        // 统计各知识点的掌握情况
        Map<String, Map<String, Object>> knowledgeStats = new HashMap<>();

        for (AnswerRecords record : records) {
            String subject = record.getSubjectName();
            String knowledgePoint = record.getKnowledgePoint();
            if (knowledgePoint == null || knowledgePoint.isEmpty()) continue;

            String key = subject + "|" + knowledgePoint;
            knowledgeStats.putIfAbsent(key, new HashMap<String, Object>() {{
                put("subject", subject);
                put("knowledgePoint", knowledgePoint);
                put("categoryLevel", record.getCategoryLevel());
                put("total", 0);
                put("correct", 0);
                put("wrong", 0);
                put("lastAnswerDate", record.getAnswerDate());
            }});

            Map<String, Object> stats = knowledgeStats.get(key);
            stats.put("total", (Integer) stats.get("total") + 1);
            if (record.getIsCorrect() == 1) {
                stats.put("correct", (Integer) stats.get("correct") + 1);
            } else {
                stats.put("wrong", (Integer) stats.get("wrong") + 1);
            }
        }

        // 筛选薄弱知识点（正确率低于60%或错误次数超过3次）
        List<Map<String, Object>> weakPoints = new ArrayList<>();
        for (Map<String, Object> stats : knowledgeStats.values()) {
            int total = (Integer) stats.get("total");
            int correct = (Integer) stats.get("correct");
            int wrong = (Integer) stats.get("wrong");
            double correctRate = total > 0 ? (double) correct / total : 0.0;

            if (correctRate < 0.6 || wrong >= 3) {
                stats.put("correctRate", correctRate);
                stats.put("wrongRate", 1.0 - correctRate);
                stats.put("suggestedPriority", calculatePriority(correctRate, wrong, total));
                stats.put("suggestedAction", getSuggestedAction(correctRate, wrong));
                weakPoints.add(stats);
            }
        }

        // 按优先级排序
        weakPoints.sort((a, b) -> Double.compare(
                (Double) b.get("suggestedPriority"),
                (Double) a.get("suggestedPriority")
        ));

        return weakPoints;
    }

    @Override
    public Map<String, Object> getKnowledgeTrend(Long userId, String subjectName, Integer days) {
        log.info("获取知识掌握趋势: userId={}, subjectName={}, days={}", userId, subjectName, days);

        LambdaQueryWrapper<AnswerRecords> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnswerRecords::getUserId, userId);
        if (StringUtils.hasText(subjectName)) {
            wrapper.eq(AnswerRecords::getSubjectName, subjectName);
        }
        wrapper.orderByAsc(AnswerRecords::getAnswerDate);

        List<AnswerRecords> records = answerRecordsMapper.selectList(wrapper);

        // 按日期分组统计
        Map<String, Map<String, Object>> dailyStats = new LinkedHashMap<>();

        for (AnswerRecords record : records) {
            String date = record.getAnswerDate().toString();
            dailyStats.putIfAbsent(date, new HashMap<String, Object>() {{
                put("date", date);
                put("total", 0);
                put("correct", 0);
                put("wrong", 0);
            }});

            Map<String, Object> stats = dailyStats.get(date);
            stats.put("total", (Integer) stats.get("total") + 1);
            if (record.getIsCorrect() == 1) {
                stats.put("correct", (Integer) stats.get("correct") + 1);
            } else {
                stats.put("wrong", (Integer) stats.get("wrong") + 1);
            }
        }

        // 计算每日正确率
        List<Map<String, Object>> trendData = new ArrayList<>();
        for (Map<String, Object> stats : dailyStats.values()) {
            int total = (Integer) stats.get("total");
            int correct = (Integer) stats.get("correct");
            stats.put("correctRate", total > 0 ? (double) correct / total : 0.0);
            trendData.add(stats);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("trendData", trendData);
        result.put("totalDays", dailyStats.size());
        result.put("totalQuestions", records.size());
        result.put("averageCorrectRate", trendData.stream()
                .mapToDouble(d -> (Double) d.get("correctRate"))
                .average()
                .orElse(0.0));

        // 计算趋势（上升/下降）
        if (trendData.size() >= 2) {
            double firstWeekRate = trendData.stream()
                    .limit(Math.min(7, trendData.size()))
                    .mapToDouble(d -> (Double) d.get("correctRate"))
                    .average()
                    .orElse(0.0);
            double lastWeekRate = trendData.stream()
                    .skip(Math.max(0, trendData.size() - 7))
                    .mapToDouble(d -> (Double) d.get("correctRate"))
                    .average()
                    .orElse(0.0);
            result.put("trend", lastWeekRate - firstWeekRate);
            result.put("trendDirection", lastWeekRate >= firstWeekRate ? "up" : "down");
        } else {
            result.put("trend", 0.0);
            result.put("trendDirection", "stable");
        }

        return result;
    }

    @Override
    public Map<String, Object> getKnowledgeRadar(Long userId, String categoryLevel) {
        log.info("获取知识点雷达图数据: userId={}, categoryLevel={}", userId, categoryLevel);

        LambdaQueryWrapper<AnswerRecords> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnswerRecords::getUserId, userId);
        if (StringUtils.hasText(categoryLevel)) {
            wrapper.eq(AnswerRecords::getCategoryLevel, categoryLevel);
        }

        List<AnswerRecords> records = answerRecordsMapper.selectList(wrapper);

        // 按知识点分组统计
        Map<String, Map<String, Object>> knowledgeStats = new HashMap<>();

        for (AnswerRecords record : records) {
            String knowledgePoint = record.getKnowledgePoint();
            if (knowledgePoint == null || knowledgePoint.isEmpty()) continue;

            knowledgeStats.putIfAbsent(knowledgePoint, new HashMap<String, Object>() {{
                put("name", knowledgePoint);
                put("total", 0);
                put("correct", 0);
            }});

            Map<String, Object> stats = knowledgeStats.get(knowledgePoint);
            stats.put("total", (Integer) stats.get("total") + 1);
            if (record.getIsCorrect() == 1) {
                stats.put("correct", (Integer) stats.get("correct") + 1);
            }
        }

        // 构建雷达图数据
        List<String> indicators = new ArrayList<>();
        List<Double> seriesData = new ArrayList<>();

        for (Map<String, Object> stats : knowledgeStats.values()) {
            int total = (Integer) stats.get("total");
            int correct = (Integer) stats.get("correct");
            double correctRate = total > 0 ? (double) correct / total : 0.0;

            indicators.add((String) stats.get("name"));
            seriesData.add(correctRate * 100); // 转换为百分比
        }

        Map<String, Object> result = new HashMap<>();
        result.put("indicators", indicators);
        result.put("seriesData", seriesData);
        result.put("knowledgeCount", knowledgeStats.size());

        return result;
    }

    // ========== 私有辅助方法 ==========

    /**
     * 构建节点
     */
    private List<KnowledgeNode> buildNodes(List<AnswerRecords> records) {
        Map<String, KnowledgeNode> nodeMap = new HashMap<>();

        // 统计各维度的数据
        Map<String, Map<String, Object>> subjectStats = new HashMap<>();
        Map<String, Map<String, Object>> categoryStats = new HashMap<>();
        Map<String, Map<String, Object>> knowledgeStats = new HashMap<>();

        for (AnswerRecords record : records) {
            // 科目节点统计
            String subjectKey = "subject_" + record.getSubjectName();
            subjectStats.putIfAbsent(subjectKey, new HashMap<String, Object>() {{
                put("name", record.getSubjectName());
                put("total", 0);
                put("correct", 0);
            }});
            Map<String, Object> subjectStat = subjectStats.get(subjectKey);
            subjectStat.put("total", (Integer) subjectStat.get("total") + 1);
            if (record.getIsCorrect() == 1) {
                subjectStat.put("correct", (Integer) subjectStat.get("correct") + 1);
            }

            // 分类层级节点统计
            String categoryKey = "category_" + record.getCategoryLevel();
            categoryStats.putIfAbsent(categoryKey, new HashMap<String, Object>() {{
                put("name", record.getCategoryLevel());
                put("total", 0);
                put("correct", 0);
            }});
            Map<String, Object> categoryStat = categoryStats.get(categoryKey);
            categoryStat.put("total", (Integer) categoryStat.get("total") + 1);
            if (record.getIsCorrect() == 1) {
                categoryStat.put("correct", (Integer) categoryStat.get("correct") + 1);
            }

            // 知识点节点统计
            if (record.getKnowledgePoint() != null && !record.getKnowledgePoint().isEmpty()) {
                String knowledgeKey = "knowledge_" + record.getKnowledgePoint();
                knowledgeStats.putIfAbsent(knowledgeKey, new HashMap<String, Object>() {{
                    put("name", record.getKnowledgePoint());
                    put("subject", record.getSubjectName());
                    put("category", record.getCategoryLevel());
                    put("total", 0);
                    put("correct", 0);
                }});
                Map<String, Object> knowledgeStat = knowledgeStats.get(knowledgeKey);
                knowledgeStat.put("total", (Integer) knowledgeStat.get("total") + 1);
                if (record.getIsCorrect() == 1) {
                    knowledgeStat.put("correct", (Integer) knowledgeStat.get("correct") + 1);
                }
            }
        }

        // 构建科目节点
        for (Map.Entry<String, Map<String, Object>> entry : subjectStats.entrySet()) {
            Map<String, Object> stat = entry.getValue();
            int total = (Integer) stat.get("total");
            int correct = (Integer) stat.get("correct");
            KnowledgeNode node = new KnowledgeNode();
            node.setId(entry.getKey());
            node.setName((String) stat.get("name"));
            node.setType("subject");
            node.setTotalCount(total);
            node.setCorrectCount(correct);
            node.setCorrectRate(total > 0 ? (double) correct / total : 0.0);
            node.setWrongCount(total - correct);
            nodeMap.put(entry.getKey(), node);
        }

        // 构建分类节点
        for (Map.Entry<String, Map<String, Object>> entry : categoryStats.entrySet()) {
            Map<String, Object> stat = entry.getValue();
            int total = (Integer) stat.get("total");
            int correct = (Integer) stat.get("correct");
            KnowledgeNode node = new KnowledgeNode();
            node.setId(entry.getKey());
            node.setName((String) stat.get("name"));
            node.setType("category");
            node.setTotalCount(total);
            node.setCorrectCount(correct);
            node.setCorrectRate(total > 0 ? (double) correct / total : 0.0);
            node.setWrongCount(total - correct);
            nodeMap.put(entry.getKey(), node);
        }

        // 构建知识点节点
        for (Map.Entry<String, Map<String, Object>> entry : knowledgeStats.entrySet()) {
            Map<String, Object> stat = entry.getValue();
            int total = (Integer) stat.get("total");
            int correct = (Integer) stat.get("correct");
            KnowledgeNode node = new KnowledgeNode();
            node.setId(entry.getKey());
            node.setName((String) stat.get("name"));
            node.setType("knowledge_point");
            node.setTotalCount(total);
            node.setCorrectCount(correct);
            node.setCorrectRate(total > 0 ? (double) correct / total : 0.0);
            node.setWrongCount(total - correct);

            Map<String, Object> extra = new HashMap<>();
            extra.put("subject", stat.get("subject"));
            extra.put("category", stat.get("category"));
            node.setExtra(extra);
            nodeMap.put(entry.getKey(), node);
        }

        return new ArrayList<>(nodeMap.values());
    }

    /**
     * 构建关系边
     */
    private List<KnowledgeEdge> buildEdges(List<AnswerRecords> records) {
        Map<String, KnowledgeEdge> edgeMap = new HashMap<>();

        // 分析记录中同时出现的知识点关联
        for (AnswerRecords record : records) {
            if (record.getKnowledgePoint() == null || record.getKnowledgePoint().isEmpty()) continue;

            String knowledgeId = "knowledge_" + record.getKnowledgePoint();
            String subjectId = "subject_" + record.getSubjectName();
            String categoryId = "category_" + record.getCategoryLevel();

            // 知识点到科目的边
            String subjectEdgeKey = knowledgeId + "->" + subjectId;
            if (!edgeMap.containsKey(subjectEdgeKey)) {
                KnowledgeEdge edge = new KnowledgeEdge();
                edge.setSource(knowledgeId);
                edge.setTarget(subjectId);
                edge.setRelation("belongs_to");
                edge.setWeight(0);
                edgeMap.put(subjectEdgeKey, edge);
            }
            edgeMap.get(subjectEdgeKey).setWeight(edgeMap.get(subjectEdgeKey).getWeight() + 1);

            // 知识点到分类的边
            String categoryEdgeKey = knowledgeId + "->" + categoryId;
            if (!edgeMap.containsKey(categoryEdgeKey)) {
                KnowledgeEdge edge = new KnowledgeEdge();
                edge.setSource(knowledgeId);
                edge.setTarget(categoryId);
                edge.setRelation("belongs_to");
                edge.setWeight(0);
                edgeMap.put(categoryEdgeKey, edge);
            }
            edgeMap.get(categoryEdgeKey).setWeight(edgeMap.get(categoryEdgeKey).getWeight() + 1);
        }

        return new ArrayList<>(edgeMap.values());
    }

    /**
     * 构建统计信息
     */
    private Map<String, Object> buildStatistics(List<AnswerRecords> records) {
        Map<String, Object> statistics = new HashMap<>();

        int total = records.size();
        int correct = (int) records.stream().filter(r -> r.getIsCorrect() == 1).count();
        int wrong = total - correct;

        statistics.put("totalQuestions", total);
        statistics.put("correctCount", correct);
        statistics.put("wrongCount", wrong);
        statistics.put("overallCorrectRate", total > 0 ? (double) correct / total : 0.0);

        // 按科目统计
        Map<String, Map<String, Object>> subjectStats = new HashMap<>();
        for (AnswerRecords record : records) {
            subjectStats.putIfAbsent(record.getSubjectName(), new HashMap<String, Object>() {{
                put("total", 0);
                put("correct", 0);
            }});
            Map<String, Object> stats = subjectStats.get(record.getSubjectName());
            stats.put("total", (Integer) stats.get("total") + 1);
            if (record.getIsCorrect() == 1) {
                stats.put("correct", (Integer) stats.get("correct") + 1);
            }
        }

        for (Map.Entry<String, Map<String, Object>> entry : subjectStats.entrySet()) {
            Map<String, Object> stats = entry.getValue();
            int subTotal = (Integer) stats.get("total");
            int subCorrect = (Integer) stats.get("correct");
            stats.put("correctRate", subTotal > 0 ? (double) subCorrect / subTotal : 0.0);
            stats.put("wrongCount", subTotal - subCorrect);
        }
        statistics.put("subjectStats", subjectStats);

        // 按分类统计
        Map<String, Map<String, Object>> categoryStats = new HashMap<>();
        for (AnswerRecords record : records) {
            categoryStats.putIfAbsent(record.getCategoryLevel(), new HashMap<String, Object>() {{
                put("total", 0);
                put("correct", 0);
            }});
            Map<String, Object> stats = categoryStats.get(record.getCategoryLevel());
            stats.put("total", (Integer) stats.get("total") + 1);
            if (record.getIsCorrect() == 1) {
                stats.put("correct", (Integer) stats.get("correct") + 1);
            }
        }

        for (Map.Entry<String, Map<String, Object>> entry : categoryStats.entrySet()) {
            Map<String, Object> stats = entry.getValue();
            int catTotal = (Integer) stats.get("total");
            int catCorrect = (Integer) stats.get("correct");
            stats.put("correctRate", catTotal > 0 ? (double) catCorrect / catTotal : 0.0);
        }
        statistics.put("categoryStats", categoryStats);

        return statistics;
    }

    /**
     * 计算推荐优先级
     */
    private double calculatePriority(double correctRate, int wrongCount, int totalCount) {
        // 优先级 = (1 - 正确率) * 0.6 + (错误次数/总次数) * 0.4
        double wrongRate = (double) wrongCount / totalCount;
        return (1 - correctRate) * 0.6 + wrongRate * 0.4;
    }

    /**
     * 获取建议操作
     */
    private String getSuggestedAction(double correctRate, int wrongCount) {
        if (correctRate < 0.3) {
            return "需要重点复习基础知识";
        } else if (correctRate < 0.6) {
            return "加强练习，多做题巩固";
        } else if (wrongCount >= 5) {
            return "虽然正确率尚可，但错误次数较多，建议系统复习";
        } else {
            return "适当练习，保持手感";
        }
    }

    /**
     * 构建空图谱
     */
    private KnowledgeGraph buildEmptyGraph() {
        KnowledgeGraph graph = new KnowledgeGraph();
        graph.setNodes(new ArrayList<>());
        graph.setEdges(new ArrayList<>());

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalQuestions", 0);
        statistics.put("correctCount", 0);
        statistics.put("wrongCount", 0);
        statistics.put("overallCorrectRate", 0.0);
        statistics.put("subjectStats", new HashMap<>());
        statistics.put("categoryStats", new HashMap<>());
        graph.setStatistics(statistics);

        return graph;
    }
}