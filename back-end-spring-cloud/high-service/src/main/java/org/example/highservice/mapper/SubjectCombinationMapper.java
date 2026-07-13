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

    @Select("SELECT sc.id, sc.combination_name AS name, sc.combination_code AS code, sc.description, " +
            "COUNT(scs.id) as selection_count, " +
            "ROUND(AVG(scs.total_score_weighted), 2) as avg_score, " +
            "MAX(scs.total_score_weighted) as max_score, " +
            "MIN(scs.total_score_weighted) as min_score " +
            "FROM hs_subject_combination sc " +
            "LEFT JOIN hs_student_selection scs ON sc.id = scs.combination_id " +
            "AND scs.is_confirmed = 1 AND scs.is_public = 1 " +
            "WHERE sc.status = 1 " +
            "GROUP BY sc.id, sc.combination_name, sc.combination_code, sc.description, sc.major_coverage_rate " +
            "ORDER BY selection_count DESC, sc.major_coverage_rate DESC")
    List<Map<String, Object>> getHotCombinations();

    @Select("SELECT sc.*, s.subject_name as first_subject_name " +
            "FROM hs_subject_combination sc " +
            "INNER JOIN hs_subject s ON sc.first_subject_id = s.id " +
            "WHERE s.subject_name = #{firstSubject} AND sc.status = 1")
    List<SubjectCombination> getCombinationsByFirstSubject(@Param("firstSubject") String firstSubject);

    @Select("SELECT sc.*, " +
            "sc.combination_name AS name, sc.combination_code AS code, " +
            "s1.subject_name as first_subject_name, " +
            "s2.subject_name as second_subject_1_name, " +
            "s3.subject_name as second_subject_2_name " +
            "FROM hs_subject_combination sc " +
            "LEFT JOIN hs_subject s1 ON sc.first_subject_id = s1.id " +
            "LEFT JOIN hs_subject s2 ON sc.second_subject_1_id = s2.id " +
            "LEFT JOIN hs_subject s3 ON sc.second_subject_2_id = s3.id " +
            "WHERE sc.status = 1 " +
            "ORDER BY sc.major_coverage_rate DESC, sc.combination_name")
    List<Map<String, Object>> getAllCombinationsWithDetails();

    @Select("SELECT sc.* FROM hs_subject_combination sc " +
            "WHERE sc.first_subject_id = #{subjectId} " +
            "OR sc.second_subject_1_id = #{subjectId} " +
            "OR sc.second_subject_2_id = #{subjectId}")
    List<SubjectCombination> getCombinationsBySubject(@Param("subjectId") Long subjectId);

    @Select("SELECT sc.id, sc.combination_name AS name, COUNT(scs.id) as student_count " +
            "FROM hs_subject_combination sc " +
            "LEFT JOIN hs_student_selection scs ON sc.id = scs.combination_id " +
            "AND scs.is_confirmed = 1 " +
            "GROUP BY sc.id, sc.combination_name")
    List<Map<String, Object>> getCombinationStudentCount();
}