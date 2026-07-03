/**
 * 文件说明：拾光记微服务后端大学服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.universityservice.service.paper.Impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.universityservice.entity.paper.Suggestion;
import org.example.universityservice.mapper.paper.SuggestionMapper;
import org.example.universityservice.service.paper.SuggestionService;
import org.example.universityservice.vo.paper.SuggestionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DS("cot_university")
public class SuggestionServiceImpl extends ServiceImpl<SuggestionMapper, Suggestion> implements SuggestionService {

    private final SuggestionMapper suggestionMapper;

    @Override
    public List<Suggestion> getSuggestionsByPaperId(Long paperId) {
        LambdaQueryWrapper<Suggestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Suggestion::getPaperId, paperId)
                .orderByDesc(Suggestion::getCreatedAt);
        return suggestionMapper.selectList(wrapper);
    }

    @Override
    public boolean addSuggestion(SuggestionVO suggestionVO) {
        Suggestion suggestion = new Suggestion();
        BeanUtils.copyProperties(suggestionVO, suggestion);
        int result = suggestionMapper.insert(suggestion);
        return result > 0;
    }

    @Override
    public boolean deleteSuggestion(Long id, Long paperId) {
        LambdaQueryWrapper<Suggestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Suggestion::getId, id)
                .eq(Suggestion::getPaperId, paperId);
        int result = suggestionMapper.delete(wrapper);
        return result > 0;
    }
}