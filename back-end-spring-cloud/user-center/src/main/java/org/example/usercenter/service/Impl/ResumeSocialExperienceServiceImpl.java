/**
 * 文件说明：拾光记微服务后端用户中心业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
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
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
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