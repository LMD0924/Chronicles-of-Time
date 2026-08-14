package org.example.generalservice.websocket.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.generalservice.entity.SystemNotification;
import org.example.generalservice.entity.NotificationPreference;
import org.example.generalservice.mapper.NotificationPreferenceMapper;
import org.example.generalservice.websocket.chat.ChatWebSocketSessionRegistry;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRealtimePublisher {

    private final ChatWebSocketSessionRegistry sessionRegistry;
    private final NotificationPreferenceMapper preferenceMapper;
    private final ObjectMapper objectMapper;

    public void publish(SystemNotification notification) {
        if (notification == null || notification.getUserId() == null) {
            return;
        }
        if (!canPush(notification.getUserId())) {
            return;
        }
        String payload = payload(notification);
        if (!payload.isBlank()) {
            sessionRegistry.sendToUser(notification.getUserId(), payload);
        }
    }

    private boolean canPush(Long userId) {
        NotificationPreference preference = preferenceMapper.selectById(userId);
        if (preference == null) {
            return true;
        }
        if (Integer.valueOf(0).equals(preference.getEnabled())) {
            return false;
        }
        if (!Integer.valueOf(1).equals(preference.getQuietHoursEnabled())) {
            return true;
        }
        try {
            LocalTime start = LocalTime.parse(preference.getQuietStart());
            LocalTime end = LocalTime.parse(preference.getQuietEnd());
            LocalTime now = LocalTime.now();
            if (start.equals(end)) {
                return false;
            }
            boolean quiet = start.isBefore(end)
                    ? !now.isBefore(start) && now.isBefore(end)
                    : !now.isBefore(start) || now.isBefore(end);
            return !quiet;
        } catch (DateTimeParseException | NullPointerException ignored) {
            return true;
        }
    }

    private String payload(SystemNotification notification) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("type", "SYSTEM_NOTIFICATION");
        ObjectNode data = root.putObject("data");
        putText(data, "id", notification.getId());
        putText(data, "noticeType", notification.getNoticeType());
        putText(data, "title", notification.getTitle());
        putText(data, "content", notification.getContent());
        putText(data, "bizType", notification.getBizType());
        putText(data, "bizId", notification.getBizId());
        putText(data, "actionPath", notification.getActionPath());
        putText(data, "dueAt", notification.getDueAt());
        putText(data, "createdAt", notification.getCreatedAt());
        data.put("readStatus", notification.getReadStatus() == null ? 0 : notification.getReadStatus());
        try {
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException exception) {
            log.warn("系统通知实时消息序列化失败: notificationId={}", notification.getId(), exception);
            return "";
        }
    }

    private void putText(ObjectNode node, String field, Object value) {
        if (value == null) {
            node.putNull(field);
        } else {
            node.put(field, String.valueOf(value));
        }
    }
}
