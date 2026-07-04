/**
 * 文件说明：拾光记微服务后端高中服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.highservice.service.Impl;

import org.example.highservice.entity.SubjectCombination;
import org.example.highservice.mapper.SubjectCombinationMapper;
import org.example.highservice.service.SubjectCombinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Service
public class SubjectCombinationServiceImpl implements SubjectCombinationService {

    @Autowired
    private SubjectCombinationMapper subjectCombinationMapper;

    @Override
    public List<Map<String, Object>> getHotCombinations() {
        return subjectCombinationMapper.getHotCombinations();
    }

    @Override
    public List<SubjectCombination> getCombinationsByFirstSubject(String firstSubject) {
        return subjectCombinationMapper.getCombinationsByFirstSubject(firstSubject);
    }

    @Override
    public List<Map<String, Object>> getAllCombinationsWithDetails() {
        return subjectCombinationMapper.getAllCombinationsWithDetails();
    }

    @Override
    public List<SubjectCombination> getCombinationsBySubject(Long subjectId) {
        return subjectCombinationMapper.getCombinationsBySubject(subjectId);
    }

    @Override
    public List<Map<String, Object>> getCombinationStudentCount() {
        return subjectCombinationMapper.getCombinationStudentCount();
    }
}