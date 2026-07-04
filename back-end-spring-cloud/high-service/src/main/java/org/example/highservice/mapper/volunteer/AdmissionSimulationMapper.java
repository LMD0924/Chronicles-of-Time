/**
 * 文件说明：拾光记微服务后端高中服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
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
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
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