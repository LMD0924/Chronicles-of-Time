package org.example.generalservice.mapper.question;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.generalservice.entity.MistakeRecord;

import java.util.List;
import java.util.Map;

/*
 * @Author:总会落叶
 * @Date:2026/4/3
 * @Description: 错题记录Mapper
 */
@Mapper
public interface MistakeRecordMapper extends BaseMapper<MistakeRecord> {

    /**
     * 查询未掌握的错题
     */
    @Select("SELECT * FROM mistake_records WHERE user_id = #{userId} AND mastered = FALSE ORDER BY mistake_date DESC")
    List<MistakeRecord> getUnmasteredMistakes(@Param("userId") Integer userId);

    /**
     * 按科目统计错题数量
     */
    @Select("SELECT subject_name, COUNT(*) as mistake_count, " +
            "SUM(CASE WHEN mastered = TRUE THEN 1 ELSE 0 END) as mastered_count " +
            "FROM mistake_records WHERE user_id = #{userId} GROUP BY subject_name")
    List<Map<String, Object>> getMistakeStatistics(@Param("userId") Integer userId);

    /**
     * 标记错题为已掌握
     */
    @Update("UPDATE mistake_records SET mastered = TRUE, last_review_date = CURDATE(), " +
            "updated_at = NOW() WHERE id = #{id}")
    int markAsMastered(@Param("id") Integer id);

    /**
     * 标记错题为未掌握
     */
    @Update("UPDATE mistake_records SET mastered = FALSE, updated_at = NOW() WHERE id = #{id}")
    int markAsUnmastered(@Param("id") Integer id);

    /**
     * 增加复习次数
     */
    @Update("UPDATE mistake_records SET review_count = review_count + 1, " +
            "last_review_date = CURDATE(), updated_at = NOW() WHERE id = #{id}")
    int incrementReviewCount(@Param("id") Integer id);
}