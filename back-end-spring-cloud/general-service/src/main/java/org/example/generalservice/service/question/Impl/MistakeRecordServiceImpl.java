/**
 * 文件说明：拾光记微服务后端通用内容服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.generalservice.service.question.Impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.generalservice.entity.MistakeRecord;
import org.example.generalservice.mapper.question.MistakeRecordMapper;
import org.example.generalservice.service.question.MistakeRecordService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/*
 * @Author:总会落叶
 * @Date:2026/4/3
 * @Description: 错题记录Service实现
 */
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Slf4j
@Service
@DS("cot_learning")
@RequiredArgsConstructor
public class MistakeRecordServiceImpl extends ServiceImpl<MistakeRecordMapper, MistakeRecord> implements MistakeRecordService {

    private final MistakeRecordMapper mistakeRecordMapper;

    @Override
    public Boolean addMistake(MistakeRecord mistakeRecord) {
        log.info("添加错题: userId={}, subjectName={}, mistakeName={}",
                mistakeRecord.getUserId(), mistakeRecord.getSubjectName(), mistakeRecord.getMistakeName());
        mistakeRecord.setMistakeDate(mistakeRecord.getMistakeDate() == null ? LocalDate.now() : mistakeRecord.getMistakeDate());
        mistakeRecord.setLastMistakeAt(mistakeRecord.getLastMistakeAt() == null ? LocalDateTime.now() : mistakeRecord.getLastMistakeAt());
        mistakeRecord.setMastered(mistakeRecord.getMastered() != null && mistakeRecord.getMastered());
        mistakeRecord.setMistakeCount(mistakeRecord.getMistakeCount() == null ? 1 : mistakeRecord.getMistakeCount());
        mistakeRecord.setReviewCount(mistakeRecord.getReviewCount() == null ? 0 : mistakeRecord.getReviewCount());
        mistakeRecord.setNextReviewDate(mistakeRecord.getNextReviewDate() == null ? LocalDate.now().plusDays(1) : mistakeRecord.getNextReviewDate());
        return save(mistakeRecord);
    }

    @Override
    public List<MistakeRecord> getUnmasteredMistakes(Long userId) {
        log.info("查询未掌握错题: userId={}", userId);
        return mistakeRecordMapper.getUnmasteredMistakes(userId);
    }

    @Override
    public Boolean markAsMastered(Long id) {
        log.info("标记错题为已掌握: id={}", id);
        return mistakeRecordMapper.markAsMastered(id) > 0;
    }

    @Override
    public Boolean markAsUnmastered(Long id) {
        log.info("标记错题为未掌握: id={}", id);
        return mistakeRecordMapper.markAsUnmastered(id) > 0;
    }

    @Override
    public Boolean reviewMistake(Long id) {
        log.info("复习错题: id={}", id);
        return mistakeRecordMapper.incrementReviewCount(id) > 0;
    }

    @Override
    public List<Map<String, Object>> getMistakeStatistics(Long userId) {
        log.info("错题统计: userId={}", userId);
        return mistakeRecordMapper.getMistakeStatistics(userId);
    }

    @Override
    public List<MistakeRecord> getMistakeList(Long userId, String subjectName, Boolean mastered, String knowledgePoint) {
        log.info("查询错题列表: userId={}, subjectName={}, mastered={}, knowledgePoint={}", userId, subjectName, mastered, knowledgePoint);
        LambdaQueryWrapper<MistakeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MistakeRecord::getUserId, userId);
        if (subjectName != null && !subjectName.isEmpty()) {
            wrapper.eq(MistakeRecord::getSubjectName, subjectName);
        }
        if (mastered != null) {
            wrapper.eq(MistakeRecord::getMastered, mastered);
        }
        if (knowledgePoint != null && !knowledgePoint.isEmpty()) {
            wrapper.eq(MistakeRecord::getKnowledgePoint, knowledgePoint);
        }
        wrapper.orderByDesc(MistakeRecord::getLastMistakeAt);
        return list(wrapper);
    }
}
