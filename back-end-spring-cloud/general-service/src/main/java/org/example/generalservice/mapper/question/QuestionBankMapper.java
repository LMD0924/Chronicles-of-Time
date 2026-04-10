package org.example.generalservice.mapper.question;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.generalservice.entity.QuestionBank;

import java.util.List;
import java.util.Map;

/*
 * @Author:总会落叶
 * @Date:2026/4/3
 * @Description: 题库Mapper
 */

@Mapper
@DS("chroniclesoftime")
public interface QuestionBankMapper extends BaseMapper<QuestionBank> {

    /**
     * 根据分类随机获取题目
     */
    List<QuestionBank> getRandomQuestions(@Param("categoryLevel") String categoryLevel,
                                          @Param("subjectName") String subjectName,
                                          @Param("questionType") String questionType,
                                          @Param("limit") Integer limit);

    /**
     * 更新题目使用统计
     */
    int incrementUseCount(@Param("id") Integer id);

    /**
     * 更新错题统计
     */
    int incrementMistakeCount(@Param("id") Integer id);

    /**
     * 获取高频错题（错误率最高的题目）
     */
    List<QuestionBank> getHighMistakeRateQuestions(@Param("limit") Integer limit);

    /**
     * 获取所有科目列表
     */
    List<String> getDistinctSubjects();

    /**
     * 获取所有题型列表
     */
    List<String> getDistinctQuestionTypes();

    /**
     * 获取所有知识点列表
     */
    List<String> getDistinctKnowledgePoints();

    /**
     * 批量插入答题记录
     */
    void batchInsertAnswerRecords(@Param("records") List<Map<String, Object>> records);

    /**
     * 获取答题记录
     */
    List<Map<String, Object>> getAnswerRecords(@Param("userId") Long userId,
                                               @Param("subjectName") String subjectName,
                                               @Param("questionType") String questionType,
                                               @Param("isCorrect") Integer isCorrect,
                                               @Param("knowledgePoint") String knowledgePoint,
                                               @Param("startDate") String startDate,
                                               @Param("endDate") String endDate,
                                               @Param("offset") Integer offset,
                                               @Param("pageSize") Integer pageSize);
}