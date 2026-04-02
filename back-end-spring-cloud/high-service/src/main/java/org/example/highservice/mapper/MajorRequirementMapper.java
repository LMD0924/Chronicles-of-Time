/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 专业选科要求Mapper接口
 */
package org.example.highservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.highservice.entity.MajorRequirement;

import java.util.List;
import java.util.Map;

@Mapper
public interface MajorRequirementMapper extends BaseMapper<MajorRequirement> {

    /**
     * 根据选科组合匹配专业
     */
    @Select("SELECT mr.*, " +
            "AVG(msm.matching_score) as avg_matching_score, " +
            "GROUP_CONCAT(DISTINCT msm.subject_name) as matched_subjects " +
            "FROM major_requirement mr " +
            "INNER JOIN major_subject_matching msm ON mr.major_code = msm.major_code " +
            "WHERE (mr.first_subject_required = '不限' OR mr.first_subject_required = #{firstSubject}) " +
            "AND msm.subject_id IN (#{subject1Id}, #{subject2Id}, #{subject3Id}) " +
            "GROUP BY mr.id " +
            "ORDER BY avg_matching_score DESC " +
            "LIMIT #{limit}")
    List<MajorRequirement> matchMajorByCombination(@Param("firstSubject") String firstSubject,
                                                   @Param("subject1Id") Long subject1Id,
                                                   @Param("subject2Id") Long subject2Id,
                                                   @Param("subject3Id") Long subject3Id,
                                                   @Param("limit") int limit);

    /**
     * 获取热门专业TOP N
     */
    @Select("SELECT major_name, category, COUNT(*) as count, " +
            "GROUP_CONCAT(DISTINCT university_name) as universities " +
            "FROM major_requirement " +
            "GROUP BY major_name, category " +
            "ORDER BY count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getHotMajors(@Param("limit") int limit);

    /**
     * 根据大学层次筛选专业
     */
    @Select("SELECT * FROM major_requirement WHERE university_level = #{level} ORDER BY create_time DESC")
    List<MajorRequirement> getMajorsByUniversityLevel(@Param("level") String level);

    /**
     * 获取专业详情及匹配科目
     */
    @Select("SELECT mr.*, " +
            "GROUP_CONCAT(DISTINCT CONCAT(msm.subject_name, '(', msm.importance_level, ')') ORDER BY msm.importance_level) as subjects_info " +
            "FROM major_requirement mr " +
            "LEFT JOIN major_subject_matching msm ON mr.major_code = msm.major_code " +
            "WHERE mr.major_code = #{majorCode} " +
            "GROUP BY mr.id")
    Map<String, Object> getMajorDetail(@Param("majorCode") String majorCode);

    /**
     * 根据专业类别统计
     */
    @Select("SELECT category, COUNT(*) as count " +
            "FROM major_requirement " +
            "GROUP BY category " +
            "ORDER BY count DESC")
    List<Map<String, Object>> getCategoryStatistics();
}