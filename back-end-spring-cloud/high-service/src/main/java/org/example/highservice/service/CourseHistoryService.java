/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 选课历史记录Service接口
 */
package org.example.highservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.highservice.entity.CourseSelectionHistory;

import java.util.List;
import java.util.Map;

public interface CourseHistoryService extends IService<CourseSelectionHistory> {

    /**
     * 记录选课变更
     */
    boolean recordChange(CourseSelectionHistory history);

    /**
     * 获取学生的变更历史
     */
    List<CourseSelectionHistory> getStudentHistory(Long studentId);

    /**
     * 获取选课记录的变更历史
     */
    List<CourseSelectionHistory> getSelectionHistory(Long selectionId);

    /**
     * 获取待审批列表
     */
    List<CourseSelectionHistory> getPendingApprovals();

    /**
     * 审批变更记录
     */
    boolean approveChange(Long id, String approver, Integer status, String comment);

    /**
     * 获取变更统计
     */
    Map<String, Object> getChangeStatistics(int days);

    /**
     * 获取学生的最后一次变更
     */
    CourseSelectionHistory getLastChange(Long studentId);

    /**
     * 获取变更趋势
     */
    List<Map<String, Object>> getChangeTrend(String startDate, String endDate);
}