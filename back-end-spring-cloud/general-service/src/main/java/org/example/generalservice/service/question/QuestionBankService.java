package org.example.generalservice.service.question;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.generalservice.entity.QuestionBank;

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
    List<Map<String, Object>> getAnswerRecords(Integer userId, String subjectName, String questionType, Integer isCorrect, String knowledgePoint, String startDate, String endDate, Integer pageNum, Integer pageSize);
}