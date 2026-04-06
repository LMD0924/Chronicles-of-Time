package org.example.generalservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.generalservice.entity.ScoreRecord;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/*
 * @Author:总会落叶
 * @Date:2026/4/3
 * @Description: 成绩记录Service
 */
public interface ScoreRecordService extends IService<ScoreRecord> {

    /**
     * 添加成绩记录
     */
    Boolean addScore(ScoreRecord scoreRecord);

    /**
     * 获取学生各科目平均分（薄弱科目分析）
     */
    List<Map<String, Object>> getWeakSubjectAnalysis(Integer userId);

    /**
     * 获取某科目成绩趋势
     */
    List<Map<String, Object>> getScoreTrend(Integer userId, String subjectName);

    /**
     * 获取学生总平均分
     */
    BigDecimal getOverallAvg(Integer userId);
}