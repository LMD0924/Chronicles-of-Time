package org.example.generalservice.websocket.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.generalservice.vo.chat.MessageVO;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRealtimePublisher {

    private final ChatWebSocketSessionRegistry sessionRegistry;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(ChatMessageCreatedEvent event) {
        event.deliveries().forEach((userId, message) -> sessionRegistry.sendToUser(userId, payload(message)));
    }

    private String payload(MessageVO message) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("type", "CHAT_MESSAGE");
        ObjectNode data = root.putObject("data");
        putText(data, "id", message.getId());
        putText(data, "conversationType", message.getConversationType());
        putText(data, "groupId", message.getGroupId());
        putText(data, "senderId", message.getSenderId());
        putText(data, "senderName", message.getSenderName());
        putText(data, "senderAvatar", message.getSenderAvatar());
        putText(data, "senderRole", message.getSenderRole());
        putText(data, "receiverId", message.getReceiverId());
        putText(data, "contentType", message.getContentType());
        putText(data, "content", message.getContent());
        putText(data, "createdAt", message.getCreatedAt());
        if (message.getReadCount() != null) {
            data.put("readCount", message.getReadCount());
        }
        if (message.getUnreadCount() != null) {
            data.put("unreadCount", message.getUnreadCount());
        }
        if (message.getReadByMe() != null) {
            data.put("readByMe", message.getReadByMe());
        }
        if (message.getMine() != null) {
            data.put("mine", message.getMine());
        }
        try {
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException exception) {
            log.warn("聊天实时消息序列化失败: messageId={}", message.getId(), exception);
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