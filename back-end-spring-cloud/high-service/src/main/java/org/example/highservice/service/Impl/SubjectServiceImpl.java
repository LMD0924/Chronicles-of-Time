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