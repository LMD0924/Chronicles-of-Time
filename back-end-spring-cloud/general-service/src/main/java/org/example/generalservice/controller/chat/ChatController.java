package org.example.generalservice.controller.chat;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.commondb.utils.RestBean;
import org.example.generalservice.dto.chat.CreateGroupDTO;
import org.example.generalservice.dto.chat.GroupModerationDTO;
import org.example.generalservice.dto.chat.ReadMessageDTO;
import org.example.generalservice.dto.chat.SendMessageDTO;
import org.example.generalservice.dto.chat.UpdateFriendRemarkDTO;
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

    @PutMapping("/friends/{friendId}/remark")
    public RestBean<FriendVO> updateFriendRemark(@PathVariable Long friendId,
                                                  @RequestBody(required = false) UpdateFriendRemarkDTO dto,
                                                  HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return RestBean.fail(401, "User is not logged in");
        }
        try {
            return RestBean.success("Remark updated", chatService.updateFriendRemark(userId, friendId, dto));
        } catch (IllegalArgumentException exception) {
            return RestBean.fail(400, exception.getMessage());
        }
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

    @GetMapping("/groups/{groupId}/members")
    public RestBean<List<GroupMemberVO>> groupMembers(@PathVariable Long groupId, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) return RestBean.fail(401, "User is not logged in");
        try {
            return RestBean.success(chatService.groupMembers(userId, groupId));
        } catch (IllegalArgumentException exception) {
            return RestBean.fail(400, exception.getMessage());
        }
    }

    @PostMapping("/groups/{groupId}/members")
    public RestBean<GroupVO> inviteGroupMember(@PathVariable Long groupId, @RequestBody GroupModerationDTO dto,
                                                HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) return RestBean.fail(401, "User is not logged in");
        try {
            return RestBean.success(chatService.inviteGroupMember(userId, groupId, dto));
        } catch (IllegalArgumentException exception) {
            return RestBean.fail(400, exception.getMessage());
        }
    }

    @DeleteMapping("/groups/{groupId}/members/{targetUserId}")
    public RestBean<Boolean> removeGroupMember(@PathVariable Long groupId, @PathVariable Long targetUserId,
                                                HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) return RestBean.fail(401, "User is not logged in");
        try {
            return RestBean.success(chatService.removeGroupMember(userId, groupId, targetUserId));
        } catch (IllegalArgumentException exception) {
            return RestBean.fail(400, exception.getMessage());
        }
    }

    @PutMapping("/groups/{groupId}/members/{targetUserId}/role")
    public RestBean<GroupMemberVO> updateGroupMemberRole(@PathVariable Long groupId, @PathVariable Long targetUserId,
                                                          @RequestBody GroupModerationDTO dto,
                                                          HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) return RestBean.fail(401, "User is not logged in");
        try {
            return RestBean.success(chatService.updateGroupMemberRole(userId, groupId, targetUserId, dto));
        } catch (IllegalArgumentException exception) {
            return RestBean.fail(400, exception.getMessage());
        }
    }

    @PutMapping("/groups/{groupId}/members/{targetUserId}/mute")
    public RestBean<GroupMemberVO> muteGroupMember(@PathVariable Long groupId, @PathVariable Long targetUserId,
                                                    @RequestBody(required = false) GroupModerationDTO dto,
                                                    HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) return RestBean.fail(401, "User is not logged in");
        try {
            return RestBean.success(chatService.muteGroupMember(userId, groupId, targetUserId, dto));
        } catch (IllegalArgumentException exception) {
            return RestBean.fail(400, exception.getMessage());
        }
    }

    @PutMapping("/groups/{groupId}/mute-all")
    public RestBean<GroupVO> setGroupMutedAll(@PathVariable Long groupId, @RequestBody(required = false) GroupModerationDTO dto,
                                               HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) return RestBean.fail(401, "User is not logged in");
        try {
            return RestBean.success(chatService.setGroupMutedAll(userId, groupId, dto));
        } catch (IllegalArgumentException exception) {
            return RestBean.fail(400, exception.getMessage());
        }
    }

    @PutMapping("/groups/{groupId}/pinned-message")
    public RestBean<GroupVO> pinGroupMessage(@PathVariable Long groupId, @RequestBody(required = false) GroupModerationDTO dto,
                                              HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) return RestBean.fail(401, "User is not logged in");
        try {
            return RestBean.success(chatService.pinGroupMessage(userId, groupId, dto));
        } catch (IllegalArgumentException exception) {
            return RestBean.fail(400, exception.getMessage());
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

    @DeleteMapping("/messages/{messageId}")
    public RestBean<Boolean> deleteMessage(@PathVariable Long messageId, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) return RestBean.fail(401, "用户未登录");
        try {
            return RestBean.success("消息已从当前会话删除", chatService.deleteMessage(userId, messageId));
        } catch (IllegalArgumentException exception) {
            return RestBean.fail(400, exception.getMessage());
        }
    }

    @PutMapping("/messages/{messageId}/recall")
    public RestBean<MessageVO> recallMessage(@PathVariable Long messageId, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) return RestBean.fail(401, "用户未登录");
        try {
            return RestBean.success("消息已撤回", chatService.recallMessage(userId, messageId));
        } catch (IllegalArgumentException exception) {
            return RestBean.fail(400, exception.getMessage());
        }
    }

    @GetMapping("/admin/groups")
    public RestBean<List<GroupVO>> adminGroups(@RequestParam(required = false) String keyword,
                                                HttpServletRequest request) {
        if (!isAdmin(request)) {
            return RestBean.fail(403, "需要管理员权限");
        }
        return RestBean.success(chatService.adminGroups(keyword));
    }

    @GetMapping("/admin/friends")
    public RestBean<List<FriendVO>> adminFriends(@RequestParam(required = false) String keyword,
                                                 HttpServletRequest request) {
        if (!isAdmin(request)) {
            return RestBean.fail(403, "需要管理员权限");
        }
        return RestBean.success(chatService.adminFriendships(keyword));
    }

    private boolean isAdmin(HttpServletRequest request) {
        String roles = String.join(",",
                valueOrEmpty(request.getHeader("X-User-Role")),
                valueOrEmpty(request.getHeader("X-User-Roles")));
        for (String role : roles.split("[,\\s]+")) {
            if ("ADMIN".equalsIgnoreCase(role) || "SUPER_ADMIN".equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
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
