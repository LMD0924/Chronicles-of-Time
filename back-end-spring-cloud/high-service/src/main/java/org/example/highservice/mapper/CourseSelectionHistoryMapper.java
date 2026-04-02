/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 选课变更历史Mapper接口
 */
package org.example.highservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.highservice.entity.CourseSelectionHistory;

import java.util.List;
import java.util.Map;

@Mapper
public interface CourseSelectionHistoryMapper extends BaseMapper<CourseSelectionHistory> {

    /**
     * 获取学生的选课变更历史
     */
    @Select("SELECT * FROM course_selection_history " +
            "WHERE student_id = #{studentId} " +
            "ORDER BY change_time DESC")
    List<CourseSelectionHistory> getHistoryByStudent(@Param("studentId") Long studentId);

    /**
     * 获取选课记录的变更历史
     */
    @Select("SELECT * FROM course_selection_history " +
            "WHERE selection_id = #{selectionId} " +
            "ORDER BY change_time DESC")
    List<CourseSelectionHistory> getHistoryBySelection(@Param("selectionId") Long selectionId);

    /**
     * 获取最近N天的变更统计
     */
    @Select("SELECT DATE(change_time) as date, change_type, COUNT(*) as count " +
            "FROM course_selection_history " +
            "WHERE change_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(change_time), change_type " +
            "ORDER BY date DESC")
    List<Map<String, Object>> getRecentChanges(@Param("days") int days);

    /**
     * 获取待审批的变更记录
     */
    @Select("SELECT * FROM course_selection_history " +
            "WHERE approve_status = 0 " +
            "ORDER BY change_time ASC")
    List<CourseSelectionHistory> getPendingApprovals();

    /**
     * 统计各种变更类型的数量
     */
    @Select("SELECT change_type, COUNT(*) as count " +
            "FROM course_selection_history " +
            "WHERE change_time >= #{startTime} " +
            "GROUP BY change_type")
    List<Map<String, Object>> getChangeTypeStatistics(@Param("startTime") String startTime);

    /**
     * 更新审批状态
     */
    @Update("UPDATE course_selection_history " +
            "SET approve_status = #{status}, " +
            "approver = #{approver}, " +
            "approve_comment = #{comment}, " +
            "update_time = NOW() " +
            "WHERE id = #{id}")
    int updateApproveStatus(@Param("id") Long id,
                            @Param("status") Integer status,
                            @Param("approver") String approver,
                            @Param("comment") String comment);

    /**
     * 获取学生的最后一次变更记录
     */
    @Select("SELECT * FROM course_selection_history " +
            "WHERE student_id = #{studentId} " +
            "ORDER BY change_time DESC LIMIT 1")
    CourseSelectionHistory getLastChange(@Param("studentId") Long studentId);

    /**
     * 获取某时间段内的变更趋势
     */
    @Select("SELECT DATE(change_time) as date, " +
            "SUM(CASE WHEN change_type = '1' THEN 1 ELSE 0 END) as add_count, " +
            "SUM(CASE WHEN change_type = '2' THEN 1 ELSE 0 END) as modify_count, " +
            "SUM(CASE WHEN change_type = '3' THEN 1 ELSE 0 END) as cancel_count " +
            "FROM course_selection_history " +
            "WHERE change_time >= #{startDate} AND change_time <= #{endDate} " +
            "GROUP BY DATE(change_time) " +
            "ORDER BY date")
    List<Map<String, Object>> getChangeTrend(@Param("startDate") String startDate,
                                             @Param("endDate") String endDate);
}