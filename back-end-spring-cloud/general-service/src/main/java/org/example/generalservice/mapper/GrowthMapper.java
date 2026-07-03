/**
 * 文件说明：拾光记微服务后端通用内容服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.generalservice.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.generalservice.entity.Growth;

import java.util.List;
import java.util.Map;
/*
 * @Author:鎬讳細钀藉彾
 * @Date:2026/3/31
 * @Description:
 */
/**
 * 楂樹腑鎴愰暱璁板綍 Mapper 鎺ュ彛
 */
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Mapper
@DS("cot_content")
public interface GrowthMapper extends BaseMapper<Growth> {

    /**
     * 缁熻鍚勯樁娈佃褰曟暟閲?
     */
    @Select("SELECT stage, COUNT(*) as count FROM growth_record WHERE user_id = #{userId} GROUP BY stage")
    List<Map<String, Object>> countByStage(@Param("userId") Long userId);

    /**
     * 鑾峰彇鎴愰暱瓒嬪娍鏁版嵁
     */
    @Select("SELECT DATE_FORMAT(record_date, '%Y-%m') as month, " +
            "AVG(study_hours) as avgStudyHours, " +
            "AVG(stress_level) as avgStressLevel, " +
            "AVG(happiness_level) as avgHappinessLevel " +
            "FROM growth_record " +
            "WHERE user_id = #{userId} AND record_date >= DATE_SUB(NOW(), INTERVAL 6 MONTH) " +
            "GROUP BY DATE_FORMAT(record_date, '%Y-%m') " +
            "ORDER BY month")
    List<Map<String, Object>> getGrowthTrend(@Param("userId") Long userId);

    /**
     * 鑾峰彇閲岀▼纰戣褰?
     */
    @Select("SELECT * FROM growth_record WHERE user_id = #{userId} AND is_milestone = 1 ORDER BY record_date DESC")
    List<Growth> getMilestones(@Param("userId") Long userId);
}

