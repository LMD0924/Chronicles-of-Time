package org.example.highservice.mapper.volunteer;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.highservice.entity.volunteer.AdmissionSimulation;
import java.util.List;
import java.util.Map;

/*
 * @Author:总会落叶
 * @Date:2026/4/4
 * @Description: 录取模拟表Mapper
 */
@Mapper
public interface AdmissionSimulationMapper extends BaseMapper<AdmissionSimulation> {

    /**
     * 模拟录取结果
     */
    List<Map<String, Object>> simulateAdmission(@Param("userId") Integer userId,
                                                @Param("volunteerId") Integer volunteerId);

    /**
     * 获取录取概率预测
     */
    Double predictAdmissionProbability(@Param("volunteerDetailId") Integer volunteerDetailId);

    /**
     * 批量更新模拟结果
     */
    int batchUpdateSimulation(@Param("list") List<AdmissionSimulation> list);
}