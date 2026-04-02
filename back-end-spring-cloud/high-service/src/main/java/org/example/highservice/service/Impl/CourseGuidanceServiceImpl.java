package org.example.highservice.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.highservice.entity.CourseGuidance;
import org.example.highservice.mapper.CourseGuidanceMapper;
import org.example.highservice.service.CourseGuidanceService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseGuidanceServiceImpl
        extends ServiceImpl<CourseGuidanceMapper, CourseGuidance>
        implements CourseGuidanceService {

    @Override
    public CourseGuidance saveOrUpdateByStudent(CourseGuidance guidance) {
        if (guidance.getId() != null) {
            guidance.setUpdateTime(LocalDateTime.now());
            this.updateById(guidance);
            return this.getById(guidance.getId());
        }
        guidance.setCreateTime(LocalDateTime.now());
        guidance.setUpdateTime(LocalDateTime.now());
        this.save(guidance);
        return guidance;
    }

    @Override
    public List<CourseGuidance> listByStudentId(Long studentId) {
        LambdaQueryWrapper<CourseGuidance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseGuidance::getStudentId, studentId)
                .orderByDesc(CourseGuidance::getGuidanceDate)
                .orderByDesc(CourseGuidance::getCreateTime);
        return this.list(wrapper);
    }
}
