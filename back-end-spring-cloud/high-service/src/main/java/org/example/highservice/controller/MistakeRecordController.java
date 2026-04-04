package org.example.highservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.commondb.utils.RestBean;
import org.example.highservice.entity.MistakeRecord;
import org.example.highservice.service.MistakeRecordService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/*
 * @Author:总会落叶
 * @Date:2026/4/3
 * @Description: 错题记录控制器
 */
@Slf4j
@RestController
@RequestMapping("api/mistake")
@RequiredArgsConstructor
public class MistakeRecordController {

    private final MistakeRecordService mistakeRecordService;

    /**
     * 添加错题
     */
    @PostMapping("/add")
    public RestBean<String> addMistake(@RequestBody MistakeRecord mistakeRecord) {
        log.info("========== 添加错题 ==========");
        if (mistakeRecordService.addMistake(mistakeRecord)) {
            return RestBean.success("添加成功");
        }
        return RestBean.fail("添加失败");
    }

    /**
     * 获取错题本
     */
    @GetMapping("/list/{userId}")
    public RestBean<List<MistakeRecord>> getMistakeList(
            @PathVariable Integer userId,
            @RequestParam(required = false) String subjectName,
            @RequestParam(required = false) Boolean mastered) {
        log.info("查询错题本: userId={}, subjectName={}, mastered={}", userId, subjectName, mastered);
        List<MistakeRecord> list = mistakeRecordService.getMistakeList(userId, subjectName, mastered);
        return RestBean.success(list);
    }

    /**
     * 获取未掌握的错题
     */
    @GetMapping("/unmastered/{userId}")
    public RestBean<List<MistakeRecord>> getUnmasteredMistakes(@PathVariable Integer userId) {
        log.info("查询未掌握错题: userId={}", userId);
        List<MistakeRecord> list = mistakeRecordService.getUnmasteredMistakes(userId);
        return RestBean.success(list);
    }

    /**
     * 标记错题为已掌握
     */
    @PutMapping("/master/{id}")
    public RestBean<String> markAsMastered(@PathVariable Integer id) {
        log.info("标记错题已掌握: id={}", id);
        if (mistakeRecordService.markAsMastered(id)) {
            return RestBean.success("标记成功");
        }
        return RestBean.fail("标记失败");
    }

    /**
     * 标记错题为未掌握
     */
    @PutMapping("/unmaster/{id}")
    public RestBean<String> markAsUnmastered(@PathVariable Integer id) {
        log.info("标记错题未掌握: id={}", id);
        if (mistakeRecordService.markAsUnmastered(id)) {
            return RestBean.success("标记成功");
        }
        return RestBean.fail("标记失败");
    }

    /**
     * 复习错题
     */
    @PutMapping("/review/{id}")
    public RestBean<String> reviewMistake(@PathVariable Integer id) {
        log.info("复习错题: id={}", id);
        if (mistakeRecordService.reviewMistake(id)) {
            return RestBean.success("复习记录已更新");
        }
        return RestBean.fail("更新失败");
    }

    /**
     * 错题统计（按科目）
     */
    @GetMapping("/statistics/{userId}")
    public RestBean<List<Map<String, Object>>> getMistakeStatistics(@PathVariable Integer userId) {
        log.info("错题统计: userId={}", userId);
        List<Map<String, Object>> statistics = mistakeRecordService.getMistakeStatistics(userId);
        return RestBean.success(statistics);
    }

    /**
     * 删除错题
     */
    @DeleteMapping("/delete/{id}")
    public RestBean<String> deleteMistake(@PathVariable Integer id) {
        log.info("删除错题: id={}", id);
        if (mistakeRecordService.removeById(id)) {
            return RestBean.success("删除成功");
        }
        return RestBean.fail("删除失败");
    }
}