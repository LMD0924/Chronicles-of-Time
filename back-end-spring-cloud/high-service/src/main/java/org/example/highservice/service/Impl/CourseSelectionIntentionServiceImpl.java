/**
 * 文件说明：拾光记微服务后端高中服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.highservice.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.highservice.entity.CourseSelectionIntention;
import org.example.highservice.mapper.CourseSelectionIntentionMapper;
import org.example.highservice.service.CourseSelectionIntentionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
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
