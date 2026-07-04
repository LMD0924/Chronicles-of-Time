/**
 * 文件说明：拾光记微服务后端通用内容服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.generalservice.service.question;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.generalservice.entity.MistakeRecord;


import java.util.List;
import java.util.Map;

/*
 * @Author:总会落叶
 * @Date:2026/4/3
 * @Description: 错题记录Service
 */
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
public interface MistakeRecordService extends IService<MistakeRecord> {

    /**
     * 添加错题
     */
    Boolean addMistake(MistakeRecord mistakeRecord);

    /**
     * 获取未掌握的错题
     */
    List<MistakeRecord> getUnmasteredMistakes(Long userId);

    /**
     * 标记错题为已掌握
     */
    Boolean markAsMastered(Long id);

    /**
     * 标记错题为未掌握
     */
    Boolean markAsUnmastered(Long id);

    /**
     * 复习错题（增加复习次数）
     */
    Boolean reviewMistake(Long id);

    /**
     * 按科目统计错题
     */
    List<Map<String, Object>> getMistakeStatistics(Long userId);

    /**
     * 获取错题本（支持筛选）
     */
    List<MistakeRecord> getMistakeList(Long userId, String subjectName, Boolean mastered, String knowledgePoint);
}
