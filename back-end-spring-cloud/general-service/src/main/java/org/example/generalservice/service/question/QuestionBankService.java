package org.example.generalservice.service.question;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.generalservice.entity.QuestionBank;
import org.example.generalservice.vo.question.KnowledgeGraph;

import java.util.List;
import java.util.Map;

/*
 * @Author:总会落叶
 * @Date:2026/4/3
 * @Description: 题库Service
 */
public interface QuestionBankService extends IService<QuestionBank> {

    /**
     * 添加题目
     */
    Boolean addQuestion(QuestionBank questionBank);

    /**
     * 随机获取题目（用于练习）
     */
    List<QuestionBank> getRandomQuestions(String categoryLevel, String subjectName, String questionType, Integer limit);

    /**
     * 获取高频错题
     */
    List<QuestionBank> getHighMistakeRateQuestions(Integer limit);

    /**
     * 记录题目使用（增加使用次数）
     */
    Boolean recordQuestionUse(Integer questionId);

    /**
     * 记录题目答错
     */
    Boolean recordQuestionMistake(Integer questionId);

    /**
     * 按分类获取题目统计
     */
    List<Map<String, Object>> getQuestionStatistics();

    /**
     * 获取筛选条件（科目列表、题型列表）
     */
    Map<String, Object> getFilters();

    /**
     * 批量记录答题结果
     */
    void recordAnswerBatch(List<Map<String, Object>> records);

    /**
     * 获取答题记录
     */
    List<Map<String, Object>> getAnswerRecords(Long userId, String subjectName, String questionType, Integer isCorrect, String knowledgePoint, String startDate, String endDate, Integer pageNum, Integer pageSize);

    // ========== 新增知识图谱相关方法 ==========

    /**
     * 获取用户知识图谱
     * @param userId 用户ID
     * @param categoryLevel 分类层级筛选（可选）
     * @param subjectName 科目筛选（可选）
     * @return 知识图谱数据
     */
    KnowledgeGraph getUserKnowledgeGraph(Long userId, String categoryLevel, String subjectName);

    /**
     * 获取知识点掌握情况矩阵（热力图数据）
     * @param userId 用户ID
     * @param categoryLevel 分类层级筛选（可选）
     * @return 热力图数据
     */
    Map<String, Object> getKnowledgeHeatmap(Long userId, String categoryLevel);

    /**
     * 获取学习路径推荐（基于薄弱知识点）
     * @param userId 用户ID
     * @param categoryLevel 分类层级筛选（可选）
     * @return 学习路径推荐列表
     */
    List<Map<String, Object>> getLearningPath(Long userId, String categoryLevel);

    /**
     * 获取知识掌握趋势（按时间）
     * @param userId 用户ID
     * @param subjectName 科目筛选（可选）
     * @param days 天数（默认30天）
     * @return 趋势数据
     */
    Map<String, Object> getKnowledgeTrend(Long userId, String subjectName, Integer days);

    /**
     * 获取知识点掌握详情（雷达图数据）
     * @param userId 用户ID
     * @param categoryLevel 分类层级筛选（可选）
     * @return 雷达图数据
     */
    Map<String, Object> getKnowledgeRadar(Long userId, String categoryLevel);
}