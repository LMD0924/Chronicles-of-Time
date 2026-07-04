/**
 * 文件说明：拾光记微服务后端大学服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.universityservice.service.paper;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.universityservice.entity.paper.Paper;
import org.example.universityservice.vo.paper.PaperVO;

import java.util.List;
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */

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