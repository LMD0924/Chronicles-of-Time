/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 选课历史记录控制器
 */
package org.example.highservice.controller;

import org.example.commondb.utils.RestBean;
import org.example.highservice.entity.CourseSelectionHistory;
import org.example.highservice.service.CourseHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/history")
public class CourseHistoryController {

    @Autowired
    private CourseHistoryService historyService;

    /**
     * 获取学生的变更历史
     */
    @GetMapping("/student/{studentId}")
    public RestBean<?> getStudentHistory(@PathVariable Long studentId) {
        List<CourseSelectionHistory> histories = historyService.getStudentHistory(studentId);
        return RestBean.success(histories);
    }

    /**
     * 获取选课记录的变更历史
     */
    @GetMapping("/selection/{selectionId}")
    public RestBean<?> getSelectionHistory(@PathVariable Long selectionId) {
        List<CourseSelectionHistory> histories = historyService.getSelectionHistory(selectionId);
        return RestBean.success(histories);
    }

    @GetMapping("/detail/{id}")
    public RestBean<?> getHistoryDetail(@PathVariable Long id) {
        CourseSelectionHistory history = historyService.getById(id);
        return RestBean.success(history);
    }

    /**
     * 获取待审批列表
     */
    @GetMapping("/pending")
    public RestBean<?> getPendingApprovals() {
        List<CourseSelectionHistory> pendingList = historyService.getPendingApprovals();
        return RestBean.success(pendingList);
    }

    /**
     * 审批变更记录
     */
    @PutMapping("/approve/{id}")
    public RestBean<?> approveChange(@PathVariable Long id,
                                     @RequestParam String approver,
                                     @RequestParam Integer status,
                                     @RequestParam(required = false) String comment) {
        boolean success = historyService.approveChange(id, approver, status, comment);
        return success ? RestBean.success("审批成功") : RestBean.fail("审批失败");
    }

    /**
     * 获取变更统计
     */
    @GetMapping("/statistics")
    public RestBean<?> getChangeStatistics(@RequestParam(defaultValue = "7") int days) {
        Map<String, Object> statistics = historyService.getChangeStatistics(days);
        return RestBean.success(statistics);
    }

    /**
     * 获取学生最后一次变更
     */
    @GetMapping("/last/{studentId}")
    public RestBean<?> getLastChange(@PathVariable Long studentId) {
        CourseSelectionHistory lastChange = historyService.getLastChange(studentId);
        return RestBean.success(lastChange);
    }
}