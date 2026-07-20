package org.example.highservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.highservice.entity.Subject;

import java.util.List;
import java.util.Map;

@Mapper
public interface SubjectMapper extends BaseMapper<Subject> {

    @Select("SELECT * FROM hs_subject WHERE subject_type = #{category} AND status = 1 ORDER BY sort_order")
    List<Subject> getSubjectsByCategory(@Param("category") Integer category);

    @Select("SELECT * FROM hs_subject WHERE subject_type = 3 AND status = 1 ORDER BY sort_order")
    List<Subject> getRequiredSubjects();

    @Select("SELECT * FROM hs_subject WHERE subject_type = 1 AND status = 1 ORDER BY sort_order")
    List<Subject> getFirstSubjects();

    @Select("SELECT * FROM hs_subject WHERE subject_type = 2 AND status = 1 ORDER BY sort_order")
    List<Subject> getSecondSubjects();

    @Select("SELECT * FROM hs_subject WHERE status = 1 ORDER BY subject_type, sort_order")
    List<Subject> getAllActiveSubjects();

    @Select("SELECT s.id, s.subject_name AS name, s.subject_type AS category, " +
            "COUNT(DISTINCT CASE WHEN scs.first_subject_id = s.id THEN scs.user_id END) as first_count, " +
            "COUNT(DISTINCT CASE WHEN scs.second_subject_1_id = s.id OR scs.second_subject_2_id = s.id THEN scs.user_id END) as second_count " +
            "FROM hs_subject s " +
            "LEFT JOIN hs_student_selection scs ON (scs.first_subject_id = s.id OR scs.second_subject_1_id = s.id OR scs.second_subject_2_id = s.id) " +
            "AND scs.is_confirmed = 1 " +
            "WHERE s.status = 1 " +
            "GROUP BY s.id, s.subject_name, s.subject_type")
    List<Map<String, Object>> getSubjectSelectionCount();
}