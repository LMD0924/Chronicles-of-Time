/**
 * 文件说明：拾光记微服务后端通用内容服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
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

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Mapper
@DS("cot_learning")
public interface QuestionBankMapper extends BaseMapper<QuestionBank> {

    /**
     * 根据分类随机获取题目
     */
    List<QuestionBank> getRandomQuestions(@Param("categoryLevel") String categoryLevel,
                                          @Param("subjectName") String subjectName,
                                          @Param("questionType") String questionType,
                                          @Param("limit") Integer limit);

    List<QuestionBank> getRandomQuestionsForUser(@Param("userId") Long userId,
                                                 @Param("categoryLevel") String categoryLevel,
                                                 @Param("subjectName") String subjectName,
                                                 @Param("questionType") String questionType,
                                                 @Param("difficultyLevel") String difficultyLevel,
                                                 @Param("knowledgePoint") String knowledgePoint,
                                                 @Param("limit") Integer limit);

    /**
     * 更新题目使用统计
     */
    int incrementUseCount(@Param("id") Long id);

    /**
     * 更新错题统计
     */
    int incrementMistakeCount(@Param("id") Long id);

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

    List<String> getDistinctKnowledgePointsByUser(@Param("userId") Long userId);

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
