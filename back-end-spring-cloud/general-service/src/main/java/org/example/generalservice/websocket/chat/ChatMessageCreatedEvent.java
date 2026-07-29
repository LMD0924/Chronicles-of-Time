package org.example.generalservice.websocket.chat;

import org.example.generalservice.vo.chat.MessageVO;

import java.util.Map;

public record ChatMessageCreatedEvent(Map<Long, MessageVO> deliveries) {
}