package org.example.usercenter.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.usercenter.entity.ResumeProject;
import org.example.usercenter.mapper.ResumeProjectMapper;
import org.example.usercenter.service.ResumeProjectService;
import org.springframework.stereotype.Service;

/*
 * @Author:总会落叶
 * @Date:2026/3/27
 * @Description: 项目经验Service实现类
 */
@Service
@RequiredArgsConstructor
public class ResumeProjectServiceImpl extends ServiceImpl<ResumeProjectMapper, ResumeProject> implements ResumeProjectService {

    private final ResumeProjectMapper resumeProjectMapper;

    /**
     * 新增项目经验
     */
    @Override
    public Integer addProject(ResumeProject resumeProject) {
        return resumeProjectMapper.insert(resumeProject);
    }

}