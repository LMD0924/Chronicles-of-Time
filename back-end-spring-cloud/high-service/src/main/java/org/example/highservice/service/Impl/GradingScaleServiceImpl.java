/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 等级赋分Service实现类
 */
package org.example.highservice.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.highservice.entity.GradingScale;
import org.example.highservice.mapper.GradingScaleMapper;
import org.example.highservice.service.GradingScaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GradingScaleServiceImpl
        extends ServiceImpl<GradingScaleMapper, GradingScale>
        implements GradingScaleService {

    @Autowired
    private GradingScaleMapper gradingScaleMapper;

    @Override
    public BigDecimal calculateWeightedScore(Long subjectId, BigDecimal rawScore, String academicYear) {
        if (rawScore == null) {
            return null;
        }

        GradingScale scale = gradingScaleMapper.getScaleByRawScore(subjectId, rawScore, academicYear);

        if (scale == null) {
            return rawScore;
        }

        // 线性转换赋分
        BigDecimal range = scale.getRawScoreMax().subtract(scale.getRawScoreMin());
        if (range.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal ratio = rawScore.subtract(scale.getRawScoreMin())
                    .divide(range, 4, RoundingMode.HALF_UP);
            BigDecimal weightedScore = BigDecimal.valueOf(scale.getAssignedScoreMin())
                    .add(ratio.multiply(BigDecimal.valueOf(scale.getAssignedScoreMax() - scale.getAssignedScoreMin())));
            return weightedScore.setScale(2, RoundingMode.HALF_UP);
        }

        return rawScore;
    }

    @Override
    public List<GradingScale> getScalesBySubject(Long subjectId, String academicYear) {
        return gradingScaleMapper.getScalesBySubject(subjectId, academicYear);
    }

    @Override
    public List<GradingScale> getActiveScales() {
        return gradingScaleMapper.getActiveScales();
    }

    @Override
    public boolean batchUpdateStatus(String academicYear, Boolean isActive) {
        int result = gradingScaleMapper.batchUpdateStatusByYear(academicYear, isActive);
        return result > 0;
    }

    @Override
    public Map<String, Object> getScoreDistribution() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> distribution = gradingScaleMapper.getScoreDistribution();
        result.put("distribution", distribution);

        // 计算总人数
        int totalCount = 0;
        for (Map<String, Object> item : distribution) {
            totalCount += ((Number) item.getOrDefault("level_a_count", 0)).intValue();
            totalCount += ((Number) item.getOrDefault("level_b_count", 0)).intValue();
            totalCount += ((Number) item.getOrDefault("level_c_count", 0)).intValue();
            totalCount += ((Number) item.getOrDefault("level_d_count", 0)).intValue();
        }
        result.put("totalCount", totalCount);

        return result;
    }

    @Override
    @Transactional
    public boolean batchImportScales(List<GradingScale> scales) {
        for (GradingScale scale : scales) {
            scale.setCreateTime(LocalDateTime.now());
            scale.setIsActive(true);
            this.save(scale);
        }
        return true;
    }
}