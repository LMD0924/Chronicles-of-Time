/**
 * 文件说明：拾光记微服务后端高中服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 科目基础信息Service实现类
 */
package org.example.highservice.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.highservice.entity.Subject;
import org.example.highservice.mapper.SubjectMapper;
import org.example.highservice.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Service
public class SubjectServiceImpl
        extends ServiceImpl<SubjectMapper, Subject>
        implements SubjectService {

    @Autowired
    private SubjectMapper subjectMapper;

    @Override
    public List<Subject> getAllActiveSubjects() {
        return subjectMapper.getAllActiveSubjects();
    }

    @Override
    public List<Subject> getRequiredSubjects() {
        return subjectMapper.getRequiredSubjects();
    }

    @Override
    public List<Subject> getFirstSubjects() {
        return subjectMapper.getFirstSubjects();
    }

    @Override
    public List<Subject> getSecondSubjects() {
        return subjectMapper.getSecondSubjects();
    }

    @Override
    public List<Subject> getSubjectsByCategory(Integer category) {
        return subjectMapper.getSubjectsByCategory(category);
    }

    @Override
    public Map<String, Object> getSubjectSelectionCount() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> statistics = subjectMapper.getSubjectSelectionCount();
        result.put("statistics", statistics);

        // 计算总选课人次
        int totalFirst = 0;
        int totalSecond = 0;
        for (Map<String, Object> stat : statistics) {
            totalFirst += ((Number) stat.getOrDefault("first_count", 0)).intValue();
            totalSecond += ((Number) stat.getOrDefault("second_count", 0)).intValue();
        }
        result.put("totalFirstCount", totalFirst);
        result.put("totalSecondCount", totalSecond);

        return result;
    }

    @Override
    @Transactional
    public boolean batchUpdateStatus(List<Long> ids, Boolean isActive) {
        for (Long id : ids) {
            Subject subject = this.getById(id);
            if (subject != null) {
                subject.setIsActive(isActive);
                subject.setUpdateTime(LocalDateTime.now());
                this.updateById(subject);
            }
        }
        return true;
    }
}