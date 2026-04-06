package org.example.generalservice.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.generalservice.entity.ScoreRecord;
import org.example.generalservice.mapper.ScoreRecordMapper;
import org.example.generalservice.service.ScoreRecordService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/*
 * @Author:总会落叶
 * @Date:2026/4/3
 * @Description: 成绩记录Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreRecordServiceImpl extends ServiceImpl<ScoreRecordMapper, ScoreRecord> implements ScoreRecordService {

    private final ScoreRecordMapper scoreRecordMapper;

    @Override
    public Boolean addScore(ScoreRecord scoreRecord) {
        log.info("添加成绩记录: userId={}, subjectName={}, score={}",
                scoreRecord.getUserId(), scoreRecord.getSubjectName(), scoreRecord.getScore());
        return save(scoreRecord);
    }

    @Override
    public List<Map<String, Object>> getWeakSubjectAnalysis(Integer userId) {
        log.info("薄弱科目分析: userId={}", userId);
        // 获取各科目平均分，按分数升序排列（最低分在前）
        return scoreRecordMapper.getSubjectAvgRank(userId);
    }

    @Override
    public List<Map<String, Object>> getScoreTrend(Integer userId, String subjectName) {
        log.info("成绩趋势查询: userId={}, subjectName={}", userId, subjectName);
        return scoreRecordMapper.getScoreTrend(userId, subjectName);
    }

    @Override
    public BigDecimal getOverallAvg(Integer userId) {
        log.info("查询总平均分: userId={}", userId);
        List<Map<String, Object>> subjectAvgs = scoreRecordMapper.getSubjectAvgRank(userId);
        if (subjectAvgs == null || subjectAvgs.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> item : subjectAvgs) {
            total = total.add((BigDecimal) item.get("avg_score"));
        }
        return total.divide(BigDecimal.valueOf(subjectAvgs.size()), 2, BigDecimal.ROUND_HALF_UP);
    }
}