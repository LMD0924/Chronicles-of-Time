/**
 * 文件说明：拾光记微服务后端通用内容服务内容社区源码，负责内容社区相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.generalservice.service.content.Impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.commondb.utils.RestBean;
import org.example.generalservice.client.UserServiceClient;
import org.example.generalservice.entity.content.Comment;
import org.example.generalservice.entity.content.LikeRecord;
import org.example.generalservice.mapper.content.CommentMapper;
import org.example.generalservice.mapper.content.LikeRecordMapper;
import org.example.generalservice.mapper.content.ContentMapper;
import org.example.generalservice.service.content.ICommentService;
import org.example.generalservice.vo.UserVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 类说明：当前类是内容社区模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DS("cot_content")
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    private final CommentMapper commentMapper;
    private final ContentMapper contentMapper;
    private final LikeRecordMapper likeRecordMapper;
    private final UserServiceClient userServiceClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addComment(Comment comment) {
        log.info("添加评论: userId={}, contentId={}", comment.getUserId(), comment.getContentId());

        Long parentId = comment.getParentId();
        if (parentId == null || parentId == 0L) {
            comment.setParentId(0L);
            comment.setReplyToUserId(null);
        } else {
            Comment parent = getById(parentId);
            if (parent == null || !Integer.valueOf(1).equals(parent.getStatus())
                    || !comment.getContentId().equals(parent.getContentId())) {
                throw new IllegalArgumentException("Reply target does not exist");
            }
            if (comment.getUserId().equals(parent.getUserId())) {
                throw new IllegalArgumentException("You cannot reply to your own comment");
            }
            comment.setReplyToUserId(parent.getUserId());
            comment.setParentId(parent.getParentId() == null || parent.getParentId() == 0L ? parent.getId() : parent.getParentId());
        }
        if (comment.getLikesCount() == null) {
            comment.setLikesCount(0L);
        }
        if (comment.getStatus() == null) {
            comment.setStatus(1);
        }
        if (comment.getCreateTime() == null) {
            comment.setCreateTime(LocalDateTime.now());
        }

        boolean saved = save(comment);

        if (saved) {
            contentMapper.incrementCommentsCount(comment.getContentId());
        }

        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteComment(Long id, Long userId) {
        log.info("删除评论: id={}, userId={}", id, userId);

        Comment comment = getById(id);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此评论");
        }

        comment.setStatus(0);
        boolean removed = updateById(comment);

        if (removed) {
            contentMapper.decrementCommentsCount(comment.getContentId());
        }

        return removed;
    }

    // ====================== 🔥 修复这里：一次性查询所有评论，构建完整树 ======================
    @Override
    public List<Comment> getCommentList(Long contentId, Long userId) {
        log.info("获取评论列表（完整树）: contentId={}", contentId);

        // 1. 一次性查出该文章所有有效评论
        List<Comment> allComments = lambdaQuery()
                .eq(Comment::getContentId, contentId)
                .eq(Comment::getStatus, 1)
                .list();
        Set<Long> likedCommentIds = new HashSet<>();
        if (userId != null && !allComments.isEmpty()) {
            List<LikeRecord> likeRecords = likeRecordMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<LikeRecord>()
                    .eq(LikeRecord::getUserId, userId)
                    .eq(LikeRecord::getBizType, "comment")
                    .eq(LikeRecord::getReactionType, "like")
                    .in(LikeRecord::getContentId, allComments.stream().map(Comment::getId).toList()));
            for (LikeRecord likeRecord : likeRecords) {
                likedCommentIds.add(likeRecord.getContentId());
            }
        }

        populateUserInfo(allComments);

        // 2. 构建评论树（不会丢任何一条）
        Map<Long, Comment> map = new HashMap<>();
        List<Comment> roots = new ArrayList<>();

        // 先全部放入map
        for (Comment c : allComments) {
            c.setChildren(new ArrayList<>());
            c.setLiked(likedCommentIds.contains(c.getId()));
            map.put(c.getId(), c);
        }

        // 构建父子关系
        for (Comment c : allComments) {
            if (c.getParentId() == 0) {
                roots.add(c);
            } else {
                Comment parent = map.get(c.getParentId());
                if (parent != null) {
                    parent.getChildren().add(c);
                }
            }
        }

        return roots;
    }

    private void populateUserInfo(List<Comment> comments) {
        Map<Long, UserVO> users = new HashMap<>();
        Set<Long> userIds = new HashSet<>();
        for (Comment comment : comments) {
            if (comment.getUserId() != null) {
                userIds.add(comment.getUserId());
            }
            if (comment.getReplyToUserId() != null) {
                userIds.add(comment.getReplyToUserId());
            }
        }
        for (Long userId : userIds) {
            try {
                RestBean<UserVO> result = userServiceClient.getAuthorInfo(userId);
                if (result != null && result.getCode() == 200 && result.getData() != null) {
                    users.put(userId, result.getData());
                }
            } catch (Exception exception) {
                log.warn("Failed to load comment user: userId={}", userId);
            }
        }
        for (Comment comment : comments) {
            UserVO author = users.get(comment.getUserId());
            comment.setUserName(displayName(author, comment.getUserId()));
            comment.setUserAvatar(author == null ? null : author.getAvatar());
            if (comment.getReplyToUserId() != null) {
                comment.setReplyToUserName(displayName(users.get(comment.getReplyToUserId()), comment.getReplyToUserId()));
            }
        }
    }

    private String displayName(UserVO user, Long userId) {
        if (user != null && user.getName() != null && !user.getName().isBlank()) {
            return user.getName();
        }
        if (user != null && user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername();
        }
        return userId == null ? "User" : "User " + userId;
    }
    @Override
    public Page<Comment> getCommentPage(Long contentId, Integer pageNum, Integer pageSize) {
        log.info("分页获取评论: contentId={}, pageNum={}, pageSize={}", contentId, pageNum, pageSize);

        Page<Comment> page = new Page<>(pageNum, pageSize);
        lambdaQuery()
                .eq(Comment::getContentId, contentId)
                .eq(Comment::getParentId, 0)
                .eq(Comment::getStatus, 1)
                .orderByDesc(Comment::getCreateTime)
                .page(page);

        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likeComment(Long commentId, Long userId) {
        Comment comment = getById(commentId);
        if (comment == null || !Integer.valueOf(1).equals(comment.getStatus())) {
            throw new IllegalArgumentException("Comment does not exist");
        }
        if (isCommentLiked(commentId, userId)) {
            return false;
        }
        LikeRecord likeRecord = new LikeRecord();
        likeRecord.setUserId(userId);
        likeRecord.setContentId(commentId);
        likeRecord.setBizType("comment");
        likeRecord.setReactionType("like");
        likeRecord.setCreateTime(LocalDateTime.now());
        if (likeRecordMapper.insert(likeRecord) != 1 || commentMapper.incrementLikesCount(commentId) != 1) {
            throw new IllegalStateException("Failed to like comment");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unlikeComment(Long commentId, Long userId) {
        int removed = likeRecordMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<LikeRecord>()
                .eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getContentId, commentId)
                .eq(LikeRecord::getBizType, "comment")
                .eq(LikeRecord::getReactionType, "like"));
        if (removed == 0) {
            return false;
        }
        if (commentMapper.decrementLikesCount(commentId) != 1) {
            throw new IllegalStateException("Failed to unlike comment");
        }
        return true;
    }

    @Override
    public boolean isCommentLiked(Long commentId, Long userId) {
        if (userId == null) {
            return false;
        }
        return likeRecordMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<LikeRecord>()
                .eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getContentId, commentId)
                .eq(LikeRecord::getBizType, "comment")
                .eq(LikeRecord::getReactionType, "like")) > 0;
    }
}