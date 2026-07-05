package org.example.generalservice.service.activity.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.commoncore.utils.MyBeanUtils;
import org.example.commondb.utils.RestBean;
import org.example.generalservice.client.UserServiceClient;
import org.example.generalservice.dto.activity.HeartbeatDTO;
import org.example.generalservice.dto.activity.MedalRuleDTO;
import org.example.generalservice.entity.activity.MedalRule;
import org.example.generalservice.entity.activity.UserActivityStats;
import org.example.generalservice.entity.activity.UserMedal;
import org.example.generalservice.mapper.activity.MedalRuleMapper;
import org.example.generalservice.mapper.activity.UserActivityStatsMapper;
import org.example.generalservice.mapper.activity.UserMedalMapper;
import org.example.generalservice.service.activity.ActivityService;
import org.example.generalservice.vo.UserVO;
import org.example.generalservice.vo.activity.ActivitySummaryVO;
import org.example.generalservice.vo.activity.AdminActivityUserVO;
import org.example.generalservice.vo.activity.UserMedalVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private static final int DEFAULT_ONLINE_WINDOW_MINUTES = 5;
    private static final Map<String, Function<UserActivityStats, Integer>> RULE_VALUE_GETTERS = Map.of(
            "LOGIN_DAYS", s -> value(s.getTotalLoginDays()),
            "STREAK_DAYS", s -> value(s.getContinuousLoginDays()),
            "ONLINE_HOURS", s -> (int) (value(s.getTotalOnlineSeconds()) / 3600),
            "TODAY_ONLINE_MINUTES", s -> (int) (value(s.getTodayOnlineSeconds()) / 60),
            "SCORE", s -> value(s.getMedalScore())
    );

    private final UserActivityStatsMapper statsMapper;
    private final MedalRuleMapper medalRuleMapper;
    private final UserMedalMapper userMedalMapper;
    private final UserServiceClient userServiceClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivitySummaryVO checkIn(Long userId) {
        UserActivityStats stats = ensureStats(userId);
        LocalDate today = LocalDate.now();
        if (!today.equals(stats.getLastCheckinDate())) {
            LocalDate yesterday = today.minusDays(1);
            int streak = yesterday.equals(stats.getLastCheckinDate())
                    ? value(stats.getContinuousLoginDays()) + 1
                    : 1;
            stats.setTotalLoginDays(value(stats.getTotalLoginDays()) + 1);
            stats.setContinuousLoginDays(streak);
            stats.setMaxContinuousLoginDays(Math.max(value(stats.getMaxContinuousLoginDays()), streak));
            stats.setLastCheckinDate(today);
        }
        rollToday(stats, today);
        stats.setLastSeenAt(LocalDateTime.now());
        stats.setMedalScore(calculateScore(stats));
        stats.setUpdatedAt(LocalDateTime.now());
        statsMapper.updateById(stats);

        List<UserMedalVO> awarded = awardMedals(stats);
        ActivitySummaryVO summary = buildSummary(stats);
        summary.setNewlyAwarded(awarded);
        return summary;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivitySummaryVO heartbeat(Long userId, HeartbeatDTO dto) {
        UserActivityStats stats = ensureStats(userId);
        LocalDate today = LocalDate.now();
        rollToday(stats, today);
        int seconds = dto == null || dto.getActiveSeconds() == null ? 60 : dto.getActiveSeconds();
        int safeSeconds = Math.max(5, Math.min(seconds, 300));
        stats.setTodayOnlineSeconds(value(stats.getTodayOnlineSeconds()) + safeSeconds);
        stats.setTotalOnlineSeconds(value(stats.getTotalOnlineSeconds()) + safeSeconds);
        stats.setLastSeenAt(LocalDateTime.now());
        stats.setMedalScore(calculateScore(stats));
        stats.setUpdatedAt(LocalDateTime.now());
        statsMapper.updateById(stats);

        List<UserMedalVO> awarded = awardMedals(stats);
        ActivitySummaryVO summary = buildSummary(stats);
        summary.setNewlyAwarded(awarded);
        return summary;
    }

    @Override
    public ActivitySummaryVO summary(Long userId) {
        return buildSummary(ensureStats(userId));
    }

    @Override
    public List<MedalRule> medalRules() {
        return medalRuleMapper.selectList(new LambdaQueryWrapper<MedalRule>()
                .orderByAsc(MedalRule::getMedalType)
                .orderByAsc(MedalRule::getThresholdValue));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MedalRule saveMedalRule(MedalRuleDTO dto) {
        MedalRule rule = dto.getId() == null ? new MedalRule() : medalRuleMapper.selectById(dto.getId());
        if (rule == null) {
            rule = new MedalRule();
        }
        MyBeanUtils.copyNonNullProperties(dto, rule);
        if (rule.getEnabled() == null) {
            rule.setEnabled(true);
        }
        if (!StringUtils.hasText(rule.getCode())) {
            rule.setCode(rule.getMedalType() + "_" + rule.getThresholdValue());
        }
        if (rule.getCreatedAt() == null) {
            rule.setCreatedAt(LocalDateTime.now());
        }
        rule.setUpdatedAt(LocalDateTime.now());
        if (rule.getId() == null) {
            medalRuleMapper.insert(rule);
        } else {
            medalRuleMapper.updateById(rule);
        }
        return rule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMedalRuleStatus(Long id, Boolean enabled) {
        MedalRule rule = medalRuleMapper.selectById(id);
        if (rule == null) {
            return false;
        }
        rule.setEnabled(Boolean.TRUE.equals(enabled));
        rule.setUpdatedAt(LocalDateTime.now());
        return medalRuleMapper.updateById(rule) > 0;
    }

    @Override
    public List<AdminActivityUserVO> adminUserStats(String keyword, Integer onlineMinutes) {
        LambdaQueryWrapper<UserActivityStats> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(UserActivityStats::getLastSeenAt);
        List<UserActivityStats> statsList = statsMapper.selectList(wrapper);
        int window = onlineMinutes == null ? DEFAULT_ONLINE_WINDOW_MINUTES : Math.max(1, onlineMinutes);
        LocalDateTime onlineAfter = LocalDateTime.now().minusMinutes(window);

        return statsList.stream()
                .map(stats -> {
                    AdminActivityUserVO vo = new AdminActivityUserVO();
                    vo.setUserId(stats.getUserId());
                    vo.setTotalLoginDays(value(stats.getTotalLoginDays()));
                    vo.setContinuousLoginDays(value(stats.getContinuousLoginDays()));
                    vo.setTotalOnlineSeconds(value(stats.getTotalOnlineSeconds()));
                    vo.setTodayOnlineSeconds(value(stats.getTodayOnlineSeconds()));
                    vo.setMedalScore(value(stats.getMedalScore()));
                    vo.setMedalCount(userMedalMapper.selectCount(new LambdaQueryWrapper<UserMedal>()
                            .eq(UserMedal::getUserId, stats.getUserId())).intValue());
                    vo.setLastCheckinDate(stats.getLastCheckinDate());
                    vo.setLastSeenAt(stats.getLastSeenAt());
                    vo.setOnline(stats.getLastSeenAt() != null && !stats.getLastSeenAt().isBefore(onlineAfter));
                    fillUser(vo);
                    return vo;
                })
                .filter(vo -> !StringUtils.hasText(keyword)
                        || contains(vo.getUsername(), keyword)
                        || contains(vo.getName(), keyword)
                        || String.valueOf(vo.getUserId()).contains(keyword.trim()))
                .collect(Collectors.toList());
    }

    private UserActivityStats ensureStats(Long userId) {
        UserActivityStats stats = statsMapper.selectOne(new LambdaQueryWrapper<UserActivityStats>()
                .eq(UserActivityStats::getUserId, userId)
                .last("LIMIT 1"));
        if (stats != null) {
            return stats;
        }
        LocalDate today = LocalDate.now();
        stats = new UserActivityStats();
        stats.setUserId(userId);
        stats.setTotalLoginDays(0);
        stats.setContinuousLoginDays(0);
        stats.setMaxContinuousLoginDays(0);
        stats.setTotalOnlineSeconds(0L);
        stats.setTodayOnlineSeconds(0L);
        stats.setOnlineDate(today);
        stats.setMedalScore(0);
        stats.setCreatedAt(LocalDateTime.now());
        stats.setUpdatedAt(LocalDateTime.now());
        statsMapper.insert(stats);
        return stats;
    }

    private void rollToday(UserActivityStats stats, LocalDate today) {
        if (!today.equals(stats.getOnlineDate())) {
            stats.setOnlineDate(today);
            stats.setTodayOnlineSeconds(0L);
        }
    }

    private int calculateScore(UserActivityStats stats) {
        int loginScore = value(stats.getTotalLoginDays()) * 10;
        int streakScore = value(stats.getContinuousLoginDays()) * 15;
        int onlineScore = (int) (value(stats.getTotalOnlineSeconds()) / 3600) * 5;
        return loginScore + streakScore + onlineScore;
    }

    private List<UserMedalVO> awardMedals(UserActivityStats stats) {
        List<UserMedalVO> awarded = new ArrayList<>();
        List<MedalRule> rules = medalRuleMapper.selectList(new LambdaQueryWrapper<MedalRule>()
                .eq(MedalRule::getEnabled, true)
                .orderByAsc(MedalRule::getThresholdValue));
        for (MedalRule rule : rules) {
            Integer sourceValue = sourceValue(stats, rule);
            if (sourceValue == null || sourceValue < value(rule.getThresholdValue())) {
                continue;
            }
            Long existing = userMedalMapper.selectCount(new LambdaQueryWrapper<UserMedal>()
                    .eq(UserMedal::getUserId, stats.getUserId())
                    .eq(UserMedal::getCode, rule.getCode()));
            if (existing > 0) {
                continue;
            }
            UserMedal medal = new UserMedal();
            medal.setUserId(stats.getUserId());
            medal.setRuleId(rule.getId());
            medal.setCode(rule.getCode());
            medal.setName(rule.getName());
            medal.setDescription(rule.getDescription());
            medal.setMedalType(rule.getMedalType());
            medal.setSourceValue(sourceValue);
            medal.setIcon(rule.getIcon());
            medal.setColor(rule.getColor());
            medal.setAwardedAt(LocalDateTime.now());
            userMedalMapper.insert(medal);
            awarded.add(toMedalVO(medal));
        }
        return awarded;
    }

    private Integer sourceValue(UserActivityStats stats, MedalRule rule) {
        Function<UserActivityStats, Integer> getter = RULE_VALUE_GETTERS.get(rule.getMedalType());
        return getter == null ? null : getter.apply(stats);
    }

    private ActivitySummaryVO buildSummary(UserActivityStats stats) {
        ActivitySummaryVO vo = new ActivitySummaryVO();
        vo.setUserId(stats.getUserId());
        vo.setTotalLoginDays(value(stats.getTotalLoginDays()));
        vo.setContinuousLoginDays(value(stats.getContinuousLoginDays()));
        vo.setMaxContinuousLoginDays(value(stats.getMaxContinuousLoginDays()));
        vo.setTotalOnlineSeconds(value(stats.getTotalOnlineSeconds()));
        vo.setTodayOnlineSeconds(value(stats.getTodayOnlineSeconds()));
        vo.setLastCheckinDate(stats.getLastCheckinDate());
        vo.setLastSeenAt(stats.getLastSeenAt());
        vo.setMedalScore(value(stats.getMedalScore()));
        vo.setCheckedInToday(LocalDate.now().equals(stats.getLastCheckinDate()));
        vo.setMedals(userMedalMapper.selectList(new LambdaQueryWrapper<UserMedal>()
                        .eq(UserMedal::getUserId, stats.getUserId())
                        .orderByDesc(UserMedal::getAwardedAt))
                .stream()
                .map(this::toMedalVO)
                .collect(Collectors.toList()));
        return vo;
    }

    private UserMedalVO toMedalVO(UserMedal medal) {
        UserMedalVO vo = new UserMedalVO();
        MyBeanUtils.copyNonNullProperties(medal, vo);
        return vo;
    }

    private void fillUser(AdminActivityUserVO vo) {
        try {
            RestBean<UserVO> result = userServiceClient.getAuthorInfo(vo.getUserId());
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                UserVO user = result.getData();
                vo.setUsername(user.getUsername());
                vo.setName(user.getName());
                vo.setAvatar(user.getAvatar());
            }
        } catch (Exception ignored) {
        }
    }

    private static boolean contains(String source, String keyword) {
        return source != null && keyword != null && source.toLowerCase().contains(keyword.trim().toLowerCase());
    }

    private static int value(Integer value) {
        return value == null ? 0 : value;
    }

    private static long value(Long value) {
        return value == null ? 0L : value;
    }
}
