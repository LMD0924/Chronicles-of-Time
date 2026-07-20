/**
 * 文件说明：拾光记微服务后端高中服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.highservice.mapper.volunteer;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.highservice.entity.volunteer.VolunteerDetail;
import java.util.List;
import java.util.Map;

/*
 * @Author:总会落叶
 * @Date:2026/4/4
 * @Description: 志愿详情表Mapper
 */
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Mapper
public interface VolunteerDetailMapper extends BaseMapper<VolunteerDetail> {

    /**
     * 获取完整志愿详情（含大学和专业信息）
     */
    List<Map<String, Object>> getFullVolunteerDetail(@Param("volunteerId") Long volunteerId);

    /**
     * 获取志愿匹配度分析
     */
    Map<String, Object> getMatchingAnalysis(@Param("volunteerDetailId") Long volunteerDetailId);

    /**
     * 批量插入志愿详情
     */
    int batchInsert(@Param("list") List<VolunteerDetail> list);
}