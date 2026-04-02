package org.example.highservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.highservice.entity.HighSchoolGrowth;
import org.example.highservice.dto.HighSchoolGrowthStatsDTO;

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
public interface HighSchoolGrowthMapper extends BaseMapper<HighSchoolGrowth> {

    /**
     * 统计各年级记录数量
     */
    @Select("SELECT grade, COUNT(*) as count FROM high_school_growth WHERE user_id = #{userId} GROUP BY grade")
    List<Map<String, Object>> countByGrade(@Param("userId") Long userId);

    /**
     * 获取成长趋势数据
     */
    @Select("SELECT DATE_FORMAT(record_date, '%Y-%m') as month, " +
            "AVG(study_hours) as avgStudyHours, " +
            "AVG(stress_level) as avgStressLevel, " +
            "AVG(happiness_level) as avgHappinessLevel " +
            "FROM high_school_growth " +
            "WHERE user_id = #{userId} AND record_date >= DATE_SUB(NOW(), INTERVAL 6 MONTH) " +
            "GROUP BY DATE_FORMAT(record_date, '%Y-%m') " +
            "ORDER BY month")
    List<Map<String, Object>> getGrowthTrend(@Param("userId") Long userId);

    /**
     * 获取里程碑记录
     */
    @Select("SELECT * FROM high_school_growth WHERE user_id = #{userId} AND is_milestone = 1 ORDER BY record_date DESC")
    List<HighSchoolGrowth> getMilestones(@Param("userId") Long userId);
}
