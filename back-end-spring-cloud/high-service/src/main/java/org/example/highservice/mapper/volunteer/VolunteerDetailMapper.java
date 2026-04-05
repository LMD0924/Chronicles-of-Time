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
@Mapper
public interface VolunteerDetailMapper extends BaseMapper<VolunteerDetail> {

    /**
     * 获取完整志愿详情（含大学和专业信息）
     */
    List<Map<String, Object>> getFullVolunteerDetail(@Param("volunteerId") Integer volunteerId);

    /**
     * 获取志愿匹配度分析
     */
    Map<String, Object> getMatchingAnalysis(@Param("volunteerDetailId") Integer volunteerDetailId);

    /**
     * 批量插入志愿详情
     */
    int batchInsert(@Param("list") List<VolunteerDetail> list);
}