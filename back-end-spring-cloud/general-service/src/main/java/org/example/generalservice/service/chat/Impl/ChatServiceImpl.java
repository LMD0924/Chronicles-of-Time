package org.example.generalservice.service.chat.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.commoncore.utils.MyBeanUtils;
import org.example.commondb.utils.RestBean;
import org.example.generalservice.client.UserServiceClient;
import org.example.generalservice.dto.chat.CreateGroupDTO;
import org.example.generalservice.dto.chat.GroupModerationDTO;
import org.example.generalservice.dto.chat.ReadMessageDTO;
import org.example.generalservice.dto.chat.SendMessageDTO;
import org.example.generalservice.dto.chat.UpdateFriendRemarkDTO;
import org.example.generalservice.entity.chat.*;
import org.example.generalservice.mapper.chat.*;
import org.example.generalservice.service.activity.ActivityService;
import org.example.generalservice.service.chat.ChatService;
import org.example.generalservice.vo.UserVO;
import org.example.generalservice.vo.activity.ActivitySummaryVO;
import org.example.generalservice.websocket.chat.ChatMessageCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.example.generalservice.vo.chat.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private static final String PRIVATE = "PRIVATE";
    private static final String GROUP = "GROUP";
    private static final String ACTIVE = "ACTIVE";
    private static final String OWNER = "OWNER";
    private static final String ADMIN = "ADMIN";
    private static final String MEMBER = "MEMBER";
    private static final int MAX_GROUP_ADMINS = 5;

    private final ChatFriendMapper friendMapper;
    private final ChatGroupMapper groupMapper;
    private final ChatGroupMemberMapper memberMapper;
    private final ChatMessageMapper messageMapper;
    private final ChatMessageReadMapper readMapper;
    private final ChatMessageHiddenMapper hiddenMapper;
    private final UserServiceClient userServiceClient;
    private final ApplicationEventPublisher eventPublisher;
    private final ActivityService activityService;

    @Override
    public List<ChatUserVO> searchUsers(Long currentUserId, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        RestBean<List<UserVO>> result = userServiceClient.searchPublicUsers(keyword.trim(), 10);
        if (result == null || result.getCode() != 200 || result.getData() == null) {
            return List.of();
        }
        return result.getData().stream()
                .filter(user -> !Objects.equals(user.getId(), currentUserId))
                .map(this::toChatUser)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FriendVO addFriend(Long currentUserId, Long friendId) {
        if (Objects.equals(currentUserId, friendId)) {
            throw new IllegalArgumentException("不能添加自己为好友");
        }
        UserVO target = loadUser(friendId);
        if (target == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        upsertFriend(currentUserId, friendId);
        upsertFriend(friendId, currentUserId);
        ChatFriend relation = friendMapper.selectOne(new LambdaQueryWrapper<ChatFriend>()
                .eq(ChatFriend::getUserId, currentUserId)
                .eq(ChatFriend::getFriendId, friendId)
                .last("LIMIT 1"));
        return toFriendVO(relation);
    }

    @Override
    public List<FriendVO> friends(Long currentUserId) {
        return friendMapper.selectList(new LambdaQueryWrapper<ChatFriend>()
                        .eq(ChatFriend::getUserId, currentUserId)
                        .eq(ChatFriend::getStatus, ACTIVE)
                        .orderByDesc(ChatFriend::getUpdatedAt))
                .stream()
                .map(this::toFriendVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FriendVO updateFriendRemark(Long currentUserId, Long friendId, UpdateFriendRemarkDTO dto) {
        requireFriend(currentUserId, friendId);
        String remark = dto == null ? null : dto.getRemark();
        if (remark != null && remark.trim().length() > 40) {
            throw new IllegalArgumentException("Remark cannot exceed 40 characters");
        }
        ChatFriend relation = friendMapper.selectOne(new LambdaQueryWrapper<ChatFriend>()
                .eq(ChatFriend::getUserId, currentUserId)
                .eq(ChatFriend::getFriendId, friendId)
                .last("LIMIT 1"));
        if (relation == null) {
            throw new IllegalArgumentException("Friendship does not exist");
        }
        relation.setRemark(StringUtils.hasText(remark) ? remark.trim() : null);
        relation.setUpdatedAt(LocalDateTime.now());
        friendMapper.updateById(relation);
        return toFriendVO(relation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GroupVO createGroup(Long currentUserId, CreateGroupDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getName())) {
            throw new IllegalArgumentException("群名称不能为空");
        }
        ChatGroup group = new ChatGroup();
        group.setGroupNo(generateGroupNo());
        group.setName(dto.getName().trim());
        group.setAnnouncement(dto.getAnnouncement());
        group.setOwnerId(currentUserId);
        group.setMemberCount(1);
        group.setSearchable(dto.getSearchable() == null || dto.getSearchable());
        group.setMutedAll(false);
        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());
        groupMapper.insert(group);

        ChatGroupMember member = new ChatGroupMember();
        member.setGroupId(group.getId());
        member.setUserId(currentUserId);
        member.setRole("OWNER");
        member.setStatus(ACTIVE);
        member.setJoinedAt(LocalDateTime.now());
        member.setLastReadAt(LocalDateTime.now());
        memberMapper.insert(member);
        return toGroupVO(group, currentUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GroupVO joinGroup(Long currentUserId, String groupNo) {
        ChatGroup group = groupMapper.selectOne(new LambdaQueryWrapper<ChatGroup>()
                .eq(ChatGroup::getGroupNo, groupNo)
                .eq(ChatGroup::getSearchable, true)
                .last("LIMIT 1"));
        if (group == null) {
            throw new IllegalArgumentException("群聊不存在或不可加入");
        }
        ChatGroupMember existing = memberMapper.selectOne(new LambdaQueryWrapper<ChatGroupMember>()
                .eq(ChatGroupMember::getGroupId, group.getId())
                .eq(ChatGroupMember::getUserId, currentUserId)
                .last("LIMIT 1"));
        if (existing == null) {
            ChatGroupMember member = new ChatGroupMember();
            member.setGroupId(group.getId());
            member.setUserId(currentUserId);
            member.setRole("MEMBER");
            member.setStatus(ACTIVE);
            member.setJoinedAt(LocalDateTime.now());
            member.setLastReadAt(LocalDateTime.now());
            memberMapper.insert(member);
            group.setMemberCount(value(group.getMemberCount()) + 1);
            group.setUpdatedAt(LocalDateTime.now());
            groupMapper.updateById(group);
        } else if (!ACTIVE.equals(existing.getStatus())) {
            existing.setStatus(ACTIVE);
            existing.setJoinedAt(LocalDateTime.now());
            existing.setLastReadAt(LocalDateTime.now());
            existing.setMutedUntil(null);
            memberMapper.updateById(existing);
        }
        return toGroupVO(group, currentUserId);
    }

    @Override
    public List<GroupVO> myGroups(Long currentUserId) {
        List<ChatGroupMember> memberships = memberMapper.selectList(new LambdaQueryWrapper<ChatGroupMember>()
                .eq(ChatGroupMember::getUserId, currentUserId)
                .eq(ChatGroupMember::getStatus, ACTIVE)
                .orderByDesc(ChatGroupMember::getJoinedAt));
        return memberships.stream()
                .map(member -> groupMapper.selectById(member.getGroupId()))
                .filter(Objects::nonNull)
                .map(group -> toGroupVO(group, currentUserId))
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupVO> searchGroups(Long currentUserId, String groupNo) {
        if (!StringUtils.hasText(groupNo)) {
            return List.of();
        }
        return groupMapper.selectList(new LambdaQueryWrapper<ChatGroup>()
                        .eq(ChatGroup::getSearchable, true)
                        .like(ChatGroup::getGroupNo, groupNo.trim())
                        .orderByDesc(ChatGroup::getUpdatedAt)
                        .last("LIMIT 10"))
                .stream()
                .map(group -> toGroupVO(group, currentUserId))
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupMemberVO> groupMembers(Long currentUserId, Long groupId) {
        requireGroupMember(groupId, currentUserId);
        return memberMapper.selectList(new LambdaQueryWrapper<ChatGroupMember>()
                        .eq(ChatGroupMember::getGroupId, groupId)
                        .eq(ChatGroupMember::getStatus, ACTIVE)
                        .orderByDesc(ChatGroupMember::getRole)
                        .orderByAsc(ChatGroupMember::getJoinedAt))
                .stream()
                .map(this::toGroupMemberVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GroupVO inviteGroupMember(Long currentUserId, Long groupId, GroupModerationDTO dto) {
        ChatGroup group = requireGroup(groupId);
        requireGroupManager(groupId, currentUserId);
        Long targetUserId = dto == null ? null : dto.getUserId();
        if (targetUserId == null || loadUser(targetUserId) == null) {
            throw new IllegalArgumentException("User does not exist");
        }
        ChatGroupMember member = findGroupMember(groupId, targetUserId);
        if (member != null && ACTIVE.equals(member.getStatus())) {
            throw new IllegalArgumentException("User is already in this group");
        }
        if (member == null) {
            member = new ChatGroupMember();
            member.setGroupId(groupId);
            member.setUserId(targetUserId);
            member.setRole(MEMBER);
            member.setStatus(ACTIVE);
            member.setJoinedAt(LocalDateTime.now());
            member.setLastReadAt(LocalDateTime.now());
            memberMapper.insert(member);
        } else {
            member.setRole(MEMBER);
            member.setStatus(ACTIVE);
            member.setMutedUntil(null);
            member.setJoinedAt(LocalDateTime.now());
            member.setLastReadAt(LocalDateTime.now());
            memberMapper.updateById(member);
        }
        refreshMemberCount(group);
        return toGroupVO(group, currentUserId);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeGroupMember(Long currentUserId, Long groupId, Long targetUserId) {
        ChatGroup group = requireGroup(groupId);
        ChatGroupMember operator = requireGroupManager(groupId, currentUserId);
        ChatGroupMember member = findGroupMember(groupId, targetUserId);
        assertCanManageTarget(group, operator, member);
        member.setStatus("REMOVED");
        member.setMutedUntil(null);
        memberMapper.updateById(member);
        refreshMemberCount(group);
        return true;
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public GroupMemberVO muteGroupMember(Long currentUserId, Long groupId, Long targetUserId, GroupModerationDTO dto) {
        ChatGroup group = requireGroup(groupId);
        ChatGroupMember operator = requireGroupManager(groupId, currentUserId);
        ChatGroupMember member = findGroupMember(groupId, targetUserId);
        assertCanManageTarget(group, operator, member);
        int minutes = dto == null || dto.getMuteMinutes() == null ? 0 : dto.getMuteMinutes();
        if (minutes < 0 || minutes > 10080) {
            throw new IllegalArgumentException("Mute duration must be between 0 and 10080 minutes");
        }
        member.setMutedUntil(minutes == 0 ? null : LocalDateTime.now().plusMinutes(minutes));
        memberMapper.updateById(member);
        return toGroupMemberVO(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GroupMemberVO updateGroupMemberRole(Long currentUserId, Long groupId, Long targetUserId, GroupModerationDTO dto) {
        ChatGroup group = requireGroupOwner(groupId, currentUserId);
        ChatGroupMember member = findGroupMember(groupId, targetUserId);
        if (member == null || !ACTIVE.equals(member.getStatus()) || Objects.equals(group.getOwnerId(), targetUserId)) {
            throw new IllegalArgumentException("Group member cannot be changed");
        }
        String role = dto == null || !StringUtils.hasText(dto.getRole()) ? MEMBER : dto.getRole().trim().toUpperCase(Locale.ROOT);
        if (!Set.of(ADMIN, MEMBER).contains(role)) {
            throw new IllegalArgumentException("Unsupported group role");
        }
        if (ADMIN.equals(role) && !ADMIN.equals(member.getRole())) {
            long adminCount = memberMapper.selectCount(new LambdaQueryWrapper<ChatGroupMember>()
                    .eq(ChatGroupMember::getGroupId, groupId)
                    .eq(ChatGroupMember::getStatus, ACTIVE)
                    .eq(ChatGroupMember::getRole, ADMIN));
            if (adminCount >= MAX_GROUP_ADMINS) {
                throw new IllegalArgumentException("A group can have at most 5 administrators");
            }
        }
        member.setRole(role);
        memberMapper.updateById(member);
        return toGroupMemberVO(member);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public GroupVO setGroupMutedAll(Long currentUserId, Long groupId, GroupModerationDTO dto) {
        ChatGroup group = requireGroupOwner(groupId, currentUserId);
        group.setMutedAll(dto != null && Boolean.TRUE.equals(dto.getEnabled()));
        group.setUpdatedAt(LocalDateTime.now());
        groupMapper.updateById(group);
        return toGroupVO(group, currentUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GroupVO pinGroupMessage(Long currentUserId, Long groupId, GroupModerationDTO dto) {
        ChatGroup group = requireGroupOwner(groupId, currentUserId);
        Long messageId = dto == null ? null : dto.getMessageId();
        if (messageId != null) {
            ChatMessage message = messageMapper.selectById(messageId);
            if (message == null || !GROUP.equals(message.getConversationType())
                    || !Objects.equals(groupId, message.getGroupId()) || message.getRecalledAt() != null) {
                throw new IllegalArgumentException("Message cannot be pinned");
            }
        }
        group.setPinnedMessageId(messageId);
        group.setUpdatedAt(LocalDateTime.now());
        groupMapper.updateById(group);
        return toGroupVO(group, currentUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageVO sendMessage(Long currentUserId, SendMessageDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getContent())) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        String type = normalizeType(dto.getConversationType());
        if (GROUP.equals(type)) {
            requireGroupCanSend(dto.getGroupId(), currentUserId);
        } else {
            requireFriend(currentUserId, dto.getReceiverId());
        }

        ChatMessage message = new ChatMessage();
        message.setConversationType(type);
        message.setGroupId(GROUP.equals(type) ? dto.getGroupId() : null);
        message.setSenderId(currentUserId);
        message.setReceiverId(PRIVATE.equals(type) ? dto.getReceiverId() : null);
        String contentType = StringUtils.hasText(dto.getContentType())
                ? dto.getContentType().trim().toUpperCase(Locale.ROOT) : "TEXT";
        if (!Set.of("TEXT", "IMAGE", "FILE", "EMOJI").contains(contentType)) {
            throw new IllegalArgumentException("Unsupported message type");
        }
        message.setContentType(contentType);
        message.setContent(dto.getContent().trim());
        message.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(message);
        MessageVO result = toMessageVO(message, currentUserId);
        eventPublisher.publishEvent(new ChatMessageCreatedEvent(realtimeDeliveries(message)));
        return result;
    }

    @Override
    public List<MessageVO> messages(Long currentUserId, String conversationType, Long targetId, Long beforeId, Integer limit) {
        String type = normalizeType(conversationType);
        int safeLimit = limit == null ? 50 : Math.max(1, Math.min(limit, 100));
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getConversationType, type)
                .isNull(ChatMessage::getRecalledAt);
        if (GROUP.equals(type)) {
            requireGroupMember(targetId, currentUserId);
            wrapper.eq(ChatMessage::getGroupId, targetId);
        } else {
            requireFriend(currentUserId, targetId);
            wrapper.and(q -> q.eq(ChatMessage::getSenderId, currentUserId).eq(ChatMessage::getReceiverId, targetId)
                    .or()
                    .eq(ChatMessage::getSenderId, targetId).eq(ChatMessage::getReceiverId, currentUserId));
        }
        if (beforeId != null) {
            wrapper.lt(ChatMessage::getId, beforeId);
        }
        wrapper.orderByDesc(ChatMessage::getId).last("LIMIT " + safeLimit);
        List<ChatMessage> rows = messageMapper.selectList(wrapper);
        Collections.reverse(rows);
        Set<Long> hiddenIds = hiddenMapper.selectList(new LambdaQueryWrapper<ChatMessageHidden>()
                        .eq(ChatMessageHidden::getUserId, currentUserId))
                .stream().map(ChatMessageHidden::getMessageId).collect(Collectors.toSet());
        return rows.stream().filter(message -> !hiddenIds.contains(message.getId()))
                .map(message -> toMessageVO(message, currentUserId)).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markRead(Long currentUserId, ReadMessageDTO dto) {
        if (dto == null) {
            return false;
        }
        List<ChatMessage> messages = resolveMessagesForRead(currentUserId, dto);
        for (ChatMessage message : messages) {
            if (!Objects.equals(message.getSenderId(), currentUserId)) {
                markMessageRead(message.getId(), currentUserId);
            }
        }
        if (GROUP.equals(normalizeType(dto.getConversationType())) && dto.getGroupId() != null) {
            ChatGroupMember member = memberMapper.selectOne(new LambdaQueryWrapper<ChatGroupMember>()
                    .eq(ChatGroupMember::getGroupId, dto.getGroupId())
                    .eq(ChatGroupMember::getUserId, currentUserId)
                    .last("LIMIT 1"));
            if (member != null) {
                member.setLastReadAt(LocalDateTime.now());
                memberMapper.updateById(member);
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMessage(Long currentUserId, Long messageId) {
        ChatMessage message = requireVisibleMessage(currentUserId, messageId);
        Long exists = hiddenMapper.selectCount(new LambdaQueryWrapper<ChatMessageHidden>()
                .eq(ChatMessageHidden::getMessageId, messageId)
                .eq(ChatMessageHidden::getUserId, currentUserId));
        if (exists == 0) {
            ChatMessageHidden hidden = new ChatMessageHidden();
            hidden.setMessageId(message.getId());
            hidden.setUserId(currentUserId);
            hidden.setHiddenAt(LocalDateTime.now());
            hiddenMapper.insert(hidden);
        }
        if (!Objects.equals(message.getSenderId(), currentUserId)) {
            markMessageRead(message.getId(), currentUserId);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageVO recallMessage(Long currentUserId, Long messageId) {
        ChatMessage message = requireVisibleMessage(currentUserId, messageId);
        if (!Objects.equals(message.getSenderId(), currentUserId)) {
            throw new IllegalArgumentException("只能撤回自己发送的消息");
        }
        if (message.getRecalledAt() != null) {
            throw new IllegalArgumentException("消息已经撤回");
        }
        if (message.getCreatedAt() == null || message.getCreatedAt().plusMinutes(2).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("消息发送超过 2 分钟，无法撤回");
        }
        message.setRecalledAt(LocalDateTime.now());
        messageMapper.updateById(message);
        ChatGroup group = message.getGroupId() == null ? null : groupMapper.selectById(message.getGroupId());
        if (group != null && Objects.equals(group.getPinnedMessageId(), message.getId())) {
            group.setPinnedMessageId(null);
            group.setUpdatedAt(LocalDateTime.now());
            groupMapper.updateById(group);
        }
        MessageVO result = toMessageVO(message, currentUserId);
        eventPublisher.publishEvent(new ChatMessageCreatedEvent(realtimeDeliveries(message)));
        return result;
    }

    private ChatMessage requireVisibleMessage(Long currentUserId, Long messageId) {
        ChatMessage message = messageMapper.selectById(messageId);
        if (message == null) throw new IllegalArgumentException("消息不存在");
        if (GROUP.equals(message.getConversationType())) {
            requireGroupMember(message.getGroupId(), currentUserId);
        } else if (!Objects.equals(message.getSenderId(), currentUserId)
                && !Objects.equals(message.getReceiverId(), currentUserId)) {
            throw new IllegalArgumentException("无权操作此消息");
        }
        return message;
    }

    @Override
    public List<ConversationVO> conversations(Long currentUserId) {
        Stream<ConversationVO> friendConversations = friends(currentUserId).stream().map(friend -> {
            ConversationVO vo = new ConversationVO();
            vo.setConversationType(PRIVATE);
            vo.setTargetId(friend.getFriendId());
            vo.setTitle(displayNameWithRemark(friend.getRemark(), friend.getName(), friend.getUsername(), friend.getFriendId()));
            vo.setAvatar(friend.getAvatar());
            vo.setRemark(friend.getRemark());
            vo.setLevel(friend.getLevel());
            vo.setLevelName(friend.getLevelName());
            vo.setUnreadCount(friend.getUnreadCount());
            vo.setLastMessage(friend.getLastMessage());
            vo.setLastMessageAt(friend.getLastMessageAt());
            return vo;
        });
        Stream<ConversationVO> groupConversations = myGroups(currentUserId).stream().map(group -> {
            ConversationVO vo = new ConversationVO();
            vo.setConversationType(GROUP);
            vo.setTargetId(group.getId());
            vo.setTitle(group.getName());
            vo.setGroupNo(group.getGroupNo());
            vo.setOwnerId(group.getOwnerId());
            vo.setOwnerName(group.getOwnerName());
            vo.setRole(group.getRole());
            vo.setMutedUntil(group.getMutedUntil());
            vo.setMutedAll(group.getMutedAll());
            vo.setPinnedMessageId(group.getPinnedMessageId());
            vo.setPinnedMessage(group.getPinnedMessage());
            vo.setPinnedMessageSenderName(group.getPinnedMessageSenderName());
            vo.setUnreadCount(group.getUnreadCount());
            vo.setLastMessage(group.getLastMessage());
            vo.setLastMessageAt(group.getLastMessageAt());
            return vo;
        });
        return Stream.concat(friendConversations, groupConversations)
                .sorted(Comparator.comparing(ConversationVO::getLastMessageAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupVO> adminGroups(String keyword) {
        LambdaQueryWrapper<ChatGroup> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(ChatGroup::getName, keyword.trim())
                    .or()
                    .like(ChatGroup::getGroupNo, keyword.trim());
        }
        wrapper.orderByDesc(ChatGroup::getUpdatedAt);
        return groupMapper.selectList(wrapper).stream()
                .map(group -> toGroupVO(group, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<FriendVO> adminFriendships(String keyword) {
        LambdaQueryWrapper<ChatFriend> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            try {
                Long id = Long.parseLong(keyword.trim());
                wrapper.eq(ChatFriend::getUserId, id).or().eq(ChatFriend::getFriendId, id);
            } catch (NumberFormatException ignored) {
                return friendsByUserKeyword(keyword);
            }
        }
        wrapper.orderByDesc(ChatFriend::getUpdatedAt).last("LIMIT 200");
        return friendMapper.selectList(wrapper).stream()
                .map(this::toFriendVO)
                .collect(Collectors.toList());
    }

    private List<FriendVO> friendsByUserKeyword(String keyword) {
        return searchUsers(null, keyword).stream()
                .flatMap(user -> friendMapper.selectList(new LambdaQueryWrapper<ChatFriend>()
                        .eq(ChatFriend::getUserId, user.getId())
                        .or()
                        .eq(ChatFriend::getFriendId, user.getId())).stream())
                .map(this::toFriendVO)
                .collect(Collectors.toList());
    }

    private void upsertFriend(Long userId, Long friendId) {
        ChatFriend relation = friendMapper.selectOne(new LambdaQueryWrapper<ChatFriend>()
                .eq(ChatFriend::getUserId, userId)
                .eq(ChatFriend::getFriendId, friendId)
                .last("LIMIT 1"));
        if (relation == null) {
            relation = new ChatFriend();
            relation.setUserId(userId);
            relation.setFriendId(friendId);
            relation.setStatus(ACTIVE);
            relation.setCreatedAt(LocalDateTime.now());
            relation.setUpdatedAt(LocalDateTime.now());
            friendMapper.insert(relation);
            return;
        }
        relation.setStatus(ACTIVE);
        relation.setUpdatedAt(LocalDateTime.now());
        friendMapper.updateById(relation);
    }

    private Map<Long, MessageVO> realtimeDeliveries(ChatMessage message) {
        Set<Long> recipients = new HashSet<>();
        if (GROUP.equals(message.getConversationType())) {
            memberMapper.selectList(new LambdaQueryWrapper<ChatGroupMember>()
                            .eq(ChatGroupMember::getGroupId, message.getGroupId())
                            .eq(ChatGroupMember::getStatus, ACTIVE))
                    .stream()
                    .map(ChatGroupMember::getUserId)
                    .filter(Objects::nonNull)
                    .forEach(recipients::add);
        } else {
            recipients.add(message.getSenderId());
            recipients.add(message.getReceiverId());
        }
        return recipients.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        userId -> userId,
                        userId -> toMessageVO(message, userId),
                        (first, second) -> first,
                        LinkedHashMap::new));
    }

    private void requireFriend(Long currentUserId, Long friendId) {
        if (friendId == null) {
            throw new IllegalArgumentException("好友不能为空");
        }
        Long count = friendMapper.selectCount(new LambdaQueryWrapper<ChatFriend>()
                .eq(ChatFriend::getUserId, currentUserId)
                .eq(ChatFriend::getFriendId, friendId)
                .eq(ChatFriend::getStatus, ACTIVE));
        if (count == 0) {
            throw new IllegalArgumentException("请先添加好友");
        }
    }

    private ChatGroupMember requireGroupMember(Long groupId, Long userId) {
        if (groupId == null) {
            throw new IllegalArgumentException("群聊不能为空");
        }
        Long count = memberMapper.selectCount(new LambdaQueryWrapper<ChatGroupMember>()
                .eq(ChatGroupMember::getGroupId, groupId)
                .eq(ChatGroupMember::getUserId, userId)
                .eq(ChatGroupMember::getStatus, ACTIVE));
        if (count == 0) {
            throw new IllegalArgumentException("未加入该群聊");
        }
        return findGroupMember(groupId, userId);
    }

    private ChatGroupMember findGroupMember(Long groupId, Long userId) {
        return memberMapper.selectOne(new LambdaQueryWrapper<ChatGroupMember>()
                .eq(ChatGroupMember::getGroupId, groupId)
                .eq(ChatGroupMember::getUserId, userId)
                .last("LIMIT 1"));
    }

    private ChatGroup requireGroup(Long groupId) {
        ChatGroup group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group does not exist");
        }
        return group;
    }

    private ChatGroup requireGroupOwner(Long groupId, Long userId) {
        ChatGroup group = requireGroup(groupId);
        if (!Objects.equals(group.getOwnerId(), userId)) {
            throw new IllegalArgumentException("Only the group owner can perform this action");
        }
        return group;
    }

    private ChatGroupMember requireGroupManager(Long groupId, Long userId) {
        ChatGroupMember member = requireGroupMember(groupId, userId);
        if (!OWNER.equals(member.getRole()) && !ADMIN.equals(member.getRole())) {
            throw new IllegalArgumentException("Only the group owner or administrators can perform this action");
        }
        return member;
    }

    private void assertCanManageTarget(ChatGroup group, ChatGroupMember operator, ChatGroupMember target) {
        if (target == null || !ACTIVE.equals(target.getStatus())) {
            throw new IllegalArgumentException("Group member does not exist");
        }
        if (Objects.equals(group.getOwnerId(), target.getUserId())) {
            throw new IllegalArgumentException("The group owner cannot be managed");
        }
        if (ADMIN.equals(operator.getRole()) && ADMIN.equals(target.getRole())) {
            throw new IllegalArgumentException("Administrators cannot manage other administrators");
        }
    }

    private void requireGroupCanSend(Long groupId, Long userId) {
        ChatGroupMember member = requireGroupMember(groupId, userId);
        if ("OWNER".equals(member.getRole())) {
            return;
        }
        ChatGroup group = groupMapper.selectById(groupId);
        if (group != null && Boolean.TRUE.equals(group.getMutedAll())) {
            throw new IllegalArgumentException("The group is muted by its owner");
        }
        if (member.getMutedUntil() != null && member.getMutedUntil().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("You are muted in this group");
        }
    }

    private void refreshMemberCount(ChatGroup group) {
        group.setMemberCount(memberMapper.selectCount(new LambdaQueryWrapper<ChatGroupMember>()
                .eq(ChatGroupMember::getGroupId, group.getId())
                .eq(ChatGroupMember::getStatus, ACTIVE)).intValue());
        group.setUpdatedAt(LocalDateTime.now());
        groupMapper.updateById(group);
    }

    private GroupMemberVO toGroupMemberVO(ChatGroupMember member) {
        GroupMemberVO vo = new GroupMemberVO();
        vo.setUserId(member.getUserId());
        vo.setRole(member.getRole());
        vo.setMutedUntil(member.getMutedUntil());
        try {
            ActivitySummaryVO summary = activityService.summary(member.getUserId());
            vo.setLevel(summary.getLevel());
            vo.setLevelName(summary.getLevelName());
            vo.setLevelProgress(summary.getLevelProgress());
        } catch (Exception ignored) {
            vo.setLevel(1);
            vo.setLevelName("Newcomer");
            vo.setLevelProgress(0);
        }
        UserVO user = loadUser(member.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setName(user.getName());
            vo.setAvatar(user.getAvatar());
        }
        return vo;
    }
    private List<ChatMessage> resolveMessagesForRead(Long currentUserId, ReadMessageDTO dto) {
        String type = normalizeType(dto.getConversationType());
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getConversationType, type)
                .isNull(ChatMessage::getRecalledAt);
        if (dto.getMessageIds() != null && !dto.getMessageIds().isEmpty()) {
            wrapper.in(ChatMessage::getId, dto.getMessageIds());
        }
        if (GROUP.equals(type)) {
            requireGroupMember(dto.getGroupId(), currentUserId);
            wrapper.eq(ChatMessage::getGroupId, dto.getGroupId());
        } else {
            requireFriend(currentUserId, dto.getFriendId());
            wrapper.and(q -> q.eq(ChatMessage::getSenderId, dto.getFriendId()).eq(ChatMessage::getReceiverId, currentUserId)
                    .or()
                    .eq(ChatMessage::getSenderId, currentUserId).eq(ChatMessage::getReceiverId, dto.getFriendId()));
        }
        return messageMapper.selectList(wrapper);
    }

    private void markMessageRead(Long messageId, Long userId) {
        Long existing = readMapper.selectCount(new LambdaQueryWrapper<ChatMessageRead>()
                .eq(ChatMessageRead::getMessageId, messageId)
                .eq(ChatMessageRead::getUserId, userId));
        if (existing > 0) {
            return;
        }
        ChatMessageRead read = new ChatMessageRead();
        read.setMessageId(messageId);
        read.setUserId(userId);
        read.setReadAt(LocalDateTime.now());
        readMapper.insert(read);
    }

    private FriendVO toFriendVO(ChatFriend relation) {
        FriendVO vo = new FriendVO();
        vo.setId(relation.getId());
        vo.setUserId(relation.getUserId());
        vo.setFriendId(relation.getFriendId());
        vo.setRemark(relation.getRemark());
        vo.setStatus(relation.getStatus());
        UserVO user = loadUser(relation.getFriendId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setName(user.getName());
            vo.setAvatar(user.getAvatar());
        }
        fillFriendLevel(vo);
        ChatMessage latest = latestPrivateMessage(relation.getUserId(), relation.getFriendId());
        if (latest != null) {
            vo.setLastMessage(messagePreview(latest));
            vo.setLastMessageAt(latest.getCreatedAt());
            vo.setLastMessageRead(readMapper.selectCount(new LambdaQueryWrapper<ChatMessageRead>()
                    .eq(ChatMessageRead::getMessageId, latest.getId())
                    .eq(ChatMessageRead::getUserId, relation.getFriendId())) > 0);
        }
        vo.setUnreadCount(unreadPrivateCount(relation.getUserId(), relation.getFriendId()));
        return vo;
    }

    private GroupVO toGroupVO(ChatGroup group, Long userId) {
        GroupVO vo = new GroupVO();
        MyBeanUtils.copyNonNullProperties(group, vo);
        if (userId != null) {
            ChatGroupMember member = memberMapper.selectOne(new LambdaQueryWrapper<ChatGroupMember>()
                    .eq(ChatGroupMember::getGroupId, group.getId())
                    .eq(ChatGroupMember::getUserId, userId)
                    .last("LIMIT 1"));
            vo.setJoined(member != null && ACTIVE.equals(member.getStatus()));
            vo.setRole(member == null ? null : member.getRole());
            vo.setMutedUntil(member == null ? null : member.getMutedUntil());
            vo.setUnreadCount(unreadGroupCount(userId, group.getId()));
        } else {
            vo.setJoined(false);
            vo.setUnreadCount(0);
        }
        UserVO owner = loadUser(group.getOwnerId());
        if (owner != null) {
            vo.setOwnerName(displayName(owner.getName(), owner.getUsername(), owner.getId()));
            vo.setOwnerAvatar(owner.getAvatar());
        }
        if (group.getPinnedMessageId() != null) {
            ChatMessage pinned = messageMapper.selectById(group.getPinnedMessageId());
            if (pinned != null && pinned.getRecalledAt() == null) {
                vo.setPinnedMessage(messagePreview(pinned));
                UserVO pinnedSender = loadUser(pinned.getSenderId());
                vo.setPinnedMessageSenderName(pinnedSender == null ? null
                        : displayName(pinnedSender.getName(), pinnedSender.getUsername(), pinnedSender.getId()));
            }
        }
        ChatMessage latest = latestGroupMessage(group.getId());
        if (latest != null) {
            vo.setLastMessage(messagePreview(latest));
            vo.setLastMessageAt(latest.getCreatedAt());
        }
        return vo;
    }

    private MessageVO toMessageVO(ChatMessage message, Long currentUserId) {
        MessageVO vo = new MessageVO();
        MyBeanUtils.copyNonNullProperties(message, vo);
        if (GROUP.equals(message.getConversationType()) && message.getGroupId() != null) {
            ChatGroupMember senderMember = findGroupMember(message.getGroupId(), message.getSenderId());
            vo.setSenderRole(senderMember == null ? null : senderMember.getRole());
        }
        UserVO sender = loadUser(message.getSenderId());
        if (sender != null) {
            vo.setSenderName(displayName(sender.getName(), sender.getUsername(), sender.getId()));
            vo.setSenderAvatar(sender.getAvatar());
        }
        vo.setReadByMe(readMapper.selectCount(new LambdaQueryWrapper<ChatMessageRead>()
                .eq(ChatMessageRead::getMessageId, message.getId())
                .eq(ChatMessageRead::getUserId, currentUserId)) > 0);
        vo.setMine(Objects.equals(message.getSenderId(), currentUserId));
        int readCount = readMapper.selectCount(new LambdaQueryWrapper<ChatMessageRead>()
                .eq(ChatMessageRead::getMessageId, message.getId())
                .ne(ChatMessageRead::getUserId, message.getSenderId())).intValue();
        vo.setReadCount(readCount);
        if (GROUP.equals(message.getConversationType()) && message.getGroupId() != null) {
            ChatGroup group = groupMapper.selectById(message.getGroupId());
            vo.setUnreadCount(Math.max(0, value(group == null ? null : group.getMemberCount()) - 1 - readCount));
        } else {
            vo.setUnreadCount(readCount >= 1 ? 0 : 1);
        }
        return vo;
    }

    private ChatMessage latestPrivateMessage(Long userId, Long friendId) {
        return messageMapper.selectOne(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getConversationType, PRIVATE)
                .isNull(ChatMessage::getRecalledAt)
                .and(q -> q.eq(ChatMessage::getSenderId, userId).eq(ChatMessage::getReceiverId, friendId)
                        .or()
                        .eq(ChatMessage::getSenderId, friendId).eq(ChatMessage::getReceiverId, userId))
                .orderByDesc(ChatMessage::getId)
                .last("LIMIT 1"));
    }

    private ChatMessage latestGroupMessage(Long groupId) {
        return messageMapper.selectOne(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getConversationType, GROUP)
                .eq(ChatMessage::getGroupId, groupId)
                .isNull(ChatMessage::getRecalledAt)
                .orderByDesc(ChatMessage::getId)
                .last("LIMIT 1"));
    }

    private Integer unreadPrivateCount(Long userId, Long friendId) {
        List<ChatMessage> messages = messageMapper.selectList(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getConversationType, PRIVATE)
                .eq(ChatMessage::getSenderId, friendId)
                .eq(ChatMessage::getReceiverId, userId)
                .isNull(ChatMessage::getRecalledAt));
        return (int) messages.stream()
                .filter(message -> readMapper.selectCount(new LambdaQueryWrapper<ChatMessageRead>()
                        .eq(ChatMessageRead::getMessageId, message.getId())
                        .eq(ChatMessageRead::getUserId, userId)) == 0)
                .count();
    }

    private Integer unreadGroupCount(Long userId, Long groupId) {
        ChatGroupMember member = memberMapper.selectOne(new LambdaQueryWrapper<ChatGroupMember>()
                .eq(ChatGroupMember::getGroupId, groupId)
                .eq(ChatGroupMember::getUserId, userId)
                .last("LIMIT 1"));
        if (member == null) {
            return 0;
        }
        List<ChatMessage> messages = messageMapper.selectList(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getConversationType, GROUP)
                .eq(ChatMessage::getGroupId, groupId)
                .ne(ChatMessage::getSenderId, userId)
                .isNull(ChatMessage::getRecalledAt));
        return (int) messages.stream()
                .filter(message -> readMapper.selectCount(new LambdaQueryWrapper<ChatMessageRead>()
                        .eq(ChatMessageRead::getMessageId, message.getId())
                        .eq(ChatMessageRead::getUserId, userId)) == 0)
                .count();
    }

    private UserVO loadUser(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            RestBean<UserVO> result = userServiceClient.getAuthorInfo(userId);
            return result != null && result.getCode() == 200 ? result.getData() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private ChatUserVO toChatUser(UserVO user) {
        ChatUserVO vo = new ChatUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setName(user.getName());
        vo.setAvatar(user.getAvatar());
        return vo;
    }

    private String normalizeType(String type) {
        return GROUP.equalsIgnoreCase(type) ? GROUP : PRIVATE;
    }

    private String generateGroupNo() {
        String groupNo;
        do {
            groupNo = String.valueOf(100000 + new Random().nextInt(900000));
        } while (groupMapper.selectCount(new LambdaQueryWrapper<ChatGroup>().eq(ChatGroup::getGroupNo, groupNo)) > 0);
        return groupNo;
    }

    private void fillFriendLevel(FriendVO vo) {
        try {
            ActivitySummaryVO summary = activityService.summary(vo.getFriendId());
            vo.setLevel(summary.getLevel());
            vo.setLevelName(summary.getLevelName());
        } catch (Exception ignored) {
            vo.setLevel(1);
            vo.setLevelName("Newcomer");
        }
    }

    private String messagePreview(ChatMessage message) {
        if ("FILE".equalsIgnoreCase(message.getContentType())) {
            return "[File]";
        }
        if ("IMAGE".equalsIgnoreCase(message.getContentType())) {
            return "[Image]";
        }
        return message.getContent();
    }

    private String displayNameWithRemark(String remark, String name, String username, Long id) {
        return StringUtils.hasText(remark) ? remark : displayName(name, username, id);
    }

    private String displayName(String name, String username, Long id) {
        if (StringUtils.hasText(name)) {
            return name;
        }
        if (StringUtils.hasText(username)) {
            return username;
        }
        return "用户" + id;
    }

    private int value(Integer value) {
        return value == null ? 0 : value;
    }
}
