package org.example.usercenter.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.usercenter.entity.ResumeSocialExperience;
import org.example.usercenter.mapper.ResumeSocialExperienceMapper;
import org.example.usercenter.service.ResumeSocialExperienceService;
import org.springframework.stereotype.Service;

/*
 * @Author:总会落叶
 * @Date:2026/3/27
 * @Description: 社会/校园经历Service实现类
 */
@Service
@RequiredArgsConstructor
public class ResumeSocialExperienceServiceImpl extends ServiceImpl<ResumeSocialExperienceMapper, ResumeSocialExperience> implements ResumeSocialExperienceService {

    private final ResumeSocialExperienceMapper resumeSocialExperienceMapper;

    /**
     * 添加社会/校园经历
     * @param resumeSocialExperience
     * @return
     */
    @Override
    public Integer addSocialExperience(ResumeSocialExperience resumeSocialExperience) {
        return resumeSocialExperienceMapper.insert(resumeSocialExperience);
    }
}