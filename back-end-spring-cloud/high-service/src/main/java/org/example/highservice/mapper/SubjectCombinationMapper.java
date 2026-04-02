/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 学科组合Mapper接口（完整版）
 */
package org.example.highservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.highservice.entity.SubjectCombination;

import java.util.List;
import java.util.Map;

@Mapper
public interface SubjectCombinationMapper extends BaseMapper<SubjectCombination> {

    /**
     * 获取热门组合排名（基于实际选课数据）
     */
    @Select("SELECT sc.id, sc.name, sc.code, sc.description, " +
            "COUNT(scs.id) as selection_count, " +
            "ROUND(AVG(scs.total_score_weighted), 2) as avg_score, " +
            "MAX(scs.total_score_weighted) as max_score, " +
            "MIN(scs.total_score_weighted) as min_score " +
            "FROM subject_combination sc " +
            "LEFT JOIN student_course_selection scs ON sc.id = scs.combination_id " +
            "AND scs.is_confirmed = 1 AND scs.is_public = 1 " +
            "WHERE sc.is_active = 1 " +
            "GROUP BY sc.id, sc.name, sc.code, sc.description " +
            "ORDER BY selection_count DESC, sc.popularity_rank ASC")
    List<Map<String, Object>> getHotCombinations();

    /**
     * 根据首选科目获取组合
     */
    @Select("SELECT sc.*, s.name as first_subject_name " +
            "FROM subject_combination sc " +
            "INNER JOIN subject s ON sc.first_subject_id = s.id " +
            "WHERE s.name = #{firstSubject} AND sc.is_active = 1")
    List<SubjectCombination> getCombinationsByFirstSubject(@Param("firstSubject") String firstSubject);

    /**
     * 获取所有组合及其详细信息
     */
    @Select("SELECT sc.*, " +
            "s1.name as first_subject_name, " +
            "s2.name as second_subject_1_name, " +
            "s3.name as second_subject_2_name " +
            "FROM subject_combination sc " +
            "LEFT JOIN subject s1 ON sc.first_subject_id = s1.id " +
            "LEFT JOIN subject s2 ON sc.second_subject_id_1 = s2.id " +
            "LEFT JOIN subject s3 ON sc.second_subject_id_2 = s3.id " +
            "WHERE sc.is_active = 1 " +
            "ORDER BY sc.popularity_rank")
    List<Map<String, Object>> getAllCombinationsWithDetails();

    /**
     * 根据科目ID查找包含该科目的组合
     */
    @Select("SELECT sc.* FROM subject_combination sc " +
            "WHERE sc.first_subject_id = #{subjectId} " +
            "OR sc.second_subject_id_1 = #{subjectId} " +
            "OR sc.second_subject_id_2 = #{subjectId}")
    List<SubjectCombination> getCombinationsBySubject(@Param("subjectId") Long subjectId);

    /**
     * 获取组合的选课人数统计
     */
    @Select("SELECT sc.id, sc.name, COUNT(scs.id) as student_count " +
            "FROM subject_combination sc " +
            "LEFT JOIN student_course_selection scs ON sc.id = scs.combination_id " +
            "AND scs.is_confirmed = 1 " +
            "GROUP BY sc.id, sc.name")
    List<Map<String, Object>> getCombinationStudentCount();
}