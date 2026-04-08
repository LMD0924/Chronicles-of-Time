package org.example.generalservice.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.generalservice.entity.Growth;

import java.util.List;
import java.util.Map;
/*
 * @Author:总会落叶
 * @Date:2026/3/31
 * @Description:
 */
/**
 * 高中成长记录 Mapper 接口
 */
@Mapper
@DS("chroniclesoftime")
public interface GrowthMapper extends BaseMapper<Growth> {

    /**
     * 统计各阶段记录数量
     */
    @Select("SELECT stage, COUNT(*) as count FROM growth WHERE user_id = #{userId} GROUP BY stage")
    List<Map<String, Object>> countByStage(@Param("userId") Long userId);

    /**
     * 获取成长趋势数据
     */
    @Select("SELECT DATE_FORMAT(record_date, '%Y-%m') as month, " +
            "AVG(study_hours) as avgStudyHours, " +
            "AVG(stress_level) as avgStressLevel, " +
            "AVG(happiness_level) as avgHappinessLevel " +
            "FROM growth " +
            "WHERE user_id = #{userId} AND record_date >= DATE_SUB(NOW(), INTERVAL 6 MONTH) " +
            "GROUP BY DATE_FORMAT(record_date, '%Y-%m') " +
            "ORDER BY month")
    List<Map<String, Object>> getGrowthTrend(@Param("userId") Long userId);

    /**
     * 获取里程碑记录
     */
    @Select("SELECT * FROM growth WHERE user_id = #{userId} AND is_milestone = 1 ORDER BY record_date DESC")
    List<Growth> getMilestones(@Param("userId") Long userId);
}
