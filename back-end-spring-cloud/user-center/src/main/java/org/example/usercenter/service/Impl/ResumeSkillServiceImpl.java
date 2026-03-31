package org.example.usercenter.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.usercenter.entity.ResumeSkill;
import org.example.usercenter.mapper.ResumeSkillMapper;
import org.example.usercenter.service.ResumeSkillService;
import org.springframework.stereotype.Service;

/*
 * @Author:总会落叶
 * @Date:2026/3/27
 * @Description: 技能特长Service实现类
 */
@Service
@RequiredArgsConstructor
public class ResumeSkillServiceImpl extends ServiceImpl<ResumeSkillMapper, ResumeSkill> implements ResumeSkillService {

    private final  ResumeSkillMapper resumeSkillMapper;

    /**
     * 新增技能特长
     * @param resumeSkill
     * @return
     */
    @Override
    public Integer addSkill(ResumeSkill resumeSkill) {
        return resumeSkillMapper.insert(resumeSkill);
    }
}