package org.example.universityservice.service.paper;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.universityservice.entity.paper.Suggestion;
import org.example.universityservice.vo.paper.SuggestionVO;

import java.util.List;

public interface SuggestionService extends IService<Suggestion> {

    /**
     * 根据论文ID获取修改意见列表
     */
    List<Suggestion> getSuggestionsByPaperId(Long paperId);

    /**
     * 添加修改意见
     */
    boolean addSuggestion(SuggestionVO suggestionVO);

    /**
     * 删除修改意见
     */
    boolean deleteSuggestion(Long id, Long paperId);
}