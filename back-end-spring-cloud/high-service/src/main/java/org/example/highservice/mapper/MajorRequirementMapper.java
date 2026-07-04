/**
 * 文件说明：拾光记微服务后端高中服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
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

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Mapper
public interface MajorRequirementMapper extends BaseMapper<MajorRequirement> {

    /**
     * 根据选科组合匹配专业
     */
    @Select("SELECT mr.*, " +
            "AVG(msm.matching_score) as avg_matching_score, " +
            "GROUP_CONCAT(DISTINCT msm.subject_name) as matched_subjects " +
            "FROM gaokao_major_requirement mr " +
            "INNER JOIN hs_major_subject_match msm ON mr.major_code = msm.major_code " +
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
            "FROM gaokao_major_requirement " +
            "GROUP BY major_name, category " +
            "ORDER BY count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getHotMajors(@Param("limit") int limit);

    /**
     * 根据大学层次筛选专业
     */
    @Select("SELECT * FROM gaokao_major_requirement WHERE university_level = #{level} ORDER BY created_at DESC")
    List<MajorRequirement> getMajorsByUniversityLevel(@Param("level") String level);

    /**
     * 获取专业详情及匹配科目
     */
    @Select("SELECT mr.*, " +
            "GROUP_CONCAT(DISTINCT CONCAT(msm.subject_name, '(', msm.importance_level, ')') ORDER BY msm.importance_level) as subjects_info " +
            "FROM gaokao_major_requirement mr " +
            "LEFT JOIN hs_major_subject_match msm ON mr.major_code = msm.major_code " +
            "WHERE mr.major_code = #{majorCode} " +
            "GROUP BY mr.id")
    Map<String, Object> getMajorDetail(@Param("majorCode") String majorCode);

    /**
     * 根据专业类别统计
     */
    @Select("SELECT category, COUNT(*) as count " +
            "FROM gaokao_major_requirement " +
            "GROUP BY category " +
            "ORDER BY count DESC")
    List<Map<String, Object>> getCategoryStatistics();
}