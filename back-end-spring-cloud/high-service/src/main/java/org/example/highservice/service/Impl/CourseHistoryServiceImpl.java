/**
 * 文件说明：拾光记微服务后端高中服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 选课历史记录Service实现类
 */
package org.example.highservice.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.highservice.entity.CourseSelectionHistory;
import org.example.highservice.mapper.CourseSelectionHistoryMapper;
import org.example.highservice.service.CourseHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Service
public class CourseHistoryServiceImpl
        extends ServiceImpl<CourseSelectionHistoryMapper, CourseSelectionHistory>
        implements CourseHistoryService {

    @Autowired
    private CourseSelectionHistoryMapper historyMapper;

    @Override
    public boolean recordChange(CourseSelectionHistory history) {
        history.setCreateTime(LocalDateTime.now());
        history.setChangeTime(LocalDateTime.now());
        if (history.getApproveStatus() == null) {
            history.setApproveStatus(0); // 默认待审批
        }
        return this.save(history);
    }

    @Override
    public List<CourseSelectionHistory> getStudentHistory(Long userId) {
        return historyMapper.getHistoryByStudent(userId);
    }

    @Override
    public List<CourseSelectionHistory> getSelectionHistory(Long selectionId) {
        return historyMapper.getHistoryBySelection(selectionId);
    }

    @Override
    public List<CourseSelectionHistory> getPendingApprovals() {
        return historyMapper.getPendingApprovals();
    }

    @Override
    @Transactional
    public boolean approveChange(Long id, String approver, Integer status, String comment) {
        int result = historyMapper.updateApproveStatus(id, status, approver, comment);
        return result > 0;
    }

    @Override
    public Map<String, Object> getChangeStatistics(int days) {
        Map<String, Object> statistics = new HashMap<>();

        // 获取最近变更统计
        List<Map<String, Object>> recentChanges = historyMapper.getRecentChanges(days);
        statistics.put("recentChanges", recentChanges);

        // 获取总变更数量
        long totalCount = this.count();
        statistics.put("totalCount", totalCount);

        // 获取待审批数量
        long pendingCount = this.count(new LambdaQueryWrapper<CourseSelectionHistory>()
                .eq(CourseSelectionHistory::getApproveStatus, 0));
        statistics.put("pendingCount", pendingCount);

        // 获取已通过数量
        long approvedCount = this.count(new LambdaQueryWrapper<CourseSelectionHistory>()
                .eq(CourseSelectionHistory::getApproveStatus, 1));
        statistics.put("approvedCount", approvedCount);

        // 获取已拒绝数量
        long rejectedCount = this.count(new LambdaQueryWrapper<CourseSelectionHistory>()
                .eq(CourseSelectionHistory::getApproveStatus, 2));
        statistics.put("rejectedCount", rejectedCount);

        return statistics;
    }

    @Override
    public CourseSelectionHistory getLastChange(Long userId) {
        return historyMapper.getLastChange(userId);
    }

    @Override
    public List<Map<String, Object>> getChangeTrend(String startDate, String endDate) {
        return historyMapper.getChangeTrend(startDate, endDate);
    }
}