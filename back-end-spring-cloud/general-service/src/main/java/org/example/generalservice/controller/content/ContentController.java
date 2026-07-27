/**
 * 文件说明：拾光记微服务后端通用内容服务内容社区源码，负责内容社区相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.generalservice.controller.content;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.commondb.utils.RestBean;
import org.example.generalservice.dto.content.ContentSaveDTO;
import org.example.generalservice.entity.content.Comment;
import org.example.generalservice.entity.content.FavoriteRecord;
import org.example.generalservice.entity.content.LikeRecord;
import org.example.generalservice.entity.content.Content;
import org.example.generalservice.mapper.content.CommentMapper;
import org.example.generalservice.mapper.content.ContentMapper;
import org.example.generalservice.mapper.content.FavoriteRecordMapper;
import org.example.generalservice.mapper.content.LikeRecordMapper;
import org.example.generalservice.service.content.*;
import org.example.generalservice.vo.content.ContentKnowledgeGraph;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 内容管理控制器
 * 统一管理文章、日记、随笔等内容
 *
 * @author 总会落叶
 * @date 2026-04-06
 */
/**
 * 类说明：当前类是内容社区模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Slf4j
@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentController {

    private final IContentService contentService;
    private final ILikeService likeService;
    private final IFavoriteService favoriteService;
    private final ICommentService commentService;
    private final ContentMapper contentMapper;
    private final LikeRecordMapper likeRecordMapper;
    private final FavoriteRecordMapper favoriteRecordMapper;
    private final CommentMapper commentMapper;
    // 注入知识图谱服务
    private final IContentKnowledgeGraphService contentKnowledgeGraphService;

    private Long getCurrentUserId(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) {
            return null;
        }
        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    @PostMapping("/save")
    public RestBean<Long> saveContent(@RequestBody ContentSaveDTO dto,
                                      @RequestAttribute(required = false) Long userId,
                                      HttpServletRequest request) {
        try {
            Long currentUserId = userId != null ? userId : getCurrentUserId(request);
            if (currentUserId == null) {
                return RestBean.fail("User is not logged in");
            }
            return RestBean.success(contentService.saveContent(dto, currentUserId));
        } catch (Exception exception) {
            log.error("Failed to save content", exception);
            return RestBean.fail(exception.getMessage());
        }
    }

    @GetMapping("/detail/{id}")
    public RestBean<Content> getContentDetail(@PathVariable Long id,
                                              @RequestAttribute(required = false) Long userId,
                                              HttpServletRequest request) {
        try {
            Long currentUserId = userId != null ? userId : getCurrentUserId(request);
            Content content = contentService.getContentDetail(id, currentUserId);
            return content == null ? RestBean.fail("Content does not exist") : RestBean.success(content);
        } catch (Exception exception) {
            log.error("Failed to get content detail", exception);
            return RestBean.fail(exception.getMessage());
        }
    }

    @GetMapping("/public/list")
    public RestBean<Page<Content>> getPublicContents(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String contentType,
            @RequestAttribute(required = false) Long userId,
            HttpServletRequest request) {
        try {
            Long currentUserId = userId != null ? userId : getCurrentUserId(request);
            return RestBean.success(contentService.getPublicContents(pageNum, pageSize, category, contentType, currentUserId));
        } catch (Exception exception) {
            log.error("Failed to get public contents", exception);
            return RestBean.fail(exception.getMessage());
        }
    }

    @GetMapping("/my/list")
    public RestBean<Page<Content>> getMyContents(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String contentType,
            @RequestParam(required = false) String keyword,
            @RequestAttribute(required = false) Long userId,
            HttpServletRequest request) {
        try {
            Long currentUserId = userId != null ? userId : getCurrentUserId(request);
            if (currentUserId == null) {
                return RestBean.fail("User is not logged in");
            }
            return RestBean.success(contentService.getMyContents(currentUserId, pageNum, pageSize, category, contentType, keyword));
        } catch (Exception exception) {
            log.error("Failed to get user contents", exception);
            return RestBean.fail(exception.getMessage());
        }
    }
    /**
     * Lists published public articles for the timeline archive.
     */
    @GetMapping("/archive")
    public RestBean<List<Content>> getContentArchive() {
        List<Content> contents = contentMapper.selectList(new QueryWrapper<Content>()
                .eq("status", 1)
                .eq("visibility", 2)
                .orderByDesc("publish_at")
                .orderByDesc("created_at"));
        return RestBean.success(contents);
    }
@GetMapping("/my/liked")
    public RestBean<Page<Content>> getMyLikedContents(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestAttribute(required = false) Long userId,
            HttpServletRequest request) {
        Long currentUserId = userId != null ? userId : getCurrentUserId(request);
        if (currentUserId == null) {
            return RestBean.fail("用户未登录");
        }
        Page<LikeRecord> recordPage = likeRecordMapper.selectPage(new Page<>(normalizePage(pageNum), normalizePageSize(pageSize)),
                new QueryWrapper<LikeRecord>()
                        .eq("user_id", currentUserId)
                        .eq("biz_type", "article")
                        .eq("reaction_type", "like")
                        .orderByDesc("created_at"));
        return RestBean.success(buildContentPage(recordPage.getRecords().stream().map(LikeRecord::getContentId).toList(), recordPage));
    }

    /**
     * 查询当前用户收藏的文章。
     */
    @GetMapping("/my/favorited")
    public RestBean<Page<Content>> getMyFavoritedContents(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestAttribute(required = false) Long userId,
            HttpServletRequest request) {
        Long currentUserId = userId != null ? userId : getCurrentUserId(request);
        if (currentUserId == null) {
            return RestBean.fail("用户未登录");
        }
        Page<FavoriteRecord> recordPage = favoriteRecordMapper.selectPage(new Page<>(normalizePage(pageNum), normalizePageSize(pageSize)),
                new QueryWrapper<FavoriteRecord>()
                        .eq("user_id", currentUserId)
                        .orderByDesc("created_at"));
        return RestBean.success(buildContentPage(recordPage.getRecords().stream().map(FavoriteRecord::getContentId).toList(), recordPage));
    }

    /**
     * 查询当前用户发表的评论。
     */
    @GetMapping("/my/comments")
    public RestBean<Page<Comment>> getMyComments(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestAttribute(required = false) Long userId,
            HttpServletRequest request) {
        Long currentUserId = userId != null ? userId : getCurrentUserId(request);
        if (currentUserId == null) {
            return RestBean.fail("用户未登录");
        }
        Page<Comment> page = commentMapper.selectPage(new Page<>(normalizePage(pageNum), normalizePageSize(pageSize)),
                new QueryWrapper<Comment>()
                        .eq("user_id", currentUserId)
                        .eq("status", 1)
                        .orderByDesc("created_at"));
        return RestBean.success(page);
    }

    private Page<Content> buildContentPage(List<Long> contentIds, Page<?> sourcePage) {
        Page<Content> page = new Page<>(sourcePage.getCurrent(), sourcePage.getSize(), sourcePage.getTotal());
        if (contentIds.isEmpty()) {
            page.setRecords(List.of());
            return page;
        }
        HashMap<Long, Content> contentById = new HashMap<>();
        for (Content content : contentMapper.selectBatchIds(contentIds)) {
            contentById.put(content.getId(), content);
        }
        List<Content> records = new ArrayList<>();
        for (Long contentId : contentIds) {
            Content content = contentById.get(contentId);
            if (content != null) {
                records.add(content);
            }
        }
        page.setRecords(records);
        return page;
    }

    private int normalizePage(Integer pageNum) {
        return pageNum == null ? 1 : Math.max(1, pageNum);
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null ? 20 : Math.min(100, Math.max(1, pageSize));
    }
    @GetMapping("/comment/list/{contentId}")
    public RestBean<List<Comment>> getCommentList(@PathVariable Long contentId,
                                                  @RequestAttribute(required = false) Long userId,
                                                  HttpServletRequest request) {
        Long currentUserId = userId != null ? userId : getCurrentUserId(request);
        return RestBean.success(commentService.getCommentList(contentId, currentUserId));
    }

    @PostMapping("/comment/add")
    public RestBean<Comment> addComment(@RequestBody Comment comment,
                                        @RequestAttribute(required = false) Long userId,
                                        HttpServletRequest request) {
        Long currentUserId = userId != null ? userId : getCurrentUserId(request);
        if (currentUserId == null) {
            return RestBean.fail("User is not logged in");
        }
        if (comment.getContentId() == null || comment.getContent() == null || comment.getContent().isBlank()) {
            return RestBean.fail("Comment content cannot be empty");
        }
        comment.setUserId(currentUserId);
        if (!commentService.addComment(comment)) {
            return RestBean.fail("Failed to add comment");
        }
        return RestBean.success(comment);
    }

    @DeleteMapping("/comment/{id}")
    public RestBean<String> deleteComment(@PathVariable Long id,
                                          @RequestAttribute(required = false) Long userId,
                                          HttpServletRequest request) {
        Long currentUserId = userId != null ? userId : getCurrentUserId(request);
        if (currentUserId == null) {
            return RestBean.fail("User is not logged in");
        }
        if (!commentService.deleteComment(id, currentUserId)) {
            return RestBean.fail("Failed to delete comment");
        }
        return RestBean.success("Comment deleted");
    }

    @PostMapping("/comment/like")
    public RestBean<String> likeComment(@RequestParam Long commentId,
                                        @RequestAttribute(required = false) Long userId,
                                        HttpServletRequest request) {
        Long currentUserId = userId != null ? userId : getCurrentUserId(request);
        if (currentUserId == null) {
            return RestBean.fail("User is not logged in");
        }
        return commentService.likeComment(commentId, currentUserId)
                ? RestBean.success("Comment liked")
                : RestBean.fail("Comment is already liked");
    }

    @DeleteMapping("/comment/unlike")
    public RestBean<String> unlikeComment(@RequestParam Long commentId,
                                          @RequestAttribute(required = false) Long userId,
                                          HttpServletRequest request) {
        Long currentUserId = userId != null ? userId : getCurrentUserId(request);
        if (currentUserId == null) {
            return RestBean.fail("User is not logged in");
        }
        return commentService.unlikeComment(commentId, currentUserId)
                ? RestBean.success("Comment unliked")
                : RestBean.fail("Comment is not liked");
    }
    /**
     * 查询用户的内容列表
     */
    @GetMapping("/user/{userId}")
    public RestBean<com.baomidou.mybatisplus.extension.plugins.pagination.Page<Content>> getUserContents(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        log.info("========== 查询用户内容 ==========");
        try {
            var page = contentService.getUserContents(userId, pageNum, pageSize, status);
            return RestBean.success(page);
        } catch (Exception e) {
            log.error("查询用户内容失败", e);
            return RestBean.fail(e.getMessage());
        }
    }

    /**
     * 删除内容
     */
    @DeleteMapping("/delete/{id}")
    public RestBean<String> deleteContent(@PathVariable Long id,
                                          @RequestAttribute(required = false) Long userId,
                                          HttpServletRequest request) {
        log.info("========== 删除内容 ==========");
        try {
            Long currentUserId = userId != null ? userId : getCurrentUserId(request);
            if (currentUserId == null) {
                return RestBean.fail("用户未登录");
            }
            if (contentService.deleteContent(id, currentUserId)) {
                return RestBean.success("删除成功");
            }
            return RestBean.fail("删除失败");
        } catch (Exception e) {
            log.error("删除内容失败", e);
            return RestBean.fail(e.getMessage());
        }
    }

    /**
     * 获取热门内容
     */
    @GetMapping("/hot")
    public RestBean<List<Content>> getHotContents(@RequestParam(defaultValue = "10") Integer limit) {
        log.info("========== 获取热门内容 ==========");
        try {
            List<Content> hotContents = contentService.getHotContents(limit);
            return RestBean.success(hotContents);
        } catch (Exception e) {
            log.error("获取热门内容失败", e);
            return RestBean.fail(e.getMessage());
        }
    }

    /**
     * 搜索内容
     */
    @GetMapping("/search")
    public RestBean<com.baomidou.mybatisplus.extension.plugins.pagination.Page<Content>> searchContents(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("========== 搜索内容 ==========");
        try {
            var page = contentService.searchContents(keyword, pageNum, pageSize);
            return RestBean.success(page);
        } catch (Exception e) {
            log.error("搜索内容失败", e);
            return RestBean.fail(e.getMessage());
        }
    }

    // ==================== 点赞功能 ====================

    /**
     * 点赞
     */
    @PostMapping("/like")
    public RestBean<String> like(@RequestParam Long contentId,
                                 @RequestAttribute(required = false) Long userId,
                                 HttpServletRequest request) {
        log.info("========== 点赞 ==========");
        try {
            Long currentUserId = userId != null ? userId : getCurrentUserId(request);
            if (currentUserId == null) {
                return RestBean.fail("用户未登录");
            }
            if (likeService.like(currentUserId, contentId)) {
                return RestBean.success("点赞成功");
            }
            return RestBean.fail("您已经点过赞了");
        } catch (Exception e) {
            log.error("点赞失败", e);
            return RestBean.fail(e.getMessage());
        }
    }

    /**
     * 取消点赞
     */
    @DeleteMapping("/unlike")
    public RestBean<String> unlike(@RequestParam Long contentId,
                                   @RequestAttribute(required = false) Long userId,
                                   HttpServletRequest request) {
        log.info("========== 取消点赞 ==========");
        try {
            Long currentUserId = userId != null ? userId : getCurrentUserId(request);
            if (currentUserId == null) {
                return RestBean.fail("用户未登录");
            }
            if (likeService.unlike(currentUserId, contentId)) {
                return RestBean.success("取消点赞成功");
            }
            return RestBean.fail("取消点赞失败");
        } catch (Exception e) {
            log.error("取消点赞失败", e);
            return RestBean.fail(e.getMessage());
        }
    }

    /**
     * 检查是否已点赞
     */
    @GetMapping("/isLiked")
    public RestBean<Boolean> isLiked(@RequestParam Long contentId,
                                     @RequestAttribute(required = false) Long userId,
                                     HttpServletRequest request) {
        log.info("========== 检查点赞状态 ==========");
        try {
            Long currentUserId = userId != null ? userId : getCurrentUserId(request);
            if (currentUserId == null) {
                return RestBean.success(false);
            }
            boolean liked = likeService.isLiked(currentUserId, contentId);
            return RestBean.success(liked);
        } catch (Exception e) {
            log.error("检查点赞状态失败", e);
            return RestBean.fail("检查点赞状态失败");
        }
    }

    // ==================== 收藏功能 ====================

    /**
     * 收藏
     */
    @PostMapping("/favorite")
    public RestBean<String> favorite(@RequestParam Long contentId,
                                     @RequestAttribute(required = false) Long userId,
                                     HttpServletRequest request) {
        log.info("========== 收藏 ==========");
        try {
            Long currentUserId = userId != null ? userId : getCurrentUserId(request);
            if (currentUserId == null) {
                return RestBean.fail("用户未登录");
            }
            if (favoriteService.favorite(currentUserId, contentId)) {
                return RestBean.success("收藏成功");
            }
            return RestBean.fail("您已经收藏过了");
        } catch (Exception e) {
            log.error("收藏失败", e);
            return RestBean.fail(e.getMessage());
        }
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/unfavorite")
    public RestBean<String> unfavorite(@RequestParam Long contentId,
                                       @RequestAttribute(required = false) Long userId,
                                       HttpServletRequest request) {
        log.info("========== 取消收藏 ==========");
        try {
            Long currentUserId = userId != null ? userId : getCurrentUserId(request);
            if (currentUserId == null) {
                return RestBean.fail("用户未登录");
            }
            if (favoriteService.unfavorite(currentUserId, contentId)) {
                return RestBean.success("取消收藏成功");
            }
            return RestBean.fail("取消收藏失败");
        } catch (Exception e) {
            log.error("取消收藏失败", e);
            return RestBean.fail(e.getMessage());
        }
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/isFavorited")
    public RestBean<Boolean> isFavorited(@RequestParam Long contentId,
                                         @RequestAttribute(required = false) Long userId,
                                         HttpServletRequest request) {
        log.info("========== 检查收藏状态 ==========");
        try {
            Long currentUserId = userId != null ? userId : getCurrentUserId(request);
            if (currentUserId == null) {
                return RestBean.success(false);
            }
            boolean favorited = favoriteService.isFavorited(currentUserId, contentId);
            return RestBean.success(favorited);
        } catch (Exception e) {
            log.error("检查收藏状态失败", e);
            return RestBean.fail("检查收藏状态失败");
        }
    }
    // ==================== 知识图谱接口（需要传入 userId） ====================

    /**
     * 获取用户自己的文章知识图谱
     */
    @GetMapping("/knowledge-graph")
    public RestBean<ContentKnowledgeGraph> getContentKnowledgeGraph(
            @RequestParam(defaultValue = "50") Integer limit,
            @RequestAttribute(required = false) Long userId,
            HttpServletRequest request) {
        log.info("获取用户文章知识图谱: limit={}", limit);
        try {
            Long currentUserId = userId != null ? userId : getCurrentUserId(request);
            if (currentUserId == null) {
                return RestBean.fail("用户未登录");
            }
            ContentKnowledgeGraph graph = contentKnowledgeGraphService.getContentKnowledgeGraph(currentUserId, limit);
            return RestBean.success(graph);
        } catch (Exception e) {
            log.error("获取文章知识图谱失败", e);
            return RestBean.fail(e.getMessage());
        }
    }

    /**
     * 获取用户自己的标签云
     */
    @GetMapping("/tag-cloud")
    public RestBean<List<Map<String, Object>>> getTagCloud(
            @RequestParam(defaultValue = "30") Integer limit,
            @RequestAttribute(required = false) Long userId,
            HttpServletRequest request) {
        log.info("获取用户标签云: limit={}", limit);
        try {
            Long currentUserId = userId != null ? userId : getCurrentUserId(request);
            if (currentUserId == null) {
                return RestBean.fail("用户未登录");
            }
            List<Map<String, Object>> tagCloud = contentKnowledgeGraphService.getTagCloud(currentUserId, limit);
            return RestBean.success(tagCloud);
        } catch (Exception e) {
            log.error("获取标签云失败", e);
            return RestBean.fail(e.getMessage());
        }
    }

    /**
     * 获取用户自己的分类统计
     */
    @GetMapping("/category-stats")
    public RestBean<List<Map<String, Object>>> getCategoryStatistics(
            @RequestAttribute(required = false) Long userId,
            HttpServletRequest request) {
        log.info("获取用户分类统计");
        try {
            Long currentUserId = userId != null ? userId : getCurrentUserId(request);
            if (currentUserId == null) {
                return RestBean.fail("用户未登录");
            }
            List<Map<String, Object>> stats = contentKnowledgeGraphService.getCategoryStatistics(currentUserId);
            return RestBean.success(stats);
        } catch (Exception e) {
            log.error("获取分类统计失败", e);
            return RestBean.fail(e.getMessage());
        }
    }

    /**
     * 获取用户自己的标签共现网络
     */
    @GetMapping("/tag-cooccurrence")
    public RestBean<ContentKnowledgeGraph> getTagCooccurrenceGraph(
            @RequestParam(defaultValue = "30") Integer limit,
            @RequestAttribute(required = false) Long userId,
            HttpServletRequest request) {
        log.info("获取用户标签共现网络: limit={}", limit);
        try {
            Long currentUserId = userId != null ? userId : getCurrentUserId(request);
            if (currentUserId == null) {
                return RestBean.fail("用户未登录");
            }
            ContentKnowledgeGraph graph = contentKnowledgeGraphService.getTagCooccurrenceGraph(currentUserId, limit);
            return RestBean.success(graph);
        } catch (Exception e) {
            log.error("获取标签共现网络失败", e);
            return RestBean.fail(e.getMessage());
        }
    }

    /**
     * 根据标签推荐相关内容（用户自己的文章）
     */
    @GetMapping("/related-by-tag")
    public RestBean<List<Map<String, Object>>> getRelatedContentsByTag(
            @RequestParam String tag,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestAttribute(required = false) Long userId,
            HttpServletRequest request) {
        log.info("根据标签推荐文章: tag={}, limit={}", tag, limit);
        try {
            Long currentUserId = userId != null ? userId : getCurrentUserId(request);
            if (currentUserId == null) {
                return RestBean.fail("用户未登录");
            }
            List<Map<String, Object>> contents = contentKnowledgeGraphService.getRelatedContentsByTag(currentUserId, tag, limit);
            return RestBean.success(contents);
        } catch (Exception e) {
            log.error("根据标签推荐文章失败", e);
            return RestBean.fail(e.getMessage());
        }
    }

    /**
     * 获取用户的内容主题分布
     */
    @GetMapping("/user-topics/{userId}")
    public RestBean<Map<String, Object>> getUserContentTopics(@PathVariable Long userId) {
        log.info("获取用户内容主题分布: userId={}", userId);
        try {
            Map<String, Object> topics = contentKnowledgeGraphService.getUserContentTopics(userId);
            return RestBean.success(topics);
        } catch (Exception e) {
            log.error("获取用户内容主题分布失败", e);
            return RestBean.fail(e.getMessage());
        }
    }
}