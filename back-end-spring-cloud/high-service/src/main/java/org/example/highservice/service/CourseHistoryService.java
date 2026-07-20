/**
 * 文件说明：拾光记微服务后端高中服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
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
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */

public interface CourseHistoryService extends IService<CourseSelectionHistory> {

    /**
     * 记录选课变更
     */
    boolean recordChange(CourseSelectionHistory history);

    /**
     * 获取用户的变更历史
     */
    List<CourseSelectionHistory> getStudentHistory(Long userId);

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
     * 获取用户的最后一次变更
     */
    CourseSelectionHistory getLastChange(Long userId);

    /**
     * 获取变更趋势
     */
    List<Map<String, Object>> getChangeTrend(String startDate, String endDate);
}