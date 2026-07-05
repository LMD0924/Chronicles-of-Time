package org.example.generalservice.controller.chat;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.commondb.utils.RestBean;
import org.example.generalservice.dto.chat.CreateGroupDTO;
import org.example.generalservice.dto.chat.ReadMessageDTO;
import org.example.generalservice.dto.chat.SendMessageDTO;
import org.example.generalservice.service.chat.ChatService;
import org.example.generalservice.vo.chat.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/users/search")
    public RestBean<List<ChatUserVO>> searchUsers(@RequestParam String keyword, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return RestBean.fail(401, "用户未登录");
        }
        return RestBean.success(chatService.searchUsers(userId, keyword));
    }

    @PostMapping("/friends/{friendId}")
    public RestBean<FriendVO> addFriend(@PathVariable Long friendId, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return RestBean.fail(401, "用户未登录");
        }
        try {
            return RestBean.success("好友已添加", chatService.addFriend(userId, friendId));
        } catch (IllegalArgumentException e) {
            return RestBean.fail(400, e.getMessage());
        }
    }

    @GetMapping("/friends")
    public RestBean<List<FriendVO>> friends(HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return RestBean.fail(401, "用户未登录");
        }
        return RestBean.success(chatService.friends(userId));
    }

    @PostMapping("/groups")
    public RestBean<GroupVO> createGroup(@RequestBody CreateGroupDTO dto, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return RestBean.fail(401, "用户未登录");
        }
        try {
            return RestBean.success("群聊已创建", chatService.createGroup(userId, dto));
        } catch (IllegalArgumentException e) {
            return RestBean.fail(400, e.getMessage());
        }
    }

    @GetMapping("/groups")
    public RestBean<List<GroupVO>> myGroups(HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return RestBean.fail(401, "用户未登录");
        }
        return RestBean.success(chatService.myGroups(userId));
    }

    @GetMapping("/groups/search")
    public RestBean<List<GroupVO>> searchGroups(@RequestParam String groupNo, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return RestBean.fail(401, "用户未登录");
        }
        return RestBean.success(chatService.searchGroups(userId, groupNo));
    }

    @PostMapping("/groups/join/{groupNo}")
    public RestBean<GroupVO> joinGroup(@PathVariable String groupNo, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return RestBean.fail(401, "用户未登录");
        }
        try {
            return RestBean.success("已加入群聊", chatService.joinGroup(userId, groupNo));
        } catch (IllegalArgumentException e) {
            return RestBean.fail(400, e.getMessage());
        }
    }

    @GetMapping("/conversations")
    public RestBean<List<ConversationVO>> conversations(HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return RestBean.fail(401, "用户未登录");
        }
        return RestBean.success(chatService.conversations(userId));
    }

    @PostMapping("/messages")
    public RestBean<MessageVO> sendMessage(@RequestBody SendMessageDTO dto, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return RestBean.fail(401, "用户未登录");
        }
        try {
            return RestBean.success("发送成功", chatService.sendMessage(userId, dto));
        } catch (IllegalArgumentException e) {
            return RestBean.fail(400, e.getMessage());
        }
    }

    @GetMapping("/messages")
    public RestBean<List<MessageVO>> messages(@RequestParam String conversationType,
                                              @RequestParam Long targetId,
                                              @RequestParam(required = false) Long beforeId,
                                              @RequestParam(defaultValue = "50") Integer limit,
                                              HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return RestBean.fail(401, "用户未登录");
        }
        try {
            return RestBean.success(chatService.messages(userId, conversationType, targetId, beforeId, limit));
        } catch (IllegalArgumentException e) {
            return RestBean.fail(400, e.getMessage());
        }
    }

    @PostMapping("/messages/read")
    public RestBean<Boolean> markRead(@RequestBody ReadMessageDTO dto, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return RestBean.fail(401, "用户未登录");
        }
        try {
            return RestBean.success("已读状态已更新", chatService.markRead(userId, dto));
        } catch (IllegalArgumentException e) {
            return RestBean.fail(400, e.getMessage());
        }
    }

    @GetMapping("/admin/groups")
    public RestBean<List<GroupVO>> adminGroups(@RequestParam(required = false) String keyword) {
        return RestBean.success(chatService.adminGroups(keyword));
    }

    @GetMapping("/admin/friends")
    public RestBean<List<FriendVO>> adminFriends(@RequestParam(required = false) String keyword) {
        return RestBean.success(chatService.adminFriendships(keyword));
    }

    private Long currentUserId(HttpServletRequest request) {
        String value = request.getHeader("X-User-Id");
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
