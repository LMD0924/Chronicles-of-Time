package org.example.generalservice.service.content.Impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.generalservice.entity.content.Comment;
import org.example.generalservice.mapper.content.CommentMapper;
import org.example.generalservice.mapper.content.ContentMapper;
import org.example.generalservice.service.content.ICommentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@DS("futurestack")
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    private final CommentMapper commentMapper;
    private final ContentMapper contentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addComment(Comment comment) {
        log.info("添加评论: userId={}, contentId={}", comment.getUserId(), comment.getContentId());

        if (comment.getParentId() == null) {
            comment.setParentId(0L);
        }
        if (comment.getLikesCount() == null) {
            comment.setLikesCount(0L);
        }
        if (comment.getStatus() == null) {
            comment.setStatus(1);
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
    public List<Comment> getCommentList(Long contentId) {
        log.info("获取评论列表（完整树）: contentId={}", contentId);

        // 1. 一次性查出该文章所有有效评论
        List<Comment> allComments = lambdaQuery()
                .eq(Comment::getContentId, contentId)
                .eq(Comment::getStatus, 1)
                .list();

        // 2. 构建评论树（不会丢任何一条）
        Map<Long, Comment> map = new HashMap<>();
        List<Comment> roots = new ArrayList<>();

        // 先全部放入map
        for (Comment c : allComments) {
            c.setChildren(new ArrayList<>());
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
    public boolean likeComment(Long id) {
        log.info("点赞评论: id={}", id);
        return commentMapper.incrementLikesCount(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unlikeComment(Long id) {
        log.info("取消点赞评论: id={}", id);
        return commentMapper.decrementLikesCount(id) > 0;
    }
}