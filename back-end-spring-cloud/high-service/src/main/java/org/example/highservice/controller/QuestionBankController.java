package org.example.highservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.commondb.utils.RestBean;
import org.example.highservice.entity.QuestionBank;
import org.example.highservice.service.QuestionBankService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/*
 * @Author:总会落叶
 * @Date:2026/4/3
 * @Description: 题库控制器
 */
@Slf4j
@RestController
@RequestMapping("api/question")
@RequiredArgsConstructor
public class QuestionBankController {

    private final QuestionBankService questionBankService;

    /**
     * 添加题目
     */
    @PostMapping("/add")
    public RestBean<String> addQuestion(@RequestBody QuestionBank questionBank) {
        log.info("========== 添加题目 ==========");
        if (questionBankService.addQuestion(questionBank)) {
            return RestBean.success("添加成功");
        }
        return RestBean.fail("添加失败");
    }

    /**
     * 随机获取题目（练习模式）
     */
    @GetMapping("/random")
    public RestBean<List<QuestionBank>> getRandomQuestions(
            @RequestParam String categoryLevel,
            @RequestParam(required = false) String subjectName,
            @RequestParam(required = false) String questionType,
            @RequestParam(defaultValue = "15") Integer limit) {
        log.info("随机获取题目: categoryLevel={}, subjectName={}, questionType={}, limit={}", categoryLevel, subjectName, questionType, limit);
        List<QuestionBank> questions = questionBankService.getRandomQuestions(categoryLevel, subjectName, questionType, limit);
        return RestBean.success(questions);
    }

    /**
     * 根据分类查询题目
     */
    @GetMapping("/list")
    public RestBean<List<QuestionBank>> getQuestionList(
            @RequestParam(required = false) String categoryLevel,
            @RequestParam(required = false) String subjectName,
            @RequestParam(required = false) String questionType) {
        log.info("查询题目列表: categoryLevel={}, subjectName={}, questionType={}", categoryLevel, subjectName, questionType);
        List<QuestionBank> list = questionBankService.lambdaQuery()
                .eq(categoryLevel != null, QuestionBank::getCategoryLevel, categoryLevel)
                .eq(subjectName != null, QuestionBank::getSubjectName, subjectName)
                .eq(questionType != null, QuestionBank::getQuestionType, questionType)
                .orderByDesc(QuestionBank::getCreatedAt)
                .list();
        return RestBean.success(list);
    }

    /**
     * 获取高频错题
     */
    @GetMapping("/high-mistake")
    public RestBean<List<QuestionBank>> getHighMistakeRateQuestions(
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取高频错题: limit={}", limit);
        List<QuestionBank> questions = questionBankService.getHighMistakeRateQuestions(limit);
        return RestBean.success(questions);
    }

    /**
     * 记录答题结果
     */
    @PostMapping("/record")
    public RestBean<String> recordAnswer(@RequestBody Map<String, Object> requestBody) {
        Integer questionId = (Integer) requestBody.get("questionId");
        Boolean isCorrect = (Boolean) requestBody.get("isCorrect");
        log.info("记录答题结果: questionId={}, isCorrect={}", questionId, isCorrect);
        questionBankService.recordQuestionUse(questionId);
        if (!isCorrect) {
            questionBankService.recordQuestionMistake(questionId);
        }
        return RestBean.success("记录成功");
    }

    /**
     * 获取题目详情
     */
    @GetMapping("/detail/{id}")
    public RestBean<QuestionBank> getQuestionDetail(@PathVariable Integer id) {
        log.info("获取题目详情: id={}", id);
        QuestionBank question = questionBankService.getById(id);
        return RestBean.success(question);
    }

    /**
     * 删除题目
     */
    @DeleteMapping("/delete/{id}")
    public RestBean<String> deleteQuestion(@PathVariable Integer id) {
        log.info("删除题目: id={}", id);
        if (questionBankService.removeById(id)) {
            return RestBean.success("删除成功");
        }
        return RestBean.fail("删除失败");
    }

    /**
     * 获取筛选条件（科目列表、题型列表）
     */
    @GetMapping("/filters")
    public RestBean<Map<String, Object>> getFilters() {
        log.info("获取筛选条件");
        Map<String, Object> filters = questionBankService.getFilters();
        return RestBean.success(filters);
    }

    /**
     * 批量记录答题结果
     */
    @PostMapping("/record-batch")
    public RestBean<String> recordAnswerBatch(@RequestBody List<Map<String, Object>> records) {
        log.info("批量记录答题结果: 共 {} 条", records.size());
        questionBankService.recordAnswerBatch(records);
        return RestBean.success("记录成功");
    }

    /**
     * 获取答题记录
     */
    @GetMapping("/answer-records")
    public RestBean<List<Map<String, Object>>> getAnswerRecords(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String subjectName,
            @RequestParam(required = false) String questionType,
            @RequestParam(required = false) Integer isCorrect,
            @RequestParam(required = false) String knowledgePoint,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取答题记录: userId={}, subjectName={}, questionType={}, isCorrect={}, knowledgePoint={}, startDate={}, endDate={}, pageNum={}, pageSize={}", 
                userId, subjectName, questionType, isCorrect, knowledgePoint, startDate, endDate, pageNum, pageSize);
        List<Map<String, Object>> records = questionBankService.getAnswerRecords(userId, subjectName, questionType, isCorrect, knowledgePoint, startDate, endDate, pageNum, pageSize);
        return RestBean.success(records);
    }
}