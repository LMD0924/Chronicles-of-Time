package org.example.generalservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.commondb.utils.RestBean;
import org.example.generalservice.entity.ScoreRecord;
import org.example.generalservice.service.ScoreRecordService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/*
 * @Author:总会落叶
 * @Date:2026/4/3
 * @Description: 成绩记录控制器
 */
@Slf4j
@RestController
@RequestMapping("api/score")
@RequiredArgsConstructor
public class ScoreRecordController {

    private final ScoreRecordService scoreRecordService;

    /**
     * 添加成绩记录
     */
    @PostMapping("/add")
    public RestBean<String> addScore(@RequestBody ScoreRecord scoreRecord) {
        log.info("========== 添加成绩记录 ==========");
        if (scoreRecordService.addScore(scoreRecord)) {
            return RestBean.success("添加成功");
        }
        return RestBean.fail("添加失败");
    }

    /**
     * 查询学生成绩列表
     */
    @GetMapping("/list/{userId}")
    public RestBean<List<ScoreRecord>> getScoreList(@PathVariable Integer userId) {
        log.info("查询成绩列表: userId={}", userId);
        List<ScoreRecord> list = scoreRecordService.lambdaQuery()
                .eq(ScoreRecord::getUserId, userId)
                .orderByDesc(ScoreRecord::getExamDate)
                .list();
        return RestBean.success(list);
    }

    /**
     * 薄弱科目分析
     */
    @GetMapping("/weak-subject/{userId}")
    public RestBean<List<Map<String, Object>>> getWeakSubject(@PathVariable Integer userId) {
        log.info("========== 薄弱科目分析 ==========");
        List<Map<String, Object>> analysis = scoreRecordService.getWeakSubjectAnalysis(userId);
        return RestBean.success(analysis);
    }

    /**
     * 成绩趋势图数据
     */
    @GetMapping("/trend/{userId}/{subjectName}")
    public RestBean<List<Map<String, Object>>> getScoreTrend(
            @PathVariable Integer userId,
            @PathVariable String subjectName) {
        log.info("成绩趋势查询: userId={}, subjectName={}", userId, subjectName);
        List<Map<String, Object>> trend = scoreRecordService.getScoreTrend(userId, subjectName);
        return RestBean.success(trend);
    }

    /**
     * 总平均分
     */
    @GetMapping("/overall-avg/{userId}")
    public RestBean<BigDecimal> getOverallAvg(@PathVariable Integer userId) {
        log.info("查询总平均分: userId={}", userId);
        BigDecimal avg = scoreRecordService.getOverallAvg(userId);
        return RestBean.success(avg);
    }

    /**
     * 删除成绩记录
     */
    @DeleteMapping("/delete/{id}")
    public RestBean<String> deleteScore(@PathVariable Integer id) {
        log.info("删除成绩记录: id={}", id);
        if (scoreRecordService.removeById(id)) {
            return RestBean.success("删除成功");
        }
        return RestBean.fail("删除失败");
    }
}