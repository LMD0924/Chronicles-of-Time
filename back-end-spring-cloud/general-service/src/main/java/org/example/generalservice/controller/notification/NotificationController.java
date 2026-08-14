package org.example.generalservice.controller.notification;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.commoncore.auth.AuthContext;
import org.example.commoncore.auth.AuthUser;
import org.example.commoncore.auth.RoleCodes;
import org.example.commondb.utils.RestBean;
import org.example.generalservice.dto.notification.NotificationSyncItem;
import org.example.generalservice.entity.NotificationPreference;
import org.example.generalservice.entity.SystemNotification;
import org.example.generalservice.mapper.NotificationMapper;
import org.example.generalservice.mapper.NotificationPreferenceMapper;
import org.example.generalservice.websocket.notification.NotificationRealtimePublisher;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private static final String DERIVED_BIZ_TYPE = "derived_reminder";

    private final NotificationMapper notificationMapper;
    private final NotificationPreferenceMapper preferenceMapper;
    private final NotificationRealtimePublisher realtimePublisher;

    @GetMapping
    public RestBean<Map<String, Object>> list(@RequestParam(defaultValue = "false") boolean unreadOnly,
                                               @RequestParam(defaultValue = "50") int limit,
                                               HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return RestBean.fail(401, "用户未登录");
        }
        QueryWrapper<SystemNotification> wrapper = new QueryWrapper<SystemNotification>()
                .eq("user_id", userId)
                .isNull("dismissed_at")
                .orderByAsc("read_status")
                .orderByDesc("created_at")
                .last("LIMIT " + Math.max(1, Math.min(limit, 100)));
        if (unreadOnly) {
            wrapper.eq("read_status", 0);
        }
        List<SystemNotification> records = notificationMapper.selectList(wrapper);
        Long unreadCount = notificationMapper.selectCount(new QueryWrapper<SystemNotification>()
                .eq("user_id", userId)
                .eq("read_status", 0)
                .isNull("dismissed_at"));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", records);
        result.put("unreadCount", unreadCount);
        result.put("preference", getOrDefaultPreference(userId));
        return RestBean.success(result);
    }

    @PostMapping("/sync")
    public RestBean<Integer> sync(@RequestBody(required = false) List<NotificationSyncItem> items,
                                  HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return RestBean.fail(401, "用户未登录");
        }
        List<NotificationSyncItem> safeItems = items == null ? List.of() : items.stream()
                .filter(Objects::nonNull)
                .filter(item -> StringUtils.hasText(item.getDedupeKey()) && StringUtils.hasText(item.getTitle()))
                .limit(100)
                .toList();
        Set<String> incomingKeys = safeItems.stream().map(NotificationSyncItem::getDedupeKey).collect(Collectors.toSet());
        LocalDateTime now = LocalDateTime.now();
        List<SystemNotification> createdNotifications = new ArrayList<>();

        for (NotificationSyncItem item : safeItems) {
            SystemNotification notification = notificationMapper.selectOne(new QueryWrapper<SystemNotification>()
                    .eq("user_id", userId)
                    .eq("dedupe_key", item.getDedupeKey())
                    .last("LIMIT 1"));
            if (notification == null) {
                notification = new SystemNotification();
                notification.setUserId(userId);
                notification.setDedupeKey(item.getDedupeKey());
                notification.setReadStatus(0);
                notification.setCreatedAt(now);
            }
            notification.setNoticeType(defaultText(item.getNoticeType(), "reminder"));
            notification.setTitle(item.getTitle().trim());
            notification.setContent(item.getContent());
            notification.setBizType(DERIVED_BIZ_TYPE);
            notification.setBizId(item.getBizId());
            notification.setActionPath(item.getActionPath());
            notification.setDueAt(item.getDueAt());
            notification.setDismissedAt(null);
            if (notification.getId() == null) {
                notificationMapper.insert(notification);
                createdNotifications.add(notification);
            } else {
                notificationMapper.updateById(notification);
            }
        }

        List<SystemNotification> previous = notificationMapper.selectList(new QueryWrapper<SystemNotification>()
                .eq("user_id", userId)
                .eq("biz_type", DERIVED_BIZ_TYPE)
                .isNull("dismissed_at"));
        for (SystemNotification notification : previous) {
            if (!incomingKeys.contains(notification.getDedupeKey())) {
                notification.setDismissedAt(now);
                notificationMapper.updateById(notification);
            }
        }
        createdNotifications.forEach(realtimePublisher::publish);
        return RestBean.success("提醒已同步", safeItems.size());
    }

    @PostMapping("/admin/{id}/publish")
    public RestBean<Boolean> publishSavedNotification(@PathVariable Long id, HttpServletRequest request) {
        AuthUser user = AuthContext.currentUser(request);
        if (user == null || !user.isLogin()) {
            return RestBean.fail(401, "用户未登录");
        }
        if (!user.hasAnyRole(RoleCodes.SUPER_ADMIN, RoleCodes.ADMIN)) {
            return RestBean.fail(403, "需要管理员权限");
        }
        SystemNotification notification = notificationMapper.selectById(id);
        if (notification == null || notification.getDismissedAt() != null) {
            return RestBean.fail(404, "通知不存在或已被移除");
        }
        realtimePublisher.publish(notification);
        return RestBean.success("通知已实时推送", true);
    }

    @PutMapping("/{id}/read")
    public RestBean<Boolean> markRead(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return RestBean.fail(401, "用户未登录");
        }
        int updated = notificationMapper.update(null, new UpdateWrapper<SystemNotification>()
                .eq("id", id)
                .eq("user_id", userId)
                .set("read_status", 1)
                .set("read_at", LocalDateTime.now()));
        return RestBean.success(updated > 0);
    }

    @PutMapping("/read-all")
    public RestBean<Boolean> markAllRead(HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return RestBean.fail(401, "用户未登录");
        }
        notificationMapper.update(null, new UpdateWrapper<SystemNotification>()
                .eq("user_id", userId)
                .eq("read_status", 0)
                .isNull("dismissed_at")
                .set("read_status", 1)
                .set("read_at", LocalDateTime.now()));
        return RestBean.success(true);
    }

    @DeleteMapping("/{id}")
    public RestBean<Boolean> dismiss(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return RestBean.fail(401, "用户未登录");
        }
        int updated = notificationMapper.update(null, new UpdateWrapper<SystemNotification>()
                .eq("id", id)
                .eq("user_id", userId)
                .set("dismissed_at", LocalDateTime.now()));
        return RestBean.success(updated > 0);
    }

    @GetMapping("/preference")
    public RestBean<NotificationPreference> getPreference(HttpServletRequest request) {
        Long userId = currentUserId(request);
        return userId == null ? RestBean.fail(401, "用户未登录") : RestBean.success(getOrDefaultPreference(userId));
    }

    @PutMapping("/preference")
    public RestBean<NotificationPreference> savePreference(@RequestBody NotificationPreference incoming,
                                                            HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return RestBean.fail(401, "用户未登录");
        }
        NotificationPreference preference = getOrDefaultPreference(userId);
        preference.setEnabled(normalizeFlag(incoming.getEnabled(), preference.getEnabled()));
        preference.setQuietHoursEnabled(normalizeFlag(incoming.getQuietHoursEnabled(), preference.getQuietHoursEnabled()));
        preference.setQuietStart(defaultText(incoming.getQuietStart(), preference.getQuietStart()));
        preference.setQuietEnd(defaultText(incoming.getQuietEnd(), preference.getQuietEnd()));
        preference.setPreferredStage(defaultText(incoming.getPreferredStage(), preference.getPreferredStage()));
        preference.setWeekStartsMonday(normalizeFlag(incoming.getWeekStartsMonday(), preference.getWeekStartsMonday()));
        preference.setDefaultReminderMinutes(incoming.getDefaultReminderMinutes() == null ? defaultInteger(preference.getDefaultReminderMinutes(), 10) : Math.max(0, Math.min(incoming.getDefaultReminderMinutes(), 1440)));
        preference.setDefaultStartTime(defaultText(incoming.getDefaultStartTime(), preference.getDefaultStartTime()));
        preference.setDefaultEndTime(defaultText(incoming.getDefaultEndTime(), preference.getDefaultEndTime()));
        preference.setBrowserNotificationsEnabled(normalizeFlag(incoming.getBrowserNotificationsEnabled(), preference.getBrowserNotificationsEnabled()));
        preference.setUpdatedAt(LocalDateTime.now());
        if (preferenceMapper.selectById(userId) == null) {
            preference.setCreatedAt(LocalDateTime.now());
            preferenceMapper.insert(preference);
        } else {
            preferenceMapper.updateById(preference);
        }
        return RestBean.success("偏好已保存", preference);
    }

    private NotificationPreference getOrDefaultPreference(Long userId) {
        NotificationPreference preference = preferenceMapper.selectById(userId);
        if (preference != null) {
            return preference;
        }
        preference = new NotificationPreference();
        preference.setUserId(userId);
        preference.setEnabled(1);
        preference.setQuietHoursEnabled(0);
        preference.setQuietStart("22:00");
        preference.setQuietEnd("08:00");
        preference.setPreferredStage("all");
        preference.setWeekStartsMonday(1);
        preference.setDefaultReminderMinutes(10);
        preference.setDefaultStartTime("09:00");
        preference.setDefaultEndTime("18:00");
        preference.setBrowserNotificationsEnabled(0);
        return preference;
    }

    private Integer defaultInteger(Integer value, Integer fallback) {
        return value == null ? fallback : value;
    }

    private Long currentUserId(HttpServletRequest request) {
        AuthUser user = AuthContext.currentUser(request);
        return user != null && user.isLogin() ? user.getUserId() : null;
    }

    private int normalizeFlag(Integer value, Integer fallback) {
        int resolved = value == null ? (fallback == null ? 0 : fallback) : value;
        return resolved == 0 ? 0 : 1;
    }

    private String defaultText(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }
}
