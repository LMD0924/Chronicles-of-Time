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