package org.example.usercenter.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.usercenter.entity.ResumeWorkExperience;
import org.example.usercenter.mapper.ResumeWorkExperienceMapper;
import org.example.usercenter.service.ResumeWorkExperienceService;
import org.springframework.stereotype.Service;

/*
 * @Author:总会落叶
 * @Date:2026/3/27
 * @Description: 工作经历Service实现类
 */
@Service
@RequiredArgsConstructor
public class ResumeWorkExperienceServiceImpl extends ServiceImpl<ResumeWorkExperienceMapper, ResumeWorkExperience> implements ResumeWorkExperienceService {

    private final ResumeWorkExperienceMapper resumeWorkExperienceMapper;

    /**
     * 新增工作经历
     */
    @Override
    public Integer addWorkExperience(ResumeWorkExperience resumeWorkExperience) {
        return resumeWorkExperienceMapper.insert(resumeWorkExperience);
    }

}