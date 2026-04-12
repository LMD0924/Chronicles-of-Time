package org.example.universityservice.service.paper;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.universityservice.entity.paper.Paper;
import org.example.universityservice.vo.paper.PaperVO;

import java.util.List;

public interface PaperService extends IService<Paper> {

    /**
     * 根据用户ID获取论文列表
     */
    List<Paper> getPapersByUserId(Long userId);

    /**
     * 根据ID获取论文详情
     */
    Paper getPaperById(Long id);

    /**
     * 创建论文
     */
    boolean createPaper(PaperVO paperVO, Long userId);

    /**
     * 更新论文
     */
    boolean updatePaper(PaperVO paperVO);

    /**
     * 删除论文
     */
    boolean deletePaper(Long id, Long userId);
}