/**
 * 文件说明：拾光记微服务后端高中服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 大学专业选科要求Service接口
 */
package org.example.highservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.highservice.entity.MajorRequirement;
import java.util.List;
import java.util.Map;
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */

public interface MajorRequirementService extends IService<MajorRequirement> {

    /**
     * 根据选科组合匹配专业
     */
    List<MajorRequirement> matchMajorByCombination(String firstSubject, Long subject1Id,
                                                   Long subject2Id, Long subject3Id, int limit);

    /**
     * 获取热门专业
     */
    List<Map<String, Object>> getHotMajors(int limit);

    /**
     * 根据大学层次获取专业
     */
    List<MajorRequirement> getMajorsByUniversityLevel(String level);

    /**
     * 获取专业详情
     */
    Map<String, Object> getMajorDetail(String majorCode);

    /**
     * 获取专业类别统计
     */
    Map<String, Object> getCategoryStatistics();

    /**
     * 搜索专业
     */
    List<MajorRequirement> searchMajors(String keyword);
}