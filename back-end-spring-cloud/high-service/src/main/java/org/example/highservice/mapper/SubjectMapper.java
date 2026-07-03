/**
 * 文件说明：拾光记微服务后端高中服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 科目基础信息Mapper接口
 */
package org.example.highservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.highservice.entity.Subject;

import java.util.List;
import java.util.Map;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Mapper
public interface SubjectMapper extends BaseMapper<Subject> {

    /**
     * 根据类别获取科目列表
     */
    @Select("SELECT * FROM hs_subject WHERE category = #{category} AND is_active = 1 ORDER BY sort_order")
    List<Subject> getSubjectsByCategory(@Param("category") Integer category);

    /**
     * 获取必考科目
     */
    @Select("SELECT * FROM hs_subject WHERE category = 1 AND is_active = 1 ORDER BY sort_order")
    List<Subject> getRequiredSubjects();

    /**
     * 获取首选科目（物理/历史）
     */
    @Select("SELECT * FROM hs_subject WHERE category = 2 AND is_active = 1 ORDER BY sort_order")
    List<Subject> getFirstSubjects();

    /**
     * 获取再选科目（化学/生物/政治/地理）
     */
    @Select("SELECT * FROM hs_subject WHERE category = 3 AND is_active = 1 ORDER BY sort_order")
    List<Subject> getSecondSubjects();

    /**
     * 获取所有启用的科目
     */
    @Select("SELECT * FROM hs_subject WHERE is_active = 1 ORDER BY category, sort_order")
    List<Subject> getAllActiveSubjects();

    /**
     * 统计各科目选课人数
     */
    @Select("SELECT s.id, s.name, s.category, " +
            "COUNT(DISTINCT CASE WHEN scs.first_subject_id = s.id THEN scs.student_id END) as first_count, " +
            "COUNT(DISTINCT CASE WHEN scs.second_subject_1_id = s.id THEN scs.student_id END) as second_count " +
            "FROM hs_subject s " +
            "LEFT JOIN hs_student_selection scs ON (scs.first_subject_id = s.id OR scs.second_subject_1_id = s.id OR scs.second_subject_2_id = s.id) " +
            "AND scs.is_confirmed = 1 " +
            "WHERE s.is_active = 1 " +
            "GROUP BY s.id, s.name, s.category")
    List<Map<String, Object>> getSubjectSelectionCount();
}