package org.example.generalservice.service.activity.Impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.generalservice.entity.PracticeSession;
import org.example.generalservice.mapper.question.PracticeSessionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LearningProgressQueryService {

    private final PracticeSessionMapper practiceSessionMapper;

    @DS("cot_learning")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public long countCompletedPractices(Long userId, LocalDateTime since) {
        LambdaQueryWrapper<PracticeSession> wrapper = new LambdaQueryWrapper<PracticeSession>()
                .eq(PracticeSession::getUserId, userId)
                .eq(PracticeSession::getStatus, 2);
        if (since != null) {
            wrapper.ge(PracticeSession::getFinishedAt, since);
        }
        return practiceSessionMapper.selectCount(wrapper);
    }
}