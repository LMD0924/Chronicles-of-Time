/**
 * 文件说明：拾光记微服务后端大学服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.universityservice.service.paper.Impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.universityservice.entity.paper.Paper;
import org.example.universityservice.mapper.paper.PaperMapper;
import org.example.universityservice.service.paper.PaperService;
import org.example.universityservice.vo.paper.PaperVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DS("cot_university")
public class PaperServiceImpl extends ServiceImpl<PaperMapper, Paper> implements PaperService {

    private final PaperMapper paperMapper;

    @Override
    public List<Paper> getPapersByUserId(Long userId) {
        LambdaQueryWrapper<Paper> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Paper::getUserId, userId)
                .orderByDesc(Paper::getUpdatedAt);
        return paperMapper.selectList(wrapper);
    }

    @Override
    public Paper getPaperById(Long id) {
        return paperMapper.selectById(id);
    }

    @Override
    public boolean createPaper(PaperVO paperVO, Long userId) {
        Paper paper = new Paper();
        BeanUtils.copyProperties(paperVO, paper, "id", "userId", "createdAt", "updatedAt");
        LocalDateTime now = LocalDateTime.now();
        paper.setUserId(userId);
        paper.setCreatedAt(now);
        paper.setUpdatedAt(now);
        int result = paperMapper.insert(paper);
        return result > 0;
    }

    @Override
    public boolean updatePaper(PaperVO paperVO) {
        Paper existingPaper = paperMapper.selectById(paperVO.getId());
        if (existingPaper == null) {
            return false;
        }
        BeanUtils.copyProperties(paperVO, existingPaper, "id", "userId", "createdAt", "updatedAt");
        existingPaper.setUpdatedAt(LocalDateTime.now());
        int result = paperMapper.updateById(existingPaper);
        return result > 0;
    }

    @Override
    public boolean deletePaper(Long id, Long userId) {
        LambdaQueryWrapper<Paper> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Paper::getId, id)
                .eq(Paper::getUserId, userId);
        int result = paperMapper.delete(wrapper);
        return result > 0;
    }
}