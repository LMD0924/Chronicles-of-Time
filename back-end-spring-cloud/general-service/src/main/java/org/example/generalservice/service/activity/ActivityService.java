package org.example.generalservice.service.activity;

import org.example.generalservice.dto.activity.HeartbeatDTO;
import org.example.generalservice.dto.activity.MedalRuleDTO;
import org.example.generalservice.entity.activity.MedalRule;
import org.example.generalservice.vo.activity.ActivitySummaryVO;
import org.example.generalservice.vo.activity.AdminActivityUserVO;

import java.util.List;

public interface ActivityService {

    ActivitySummaryVO checkIn(Long userId);

    ActivitySummaryVO heartbeat(Long userId, HeartbeatDTO dto);

    ActivitySummaryVO summary(Long userId);

    List<MedalRule> medalRules();

    MedalRule saveMedalRule(MedalRuleDTO dto);

    boolean updateMedalRuleStatus(Long id, Boolean enabled);

    List<AdminActivityUserVO> adminUserStats(String keyword, Integer onlineMinutes);
}
