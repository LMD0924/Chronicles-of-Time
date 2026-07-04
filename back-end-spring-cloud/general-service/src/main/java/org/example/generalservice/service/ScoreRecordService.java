/**
 * 文件说明：拾光记微服务后端通用内容服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
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
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
public interface ScoreRecordService extends IService<ScoreRecord> {

    /**
     * 添加成绩记录
     */
    Boolean addScore(ScoreRecord scoreRecord);

    /**
     * 获取学生各科目平均分（薄弱科目分析）
     */
    List<Map<String, Object>> getWeakSubjectAnalysis(Long userId);

    /**
     * 获取某科目成绩趋势
     */
    List<Map<String, Object>> getScoreTrend(Long userId, String subjectName);

    /**
     * 获取学生总平均分
     */

    BigDecimal getOverallAvg(Long userId);
}