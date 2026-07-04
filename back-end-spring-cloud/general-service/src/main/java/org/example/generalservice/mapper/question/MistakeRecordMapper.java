/**
 * 文件说明：拾光记微服务后端通用内容服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.generalservice.mapper.question;

import com.baomidou.dynamic.datasource.annotation.DS;
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
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Mapper
@DS("cot_learning")
public interface MistakeRecordMapper extends BaseMapper<MistakeRecord> {

    /**
     * 查询未掌握的错题
     */
    @Select("SELECT * FROM mistake_record WHERE user_id = #{userId} AND mastered = FALSE AND deleted_at IS NULL ORDER BY last_mistake_at DESC")
    List<MistakeRecord> getUnmasteredMistakes(@Param("userId") Long userId);

    /**
     * 按科目统计错题数量
     */
    @Select("SELECT subject_name, COUNT(*) as mistake_count, " +
            "SUM(CASE WHEN mastered = TRUE THEN 1 ELSE 0 END) as mastered_count " +
            "FROM mistake_record WHERE user_id = #{userId} AND deleted_at IS NULL GROUP BY subject_name")
    List<Map<String, Object>> getMistakeStatistics(@Param("userId") Long userId);

    /**
     * 标记错题为已掌握
     */
    @Update("UPDATE mistake_record SET mastered = TRUE, last_review_date = CURDATE(), next_review_date = DATE_ADD(CURDATE(), INTERVAL 7 DAY), " +
            "updated_at = NOW() WHERE id = #{id}")
    int markAsMastered(@Param("id") Long id);

    /**
     * 标记错题为未掌握
     */
    @Update("UPDATE mistake_record SET mastered = FALSE, updated_at = NOW() WHERE id = #{id}")
    int markAsUnmastered(@Param("id") Long id);

    /**
     * 增加复习次数
     */
    @Update("UPDATE mistake_record SET review_count = review_count + 1, last_review_date = CURDATE(), " +
            "next_review_date = DATE_ADD(CURDATE(), INTERVAL 1 DAY), updated_at = NOW() WHERE id = #{id}")
    int incrementReviewCount(@Param("id") Long id);
}
