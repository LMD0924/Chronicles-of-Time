package org.example.highservice.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.highservice.entity.MistakeRecord;
import org.example.highservice.mapper.MistakeRecordMapper;
import org.example.highservice.service.MistakeRecordService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/*
 * @Author:总会落叶
 * @Date:2026/4/3
 * @Description: 错题记录Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MistakeRecordServiceImpl extends ServiceImpl<MistakeRecordMapper, MistakeRecord> implements MistakeRecordService {

    private final MistakeRecordMapper mistakeRecordMapper;

    @Override
    public Boolean addMistake(MistakeRecord mistakeRecord) {
        log.info("添加错题: userId={}, subjectName={}, mistakeName={}",
                mistakeRecord.getUserId(), mistakeRecord.getSubjectName(), mistakeRecord.getMistakeName());
        mistakeRecord.setMistakeDate(LocalDate.now());
        mistakeRecord.setMastered(false);
        mistakeRecord.setReviewCount(0);
        return save(mistakeRecord);
    }

    @Override
    public List<MistakeRecord> getUnmasteredMistakes(Integer userId) {
        log.info("查询未掌握错题: userId={}", userId);
        return mistakeRecordMapper.getUnmasteredMistakes(userId);
    }

    @Override
    public Boolean markAsMastered(Integer id) {
        log.info("标记错题为已掌握: id={}", id);
        return mistakeRecordMapper.markAsMastered(id) > 0;
    }

    @Override
    public Boolean markAsUnmastered(Integer id) {
        log.info("标记错题为未掌握: id={}", id);
        return mistakeRecordMapper.markAsUnmastered(id) > 0;
    }

    @Override
    public Boolean reviewMistake(Integer id) {
        log.info("复习错题: id={}", id);
        return mistakeRecordMapper.incrementReviewCount(id) > 0;
    }

    @Override
    public List<Map<String, Object>> getMistakeStatistics(Integer userId) {
        log.info("错题统计: userId={}", userId);
        return mistakeRecordMapper.getMistakeStatistics(userId);
    }

    @Override
    public List<MistakeRecord> getMistakeList(Integer userId, String subjectName, Boolean mastered) {
        log.info("查询错题列表: userId={}, subjectName={}, mastered={}", userId, subjectName, mastered);
        LambdaQueryWrapper<MistakeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MistakeRecord::getUserId, userId);
        if (subjectName != null && !subjectName.isEmpty()) {
            wrapper.eq(MistakeRecord::getSubjectName, subjectName);
        }
        if (mastered != null) {
            wrapper.eq(MistakeRecord::getMastered, mastered);
        }
        wrapper.orderByDesc(MistakeRecord::getMistakeDate);
        return list(wrapper);
    }
}