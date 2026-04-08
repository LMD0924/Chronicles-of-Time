package org.example.generalservice.controller.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.commondb.utils.RestBean;
import org.example.generalservice.dto.content.ContentSaveDTO;
import org.example.generalservice.entity.content.Comment;
import org.example.generalservice.entity.content.Content;
import org.example.generalservice.service.content.ICommentService;
import org.example.generalservice.service.content.IContentService;
import org.example.generalservice.service.content.IFavoriteService;
import org.example.generalservice.service.content.ILikeService;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 内容管理控制器
 * 统一管理文章、日记、随笔等内容
 *
 * @author 总会落叶
 * @date 2026-04-06
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

    /**
     * 从请求头中获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) return null;
        try {
            return Long.parseLong(userIdStr);
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== 内容管理 ====================

    /**
     * 保存内容（新增或更新）
     * 图片URL由上传模块返回后传入
     */
    @PostMapping("/save")
    public RestBean<Long> saveContent(@RequestBody ContentSaveDTO dto, 
                                       @RequestAttribute(required = false) Long userId,
                                       HttpServletRequest request) {
        log.info("========== 保存内容 ==========");
        try {
            Long currentUserId = userId != null ? userId : getCurrentUserId(request);
            if (currentUserId == null) {
                return RestBean.fail("用户未登录");
            }
            Long contentId = contentService.saveContent(dto, currentUserId);
            return RestBean.success(contentId);
        } catch (Exception e) {
            log.error("保存内容失败", e);
            return RestBean.fail(e.getMessage());
        }
    }

    /**
     * 获取内容详情
     */
    @GetMapping("/detail/{id}")
    public RestBean<Content> getContentDetail(@PathVariable Long id, 
                                               @RequestAttribute(required = false) Long userId,
                                               HttpServletRequest request) {
        log.info("========== 获取内容详情 ==========");
        try {
            Long currentUserId = userId != null ? userId : getCurrentUserId(request);
            Content content = contentService.getContentDetail(id, currentUserId);
            if (content != null) {
                return RestBean.success(content);
            }
            return RestBean.fail("内容不存在");
        } catch (Exception e) {
            log.error("获取内容详情失败", e);
            return RestBean.fail(e.getMessage());
        }
    }

    /**
     * 分页查询公开内容
     */
    @GetMapping("/public/list")
    public RestBean<com.baomidou.mybatisplus.extension.plugins.pagination.Page<Content>> getPublicContents(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String contentType,
            @RequestAttribute(required = false) Long userId,
            HttpServletRequest request) {
        log.info("========== 查询公开内容 ==========");
        try {
            Long currentUserId = userId != null ? userId : getCurrentUserId(request);
            var page = contentService.getPublicContents(pageNum, pageSize, category, contentType, currentUserId);
            return RestBean.success(page);
        } catch (Exception e) {
            log.error("查询公开内容失败", e);
            return RestBean.fail(e.getMessage());
        }
    }

    /**
     * 查询当前登录用户的内容列表
     */
    @GetMapping("/my/list")
    public RestBean<com.baomidou.mybatisplus.extension.plugins.pagination.Page<Content>> getMyContents(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String contentType,
            @RequestParam(required = false) String keyword,
            @RequestAttribute(required = false) Long userId,
            HttpServletRequest request) {
        log.info("========== 查询我的内容 ==========");
        try {
            Long currentUserId = userId != null ? userId : getCurrentUserId(request);
            if (currentUserId == null) {
                return RestBean.fail("用户未登录");
            }
            var page = contentService.getMyContents(currentUserId, pageNum, pageSize, category, contentType, keyword);
            return RestBean.success(page);
        } catch (Exception e) {
            log.error("查询我的内容失败", e);
            return RestBean.fail(e.getMessage());
        }
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

    // ==================== 评论功能 ====================

    /**
     * 添加评论
     */
    @PostMapping("/comment/add")
    public RestBean<String> addComment(@RequestBody Comment comment, 
                                       @RequestAttribute(required = false) Long userId,
                                       HttpServletRequest request) {
        log.info("========== 添加评论 ==========");
        try {
            Long currentUserId = userId != null ? userId : getCurrentUserId(request);
            if (currentUserId == null) {
                return RestBean.fail("用户未登录");
            }
            comment.setUserId(currentUserId);
            if (commentService.addComment(comment)) {
                return RestBean.success("评论成功");
            }
            return RestBean.fail("评论失败");
        } catch (Exception e) {
            log.error("添加评论失败", e);
            return RestBean.fail(e.getMessage());
        }
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/comment/delete/{id}")
    public RestBean<String> deleteComment(@PathVariable Long id, 
                                          @RequestAttribute(required = false) Long userId,
                                          HttpServletRequest request) {
        log.info("========== 删除评论 ==========");
        try {
            Long currentUserId = userId != null ? userId : getCurrentUserId(request);
            if (currentUserId == null) {
                return RestBean.fail("用户未登录");
            }
            if (commentService.deleteComment(id, currentUserId)) {
                return RestBean.success("删除成功");
            }
            return RestBean.fail("删除失败");
        } catch (Exception e) {
            log.error("删除评论失败", e);
            return RestBean.fail(e.getMessage());
        }
    }

    /**
     * 获取内容的评论列表
     */
    @GetMapping("/comment/list/{contentId}")
    public RestBean<List<Comment>> getCommentList(@PathVariable Long contentId) {
        log.info("========== 获取评论列表 ==========");
        try {
            List<Comment> comments = commentService.getCommentList(contentId);
            return RestBean.success(comments);
        } catch (Exception e) {
            log.error("获取评论列表失败", e);
            return RestBean.fail(e.getMessage());
        }
    }
}