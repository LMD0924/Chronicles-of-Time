/**
 * 文件说明：拾光记微服务后端大学服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.universityservice.service.paper;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.universityservice.entity.paper.Suggestion;
import org.example.universityservice.vo.paper.SuggestionVO;

import java.util.List;
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */

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