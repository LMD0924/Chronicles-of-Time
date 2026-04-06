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
public interface MistakeRecordService extends IService<MistakeRecord> {

    /**
     * 添加错题
     */
    Boolean addMistake(MistakeRecord mistakeRecord);

    /**
     * 获取未掌握的错题
     */
    List<MistakeRecord> getUnmasteredMistakes(Integer userId);

    /**
     * 标记错题为已掌握
     */
    Boolean markAsMastered(Integer id);

    /**
     * 标记错题为未掌握
     */
    Boolean markAsUnmastered(Integer id);

    /**
     * 复习错题（增加复习次数）
     */
    Boolean reviewMistake(Integer id);

    /**
     * 按科目统计错题
     */
    List<Map<String, Object>> getMistakeStatistics(Integer userId);

    /**
     * 获取错题本（支持筛选）
     */
    List<MistakeRecord> getMistakeList(Integer userId, String subjectName, Boolean mastered, String knowledgePoint);
}