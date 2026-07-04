/**
 * 文件说明：拾光记微服务后端高中服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 学生选课记录Mapper接口（完整版）
 */
package org.example.highservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.highservice.entity.StudentCourseSelection;

import java.util.List;
import java.util.Map;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Mapper
public interface StudentCourseSelectionMapper extends BaseMapper<StudentCourseSelection> {

    /**
     * 统计各组合选课人数
     */
    @Select("SELECT combination_name, COUNT(*) as count, AVG(total_score_weighted) as avg_score " +
            "FROM hs_student_selection " +
            "WHERE is_confirmed = 1 AND is_public = 1 " +
            "GROUP BY combination_name " +
            "ORDER BY count DESC")
    List<Map<String, Object>> getCombinationStatistics();

    /**
     * 统计各首选科目选课人数
     */
    @Select("SELECT first_subject_name, COUNT(*) as count, " +
            "ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM hs_student_selection WHERE is_confirmed = 1), 2) as percentage " +
            "FROM hs_student_selection " +
            "WHERE is_confirmed = 1 AND is_public = 1 " +
            "GROUP BY first_subject_name")
    List<Map<String, Object>> getFirstSubjectStatistics();

    /**
     * 获取年级排名前N的学生
     */
    @Select("SELECT id, student_id, student_name, grade, class_name, combination_name, " +
            "total_score_weighted, grade_rank, first_subject_name, second_subject_1_name, second_subject_2_name " +
            "FROM hs_student_selection " +
            "WHERE is_confirmed = 1 AND is_public = 1 AND grade = #{grade} " +
            "ORDER BY total_score_weighted DESC " +
            "LIMIT #{limit}")
    List<StudentCourseSelection> getTopStudents(@Param("grade") String grade, @Param("limit") int limit);

    /**
     * 获取热门组合排名（基于实际选课数据）
     */
    @Select("SELECT sc.id, sc.name, sc.code, sc.description, " +
            "COUNT(scs.id) as selection_count, " +
            "ROUND(AVG(scs.total_score_weighted), 2) as avg_score " +
            "FROM hs_subject_combination sc " +
            "LEFT JOIN hs_student_selection scs ON sc.id = scs.combination_id " +
            "AND scs.is_confirmed = 1 AND scs.is_public = 1 " +
            "WHERE sc.is_active = 1 " +
            "GROUP BY sc.id, sc.name, sc.code, sc.description " +
            "ORDER BY selection_count DESC, sc.popularity_rank ASC")
    List<Map<String, Object>> getHotCombinations();

    /**
     * 更新公开状态
     */
    @Update("UPDATE hs_student_selection SET is_public = #{isPublic}, updated_at = NOW() WHERE id = #{id}")
    int updatePublicStatus(@Param("id") Long id, @Param("isPublic") Boolean isPublic);

    /**
     * 批量更新公开状态
     */
    @Update("<script>" +
            "UPDATE hs_student_selection SET is_public = #{isPublic}, updated_at = NOW() " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdatePublicStatus(@Param("ids") List<Long> ids, @Param("isPublic") Boolean isPublic);

    /**
     * 获取公开的选课记录
     */
    @Select("SELECT * FROM hs_student_selection WHERE is_public = 1 AND is_confirmed = 1 ORDER BY created_at DESC")
    List<StudentCourseSelection> getPublicSelections();

    /**
     * 按专业方向推荐选课组合
     */
    @Select("SELECT DISTINCT scs.combination_name, scs.combination_id, COUNT(scs.id) as selection_count " +
            "FROM hs_student_selection scs " +
            "INNER JOIN hs_major_subject_match msm ON msm.subject_id IN (scs.first_subject_id, scs.second_subject_1_id, scs.second_subject_2_id) " +
            "WHERE msm.major_name LIKE CONCAT('%', #{majorName}, '%') AND scs.is_confirmed = 1 " +
            "GROUP BY scs.combination_name, scs.combination_id " +
            "ORDER BY selection_count DESC")
    List<Map<String, Object>> recommendByMajor(@Param("majorName") String majorName);

    /**
     * 获取班级成绩统计
     */
    @Select("SELECT class_name, " +
            "COUNT(*) as student_count, " +
            "ROUND(AVG(total_score_weighted), 2) as avg_score, " +
            "MAX(total_score_weighted) as max_score, " +
            "MIN(total_score_weighted) as min_score " +
            "FROM hs_student_selection " +
            "WHERE grade = #{grade} AND is_confirmed = 1 AND is_public = 1 " +
            "GROUP BY class_name " +
            "ORDER BY avg_score DESC")
    List<Map<String, Object>> getClassScoreStatistics(@Param("grade") String grade);

    /**
     * 获取各组合的平均分排名
     */
    @Select("SELECT combination_name, " +
            "COUNT(*) as student_count, " +
            "ROUND(AVG(total_score_weighted), 2) as avg_score, " +
            "ROUND(AVG(chinese_score), 2) as avg_chinese, " +
            "ROUND(AVG(math_score), 2) as avg_math, " +
            "ROUND(AVG(english_score), 2) as avg_english " +
            "FROM hs_student_selection " +
            "WHERE grade = #{grade} AND is_confirmed = 1 AND is_public = 1 " +
            "GROUP BY combination_name " +
            "ORDER BY avg_score DESC")
    List<Map<String, Object>> getCombinationScoreRanking(@Param("grade") String grade);

    /**
     * 根据学生ID获取最新选课记录
     */
    @Select("SELECT * FROM hs_student_selection " +
            "WHERE student_id = #{studentId} " +
            "ORDER BY created_at DESC LIMIT 1")
    StudentCourseSelection getLatestSelection(@Param("studentId") Long studentId);

    /**
     * 获取某组合的详细统计信息
     */
    @Select("SELECT scs.*, s.name as subject_name " +
            "FROM hs_student_selection scs " +
            "INNER JOIN hs_subject s ON scs.first_subject_id = s.id " +
            "WHERE scs.combination_id = #{combinationId} " +
            "AND scs.is_confirmed = 1 AND scs.is_public = 1 " +
            "ORDER BY scs.total_score_weighted DESC")
    List<StudentCourseSelection> getStudentsByCombination(@Param("combinationId") Long combinationId);

    /**
     * 获取选课趋势统计（按月）
     */
    @Select("SELECT DATE_FORMAT(created_at, '%Y-%m') as month, " +
            "COUNT(*) as selection_count " +
            "FROM hs_student_selection " +
            "WHERE is_confirmed = 1 " +
            "GROUP BY DATE_FORMAT(created_at, '%Y-%m') " +
            "ORDER BY month DESC " +
            "LIMIT 12")
    List<Map<String, Object>> getSelectionTrend();
}