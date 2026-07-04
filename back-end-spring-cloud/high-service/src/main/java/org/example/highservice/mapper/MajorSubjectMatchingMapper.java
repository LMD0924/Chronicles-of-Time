/**
 * 文件说明：拾光记微服务后端高中服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
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

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Mapper
public interface MajorSubjectMatchingMapper extends BaseMapper<MajorSubjectMatching> {

    /**
     * 根据专业获取匹配科目
     */
    @Select("SELECT * FROM hs_major_subject_match " +
            "WHERE major_code = #{majorCode} " +
            "ORDER BY importance_level, matching_score DESC")
    List<MajorSubjectMatching> getMatchingSubjectsByMajor(@Param("majorCode") String majorCode);

    /**
     * 根据科目获取匹配的专业
     */
    @Select("SELECT msm.*, mr.major_name, mr.category " +
            "FROM hs_major_subject_match msm " +
            "INNER JOIN gaokao_major_requirement mr ON msm.major_code = mr.major_code " +
            "WHERE msm.subject_id = #{subjectId} " +
            "ORDER BY msm.matching_score DESC")
    List<Map<String, Object>> getMajorsBySubject(@Param("subjectId") Long subjectId);

    /**
     * 获取专业的平均匹配度
     */
    @Select("SELECT major_code, AVG(matching_score) as avg_score " +
            "FROM hs_major_subject_match " +
            "GROUP BY major_code " +
            "ORDER BY avg_score DESC")
    List<Map<String, Object>> getAverageMatchingScore();
}