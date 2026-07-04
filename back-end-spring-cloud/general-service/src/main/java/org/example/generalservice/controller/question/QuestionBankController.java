/**
 * 文件说明：拾光记微服务后端通用内容服务接口控制器源码，负责接口控制器相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.generalservice.controller.question;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.commondb.utils.RestBean;
import org.example.generalservice.dto.question.ExamStartRequest;
import org.example.generalservice.dto.question.ExamSubmitRequest;
import org.example.generalservice.entity.QuestionBank;
import org.example.generalservice.service.question.OnlineExamService;
import org.example.generalservice.service.question.QuestionBankService;
import org.example.generalservice.vo.question.ExamDetailVO;
import org.example.generalservice.vo.question.ExamHistoryVO;
import org.example.generalservice.vo.question.ExamStartVO;
import org.example.generalservice.vo.question.ExamSubmitVO;
import org.example.generalservice.vo.question.KnowledgeGraph;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/*
 * @Author:总会落叶
 * @Date:2026/4/3
 * @Description: 题库控制器
 */
/**
 * 类说明：当前类是接口控制器模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Slf4j
@RestController
@RequestMapping("api/question")
@RequiredArgsConstructor
public class QuestionBankController {

    private final QuestionBankService questionBankService;
    private final OnlineExamService onlineExamService;

    /**
     * 添加题目
     */
    @PostMapping("/add")
    public RestBean<String> addQuestion(@RequestBody QuestionBank questionBank) {
        log.info("========== 添加题目 ==========");
        if (questionBankService.addQuestion(questionBank)) {
            return RestBean.success("添加成功，等待管理员审核");
        }
        return RestBean.fail("添加失败");
    }

    /**
     * 随机获取题目（练习模式）
     */
    @GetMapping("/random")
    public RestBean<List<QuestionBank>> getRandomQuestions(
            @RequestParam(required = false) Long userId,
            @RequestParam String categoryLevel,
            @RequestParam(required = false) String subjectName,
            @RequestParam(required = false) String questionType,
            @RequestParam(required = false) String difficultyLevel,
            @RequestParam(required = false) String knowledgePoint,
            @RequestParam(defaultValue = "15") Integer limit) {
        log.info("随机获取题目: userId={}, categoryLevel={}, subjectName={}, questionType={}, difficultyLevel={}, knowledgePoint={}, limit={}",
                userId, categoryLevel, subjectName, questionType, difficultyLevel, knowledgePoint, limit);
        List<QuestionBank> questions = userId == null
                ? questionBankService.getRandomQuestions(categoryLevel, subjectName, questionType, limit)
                : questionBankService.getRandomQuestions(userId, categoryLevel, subjectName, questionType, difficultyLevel, knowledgePoint, limit);
        return RestBean.success(questions);
    }

    /**
     * 根据分类查询题目
     */
    @GetMapping("/list")
    public RestBean<List<QuestionBank>> getQuestionList(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String categoryLevel,
            @RequestParam(required = false) String subjectName,
            @RequestParam(required = false) String questionType,
            @RequestParam(required = false) String knowledgePoint,
            @RequestParam(required = false) String difficultyLevel,
            @RequestParam(required = false) String auditStatus) {
        log.info("查询题目列表: userId={}, categoryLevel={}, subjectName={}, questionType={}, knowledgePoint={}, difficultyLevel={}, auditStatus={}",
                userId, categoryLevel, subjectName, questionType, knowledgePoint, difficultyLevel, auditStatus);
        List<QuestionBank> list = userId == null
                ? questionBankService.getAuditQuestionList(auditStatus, null, categoryLevel, subjectName, null)
                : questionBankService.getUserQuestionList(userId, categoryLevel, subjectName, questionType, knowledgePoint, difficultyLevel, auditStatus);
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
        Long questionId = Long.valueOf(String.valueOf(requestBody.get("questionId")));
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
    public RestBean<QuestionBank> getQuestionDetail(@PathVariable Long id) {
        log.info("获取题目详情: id={}", id);
        QuestionBank question = questionBankService.getById(id);
        return RestBean.success(question);
    }

    /**
     * 删除题目
     */
    @DeleteMapping("/delete/{id}")
    public RestBean<String> deleteQuestion(@PathVariable Long id) {
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

    @GetMapping("/filters/{userId}")
    public RestBean<Map<String, Object>> getUserFilters(@PathVariable Long userId) {
        log.info("获取用户筛选条件: userId={}", userId);
        return RestBean.success(questionBankService.getFilters(userId));
    }

    /**
     * 后台审核列表
     */
    @GetMapping("/admin/audit-list")
    public RestBean<List<QuestionBank>> getAuditList(
            @RequestParam(required = false, defaultValue = "pending") String auditStatus,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String categoryLevel,
            @RequestParam(required = false) String subjectName,
            @RequestParam(required = false) String keyword) {
        return RestBean.success(questionBankService.getAuditQuestionList(auditStatus, userId, categoryLevel, subjectName, keyword));
    }

    /**
     * 后台审核题目
     */
    @PutMapping("/admin/audit/{id}")
    public RestBean<String> auditQuestion(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String auditStatus = String.valueOf(body.getOrDefault("auditStatus", "approved"));
        String auditRemark = body.get("auditRemark") == null ? null : String.valueOf(body.get("auditRemark"));
        Long auditorId = body.get("auditorId") == null ? null : Long.valueOf(String.valueOf(body.get("auditorId")));
        if (questionBankService.auditQuestion(id, auditStatus, auditRemark, auditorId)) {
            return RestBean.success("审核成功");
        }
        return RestBean.fail("题目不存在");
    }

    /**
     * 开始考试或错题练习
     */
    @PostMapping("/exam/start")
    public RestBean<ExamStartVO> startExam(@RequestBody ExamStartRequest request) {
        try {
            return RestBean.success(onlineExamService.startExam(request));
        } catch (IllegalArgumentException e) {
            return RestBean.fail(400, e.getMessage());
        }
    }

    /**
     * 提交考试或错题练习
     */
    @PostMapping("/exam/submit")
    public RestBean<ExamSubmitVO> submitExam(@RequestBody ExamSubmitRequest request) {
        try {
            return RestBean.success(onlineExamService.submitExam(request));
        } catch (IllegalArgumentException e) {
            return RestBean.fail(400, e.getMessage());
        }
    }

    /**
     * 考试历史
     */
    @GetMapping("/exam/history/{userId}")
    public RestBean<List<ExamHistoryVO>> getExamHistory(
            @PathVariable Long userId,
            @RequestParam(required = false) String mode) {
        return RestBean.success(onlineExamService.getHistory(userId, mode));
    }

    /**
     * 考试详情
     */
    @GetMapping("/exam/detail/{userId}/{sessionId}")
    public RestBean<ExamDetailVO> getExamDetail(@PathVariable Long userId, @PathVariable Long sessionId) {
        try {
            return RestBean.success(onlineExamService.getDetail(userId, sessionId));
        } catch (IllegalArgumentException e) {
            return RestBean.fail(400, e.getMessage());
        }
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
            @RequestParam(required = false) Long userId,
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

    // ========== 知识图谱相关接口 ==========

    /**
     * 获取用户知识图谱
     */
    @GetMapping("/knowledge-graph/{userId}")
    public RestBean<KnowledgeGraph> getUserKnowledgeGraph(
            @PathVariable Long userId,
            @RequestParam(required = false) String categoryLevel,
            @RequestParam(required = false) String subjectName) {
        log.info("获取用户知识图谱: userId={}, categoryLevel={}, subjectName={}", userId, categoryLevel, subjectName);
        KnowledgeGraph graph = questionBankService.getUserKnowledgeGraph(userId, categoryLevel, subjectName);
        return RestBean.success(graph);
    }

    /**
     * 获取知识点掌握热力图
     */
    @GetMapping("/knowledge-heatmap/{userId}")
    public RestBean<Map<String, Object>> getKnowledgeHeatmap(
            @PathVariable Long userId,
            @RequestParam(required = false) String categoryLevel) {
        log.info("获取知识点热力图: userId={}, categoryLevel={}", userId, categoryLevel);
        Map<String, Object> heatmap = questionBankService.getKnowledgeHeatmap(userId, categoryLevel);
        return RestBean.success(heatmap);
    }

    /**
     * 获取学习路径推荐
     */
    @GetMapping("/learning-path/{userId}")
    public RestBean<List<Map<String, Object>>> getLearningPath(
            @PathVariable Long userId,
            @RequestParam(required = false) String categoryLevel) {
        log.info("获取学习路径推荐: userId={}, categoryLevel={}", userId, categoryLevel);
        List<Map<String, Object>> learningPath = questionBankService.getLearningPath(userId, categoryLevel);
        return RestBean.success(learningPath);
    }

    /**
     * 获取知识掌握趋势
     */
    @GetMapping("/knowledge-trend/{userId}")
    public RestBean<Map<String, Object>> getKnowledgeTrend(
            @PathVariable Long userId,
            @RequestParam(required = false) String subjectName,
            @RequestParam(defaultValue = "30") Integer days) {
        log.info("获取知识掌握趋势: userId={}, subjectName={}, days={}", userId, subjectName, days);
        Map<String, Object> trend = questionBankService.getKnowledgeTrend(userId, subjectName, days);
        return RestBean.success(trend);
    }

    /**
     * 获取知识点雷达图数据
     */
    @GetMapping("/knowledge-radar/{userId}")
    public RestBean<Map<String, Object>> getKnowledgeRadar(
            @PathVariable Long userId,
            @RequestParam(required = false) String categoryLevel) {
        log.info("获取知识点雷达图: userId={}, categoryLevel={}", userId, categoryLevel);
        Map<String, Object> radarData = questionBankService.getKnowledgeRadar(userId, categoryLevel);
        return RestBean.success(radarData);
    }
}
