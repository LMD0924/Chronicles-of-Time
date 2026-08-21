package org.example.generalservice.service.chat;

import org.example.generalservice.dto.chat.CreateGroupDTO;
import org.example.generalservice.dto.chat.GroupModerationDTO;
import org.example.generalservice.dto.chat.ReadMessageDTO;
import org.example.generalservice.dto.chat.SendMessageDTO;
import org.example.generalservice.dto.chat.UpdateFriendRemarkDTO;
import org.example.generalservice.vo.chat.*;

import java.util.List;

public interface ChatService {

    List<ChatUserVO> searchUsers(Long currentUserId, String keyword);
    FriendVO addFriend(Long currentUserId, Long friendId);
    List<FriendVO> friends(Long currentUserId);
    FriendVO updateFriendRemark(Long currentUserId, Long friendId, UpdateFriendRemarkDTO dto);
    GroupVO createGroup(Long currentUserId, CreateGroupDTO dto);
    GroupVO joinGroup(Long currentUserId, String groupNo);
    List<GroupVO> myGroups(Long currentUserId);
    List<GroupVO> searchGroups(Long currentUserId, String groupNo);
    List<GroupMemberVO> groupMembers(Long currentUserId, Long groupId);
    GroupVO inviteGroupMember(Long currentUserId, Long groupId, GroupModerationDTO dto);
    boolean removeGroupMember(Long currentUserId, Long groupId, Long targetUserId);
    GroupMemberVO muteGroupMember(Long currentUserId, Long groupId, Long targetUserId, GroupModerationDTO dto);
    GroupMemberVO updateGroupMemberRole(Long currentUserId, Long groupId, Long targetUserId, GroupModerationDTO dto);
    GroupVO setGroupMutedAll(Long currentUserId, Long groupId, GroupModerationDTO dto);
    GroupVO pinGroupMessage(Long currentUserId, Long groupId, GroupModerationDTO dto);
    MessageVO sendMessage(Long currentUserId, SendMessageDTO dto);
    List<MessageVO> messages(Long currentUserId, String conversationType, Long targetId, Long beforeId, Integer limit);
    boolean markRead(Long currentUserId, ReadMessageDTO dto);
    boolean deleteMessage(Long currentUserId, Long messageId);
    MessageVO recallMessage(Long currentUserId, Long messageId);
    List<ConversationVO> conversations(Long currentUserId);
    List<GroupVO> adminGroups(String keyword);
    List<FriendVO> adminFriendships(String keyword);
}
