package org.example.usercenter.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.usercenter.entity.ResumeEducation;
import org.example.usercenter.mapper.ResumeEducationMapper;
import org.example.usercenter.service.ResumeEducationService;
import org.springframework.stereotype.Service;

/*
 * @Author:总会落叶
 * @Date:2026/3/27
 * @Description: 教育经历Service实现类
 */
@Service
@RequiredArgsConstructor
public class ResumeEducationServiceImpl extends ServiceImpl<ResumeEducationMapper, ResumeEducation> implements ResumeEducationService {

    private final ResumeEducationMapper resumeEducationMapper;

    /**
     * 新增教育经历
     */
    @Override
    public Integer addEducation(ResumeEducation resumeEducation) {
        return resumeEducationMapper.insert(resumeEducation);
    }

}