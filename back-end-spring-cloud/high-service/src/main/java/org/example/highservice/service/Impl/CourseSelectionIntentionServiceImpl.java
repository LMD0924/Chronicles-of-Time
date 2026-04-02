package org.example.highservice.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.highservice.entity.CourseSelectionIntention;
import org.example.highservice.mapper.CourseSelectionIntentionMapper;
import org.example.highservice.service.CourseSelectionIntentionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseSelectionIntentionServiceImpl
        extends ServiceImpl<CourseSelectionIntentionMapper, CourseSelectionIntention>
        implements CourseSelectionIntentionService {

    @Override
    public CourseSelectionIntention saveOrUpdateByStudent(CourseSelectionIntention intention) {
        if (intention.getId() != null) {
            intention.setUpdateTime(LocalDateTime.now());
            this.updateById(intention);
            return this.getById(intention.getId());
        }

        LambdaQueryWrapper<CourseSelectionIntention> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseSelectionIntention::getStudentId, intention.getStudentId())
                .orderByDesc(CourseSelectionIntention::getSubmitTime)
                .last("LIMIT 1");
        CourseSelectionIntention latest = this.getOne(wrapper);
        if (latest != null) {
            intention.setId(latest.getId());
            intention.setUpdateTime(LocalDateTime.now());
            this.updateById(intention);
            return this.getById(latest.getId());
        }

        intention.setSubmitTime(LocalDateTime.now());
        intention.setCreateTime(LocalDateTime.now());
        intention.setUpdateTime(LocalDateTime.now());
        this.save(intention);
        return intention;
    }

    @Override
    public List<CourseSelectionIntention> listByStudentId(Long studentId) {
        LambdaQueryWrapper<CourseSelectionIntention> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseSelectionIntention::getStudentId, studentId)
                .orderByDesc(CourseSelectionIntention::getSubmitTime);
        return this.list(wrapper);
    }
}
