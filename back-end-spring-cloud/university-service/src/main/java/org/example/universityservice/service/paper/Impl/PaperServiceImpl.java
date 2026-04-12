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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@DS("futurestack")
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
        BeanUtils.copyProperties(paperVO, paper);
        paper.setUserId(userId);
        int result = paperMapper.insert(paper);
        return result > 0;
    }

    @Override
    public boolean updatePaper(PaperVO paperVO) {
        Paper existingPaper = paperMapper.selectById(paperVO.getId());
        if (existingPaper == null) {
            return false;
        }
        BeanUtils.copyProperties(paperVO, existingPaper, "id", "userId", "createdAt");
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