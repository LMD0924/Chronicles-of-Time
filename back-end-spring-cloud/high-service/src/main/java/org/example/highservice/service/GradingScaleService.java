/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 等级赋分Service接口
 */
package org.example.highservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.highservice.entity.GradingScale;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface GradingScaleService extends IService<GradingScale> {

    /**
     * 根据原始分计算赋分
     */
    BigDecimal calculateWeightedScore(Long subjectId, BigDecimal rawScore, String academicYear);

    /**
     * 获取科目的所有赋分等级配置
     */
    List<GradingScale> getScalesBySubject(Long subjectId, String academicYear);

    /**
     * 获取所有有效的赋分配置
     */
    List<GradingScale> getActiveScales();

    /**
     * 批量更新赋分等级状态
     */
    boolean batchUpdateStatus(String academicYear, Boolean isActive);

    /**
     * 获取分数段分布统计
     */
    Map<String, Object> getScoreDistribution();

    /**
     * 批量导入赋分等级配置
     */
    boolean batchImportScales(List<GradingScale> scales);
}