package org.example.generalservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.generalservice.entity.ScoreRecord;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/*
 * @Author:总会落叶
 * @Date:2026/4/3
 * @Description: 成绩记录Mapper
 */
@Mapper
public interface ScoreRecordMapper extends BaseMapper<ScoreRecord> {

    /**
     * 查询某学生某科目的平均分
     */
    @Select("SELECT AVG(score) FROM score_records WHERE user_id = #{userId} AND subject_name = #{subjectName}")
    BigDecimal getAvgScoreByUserAndSubject(@Param("userId") Integer userId, @Param("subjectName") String subjectName);

    /**
     * 查询某学生各科目平均分（用于薄弱科目分析）
     */
    @Select("SELECT subject_name, AVG(score) as avg_score, COUNT(*) as exam_count " +
            "FROM score_records WHERE user_id = #{userId} GROUP BY subject_name ORDER BY avg_score ASC")
    List<Map<String, Object>> getSubjectAvgRank(@Param("userId") Integer userId);

    /**
     * 查询某学生某科目的成绩趋势（按时间排序）
     */
    @Select("SELECT exam_date, score FROM score_records " +
            "WHERE user_id = #{userId} AND subject_name = #{subjectName} ORDER BY exam_date ASC")
    List<Map<String, Object>> getScoreTrend(@Param("userId") Integer userId, @Param("subjectName") String subjectName);
}