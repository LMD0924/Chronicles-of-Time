/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 专业与科目匹配度Mapper接口
 */
package org.example.highservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.highservice.entity.MajorSubjectMatching;

import java.util.List;
import java.util.Map;

@Mapper
public interface MajorSubjectMatchingMapper extends BaseMapper<MajorSubjectMatching> {

    /**
     * 根据专业获取匹配科目
     */
    @Select("SELECT * FROM major_subject_matching " +
            "WHERE major_code = #{majorCode} " +
            "ORDER BY importance_level, matching_score DESC")
    List<MajorSubjectMatching> getMatchingSubjectsByMajor(@Param("majorCode") String majorCode);

    /**
     * 根据科目获取匹配的专业
     */
    @Select("SELECT msm.*, mr.major_name, mr.category " +
            "FROM major_subject_matching msm " +
            "INNER JOIN major_requirement mr ON msm.major_code = mr.major_code " +
            "WHERE msm.subject_id = #{subjectId} " +
            "ORDER BY msm.matching_score DESC")
    List<Map<String, Object>> getMajorsBySubject(@Param("subjectId") Long subjectId);

    /**
     * 获取专业的平均匹配度
     */
    @Select("SELECT major_code, AVG(matching_score) as avg_score " +
            "FROM major_subject_matching " +
            "GROUP BY major_code " +
            "ORDER BY avg_score DESC")
    List<Map<String, Object>> getAverageMatchingScore();
}