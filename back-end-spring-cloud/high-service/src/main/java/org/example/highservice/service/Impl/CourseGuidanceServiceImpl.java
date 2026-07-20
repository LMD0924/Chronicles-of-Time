/**
 * 文件说明：拾光记微服务后端高中服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.highservice.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.highservice.entity.CourseGuidance;
import org.example.highservice.mapper.CourseGuidanceMapper;
import org.example.highservice.service.CourseGuidanceService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
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
    public List<CourseGuidance> listByUserId(Long userId) {
        LambdaQueryWrapper<CourseGuidance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseGuidance::getUserId, userId)
                .orderByDesc(CourseGuidance::getGuidanceDate)
                .orderByDesc(CourseGuidance::getCreateTime);
        return this.list(wrapper);
    }
}
