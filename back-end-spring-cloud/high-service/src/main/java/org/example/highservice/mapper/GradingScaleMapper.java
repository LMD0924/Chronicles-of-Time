/**
 * 文件说明：拾光记微服务后端高中服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 等级赋分表Mapper接口
 */
package org.example.highservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.highservice.entity.GradingScale;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Mapper
public interface GradingScaleMapper extends BaseMapper<GradingScale> {

    /**
     * 根据原始分获取赋分等级
     */
    @Select("SELECT * FROM hs_grading_scale " +
            "WHERE subject_id = #{subjectId} " +
            "AND academic_year = #{academicYear} " +
            "AND raw_score_min <= #{rawScore} " +
            "AND raw_score_max >= #{rawScore} " +
            "AND is_active = 1")
    GradingScale getScaleByRawScore(@Param("subjectId") Long subjectId,
                                    @Param("rawScore") BigDecimal rawScore,
                                    @Param("academicYear") String academicYear);

    /**
     * 获取科目的所有赋分等级配置
     */
    @Select("SELECT * FROM hs_grading_scale " +
            "WHERE subject_id = #{subjectId} " +
            "AND academic_year = #{academicYear} " +
            "AND is_active = 1 " +
            "ORDER BY raw_score_min ASC")
    List<GradingScale> getScalesBySubject(@Param("subjectId") Long subjectId,
                                          @Param("academicYear") String academicYear);

    /**
     * 获取当前有效的赋分等级配置
     */
    @Select("SELECT * FROM hs_grading_scale WHERE is_active = 1 ORDER BY subject_id, raw_score_min")
    List<GradingScale> getActiveScales();

    /**
     * 批量更新赋分等级状态
     */
    @Update("UPDATE hs_grading_scale SET is_active = #{isActive}, updated_at = NOW() " +
            "WHERE academic_year = #{academicYear}")
    int batchUpdateStatusByYear(@Param("academicYear") String academicYear,
                                @Param("isActive") Boolean isActive);

    /**
     * 获取各分数段学生人数统计
     */
    @Select("SELECT subject_id, " +
            "SUM(CASE WHEN raw_score_min <= 90 THEN 1 ELSE 0 END) as level_a_count, " +
            "SUM(CASE WHEN raw_score_min BETWEEN 75 AND 89 THEN 1 ELSE 0 END) as level_b_count, " +
            "SUM(CASE WHEN raw_score_min BETWEEN 60 AND 74 THEN 1 ELSE 0 END) as level_c_count, " +
            "SUM(CASE WHEN raw_score_min < 60 THEN 1 ELSE 0 END) as level_d_count " +
            "FROM hs_grading_scale " +
            "WHERE is_active = 1 " +
            "GROUP BY subject_id")
    List<Map<String, Object>> getScoreDistribution();
}