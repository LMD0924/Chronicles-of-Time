package org.example.generalservice.service.chat;

import org.example.generalservice.dto.chat.CreateGroupDTO;
import org.example.generalservice.dto.chat.ReadMessageDTO;
import org.example.generalservice.dto.chat.SendMessageDTO;
import org.example.generalservice.vo.chat.*;

import java.util.List;

public interface ChatService {

    List<ChatUserVO> searchUsers(Long currentUserId, String keyword);

    FriendVO addFriend(Long currentUserId, Long friendId);

    List<FriendVO> friends(Long currentUserId);

    GroupVO createGroup(Long currentUserId, CreateGroupDTO dto);

    GroupVO joinGroup(Long currentUserId, String groupNo);

    List<GroupVO> myGroups(Long currentUserId);

    List<GroupVO> searchGroups(Long currentUserId, String groupNo);

    MessageVO sendMessage(Long currentUserId, SendMessageDTO dto);

    List<MessageVO> messages(Long currentUserId, String conversationType, Long targetId, Long beforeId, Integer limit);

    boolean markRead(Long currentUserId, ReadMessageDTO dto);

    List<ConversationVO> conversations(Long currentUserId);

    List<GroupVO> adminGroups(String keyword);

    List<FriendVO> adminFriendships(String keyword);
}
