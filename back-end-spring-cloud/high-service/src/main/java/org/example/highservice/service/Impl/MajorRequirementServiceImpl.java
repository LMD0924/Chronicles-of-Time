/**
 * 文件说明：拾光记微服务后端高中服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 大学专业选科要求Service实现类
 */
package org.example.highservice.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.highservice.entity.MajorRequirement;
import org.example.highservice.mapper.MajorRequirementMapper;
import org.example.highservice.service.MajorRequirementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Service
public class MajorRequirementServiceImpl
        extends ServiceImpl<MajorRequirementMapper, MajorRequirement>
        implements MajorRequirementService {

    @Autowired
    private MajorRequirementMapper majorRequirementMapper;

    @Override
    public List<MajorRequirement> matchMajorByCombination(String firstSubject, Long subject1Id,
                                                          Long subject2Id, Long subject3Id, int limit) {
        return majorRequirementMapper.matchMajorByCombination(firstSubject, subject1Id, subject2Id, subject3Id, limit);
    }

    @Override
    public List<Map<String, Object>> getHotMajors(int limit) {
        return majorRequirementMapper.getHotMajors(limit);
    }

    @Override
    public List<MajorRequirement> getMajorsByUniversityLevel(String level) {
        return majorRequirementMapper.getMajorsByUniversityLevel(level);
    }

    @Override
    public Map<String, Object> getMajorDetail(String majorCode) {
        return majorRequirementMapper.getMajorDetail(majorCode);
    }

    @Override
    public Map<String, Object> getCategoryStatistics() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> statistics = majorRequirementMapper.getCategoryStatistics();
        result.put("statistics", statistics);

        // 计算总专业数
        int totalCount = 0;
        for (Map<String, Object> stat : statistics) {
            totalCount += ((Number) stat.getOrDefault("count", 0)).intValue();
        }
        result.put("totalCount", totalCount);

        return result;
    }

    @Override
    public List<MajorRequirement> searchMajors(String keyword) {
        LambdaQueryWrapper<MajorRequirement> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(MajorRequirement::getMajorName, keyword)
                .or()
                .like(MajorRequirement::getMajorCode, keyword)
                .or()
                .like(MajorRequirement::getCategory, keyword);
        return this.list(wrapper);
    }
}