package org.example.generalservice.service.Impl.question;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.generalservice.entity.QuestionBank;
import org.example.generalservice.mapper.question.QuestionBankMapper;
import org.example.generalservice.service.question.QuestionBankService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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
    public List<Map<String, Object>> getAnswerRecords(Integer userId, String subjectName, String questionType, Integer isCorrect, String knowledgePoint, String startDate, String endDate, Integer pageNum, Integer pageSize) {
        log.info("获取答题记录: userId={}, subjectName={}, questionType={}, isCorrect={}, knowledgePoint={}, startDate={}, endDate={}, pageNum={}, pageSize={}", 
                userId, subjectName, questionType, isCorrect, knowledgePoint, startDate, endDate, pageNum, pageSize);
        int offset = (pageNum - 1) * pageSize;
        return questionBankMapper.getAnswerRecords(userId, subjectName, questionType, isCorrect, knowledgePoint, startDate, endDate, offset, pageSize);
    }
}