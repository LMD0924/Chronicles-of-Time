package org.example.generalservice.service.Impl.content;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.commondb.utils.RestBean;
import org.example.generalservice.client.UserServiceClient;
import org.example.generalservice.dto.content.ContentSaveDTO;
import org.example.generalservice.entity.content.Content;
import org.example.generalservice.mapper.content.ContentMapper;
import org.example.generalservice.service.content.IContentService;
import org.example.generalservice.service.content.IFavoriteService;
import org.example.generalservice.service.content.ILikeService;
import org.example.generalservice.vo.UserVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.hutool.core.util.StrUtil;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 内容服务实现类
 *
 * @author 总会落叶
 * @date 2026-04-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentServiceImpl extends ServiceImpl<ContentMapper, Content> implements IContentService {

    private final ContentMapper contentMapper;
    private final ObjectMapper objectMapper;
    private final UserServiceClient userServiceClient;
    private final ILikeService likeService;
    private final IFavoriteService favoriteService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DS("futurestack")
    public Long saveContent(ContentSaveDTO dto, Long userId) {
        log.info("保存内容: userId={}, title={}", userId, dto.getTitle());

        Content content;
        if (dto.getId() != null && dto.getId() > 0) {
            // 更新操作
            content = getById(dto.getId());
            if (content == null) {
                throw new RuntimeException("内容不存在");
            }
            // 验证权限
            if (!content.getUserId().equals(userId)) {
                throw new RuntimeException("无权修改此内容");
            }
        } else {
            // 新增操作
            content = new Content();
            content.setUserId(userId);
            content.setViews(0L);
            content.setLikesCount(0L);
            content.setFavoritesCount(0L);
            content.setCommentsCount(0L);
        }

        // 设置基本字段
        content.setTitle(dto.getTitle());
        content.setContent(dto.getContent());
        content.setContentType(dto.getContentType());
        content.setCoverImage(dto.getCoverImage());
        content.setLocation(dto.getLocation());
        content.setWeather(dto.getWeather());
        content.setMood(dto.getMood());
        content.setCategory(dto.getCategory());
        content.setIsPublic(dto.getIsPublic());
        content.setStatus(dto.getStatus());

        // 处理图片列表（Spring自带Jackson，无报错）
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            try {
                String imagesJson = objectMapper.writeValueAsString(dto.getImages());
                content.setImages(imagesJson);
            } catch (JsonProcessingException e) {
                log.error("图片列表转JSON失败", e);
                content.setImages("[]");
            }
        } else {
            content.setImages("[]");
        }

        // 处理标签
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            content.setTags(String.join(",", dto.getTags()));
        }

        // 设置发布时间
        if (dto.getStatus() != null && dto.getStatus() == 1 && content.getPublishTime() == null) {
            content.setPublishTime(LocalDateTime.now());
        }

        saveOrUpdate(content);
        return content.getId();
    }

    @Override
    public Content getContentDetail(Long id, Long userId) {
        log.info("获取内容详情: id={}, currentUserId={}", id, userId);

        // 查询内容
        Content content = getById(id);
        if (content == null) {
            log.warn("内容不存在: id={}", id);
            return null;
        }

        // 增加浏览量（异步更新，不影响主流程）
        try {
            contentMapper.incrementViews(id);
            content.setViews((content.getViews() == null ? 0 : content.getViews()) + 1);
        } catch (Exception e) {
            log.error("增加浏览量失败: id={}", id, e);
        }

        // 获取作者信息（跨库调用用户服务）
        if (content.getUserId() != null) {
            try {
                RestBean<UserVO> result = userServiceClient.getAuthorInfo(content.getUserId());
                if (result != null && result.getCode() == 200) {
                    content.setAuthor(result.getData());
                    log.info("获取作者信息成功: userId={}, author={}", content.getUserId(), content.getAuthor());
                } else {
                    log.warn("获取作者信息失败: userId={}, message={}",
                            content.getUserId(), result);
                    // 设置默认作者信息
                    UserVO defaultAuthor = new UserVO();
                    defaultAuthor.setId(content.getUserId());
                    defaultAuthor.setName("用户_" + content.getUserId());
                    defaultAuthor.setUsername("user_" + content.getUserId());
                    content.setAuthor(defaultAuthor);
                }
            } catch (Exception e) {
                log.error("调用用户服务异常: userId={}", content.getUserId(), e);
                // 降级处理：设置默认作者信息
                UserVO defaultAuthor = new UserVO();
                defaultAuthor.setId(content.getUserId());
                defaultAuthor.setName("用户_" + content.getUserId());
                defaultAuthor.setUsername("user_" + content.getUserId());
                content.setAuthor(defaultAuthor);
            }
        }

        // 解析图片列表
        if (StrUtil.isNotBlank(content.getImages())) {
            try {
                List<String> imageList = objectMapper.readValue(
                        content.getImages(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                );
                content.setImageList(imageList);
            } catch (Exception e) {
                log.error("解析图片JSON失败: id={}", id, e);
                content.setImageList(List.of());
            }
        }

        // 解析标签列表
        if (StrUtil.isNotBlank(content.getTags())) {
            content.setTagList(Arrays.asList(content.getTags().split(",")));
        }

        // ====================== 修复点赞/收藏状态 ======================
        if (userId != null) {
            content.setIsLiked(likeService.isLiked(userId, id));
            content.setIsFavorited(favoriteService.isFavorited(userId, id));
        } else {
            content.setIsLiked(false);
            content.setIsFavorited(false);
        }
        // ==============================================================

        return content;
    }

    @Override
    public Page<Content> getPublicContents(Integer pageNum, Integer pageSize, String category, String contentType, Long userId) {
        log.info("查询公开内容: pageNum={}, pageSize={}, category={}, contentType={}", pageNum, pageSize, category, contentType);

        Page<Content> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Content> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(Content::getStatus, 1)
                .eq(Content::getIsPublic, 1)
                .orderByDesc(Content::getIsTop)
                .orderByDesc(Content::getPublishTime);

        if (StrUtil.isNotBlank(category)) {
            wrapper.eq(Content::getCategory, category);
        }
        if (StrUtil.isNotBlank(contentType)) {
            wrapper.eq(Content::getContentType, contentType);
        }

        Page<Content> result = page(page, wrapper);

        // 添加日志，打印查询到的数据
        log.info("查询到 {} 条记录", result.getRecords().size());
        for (Content content : result.getRecords()) {
            log.info("文章 - ID: {}, 标题: {}", content.getId(), content.getTitle());

            // 获取作者信息
            if (content.getUserId() != null) {
                try {
                    RestBean<UserVO> authorResult = userServiceClient.getAuthorInfo(content.getUserId());
                    if (authorResult != null && authorResult.getCode() == 200) {
                        content.setAuthor(authorResult.getData());
                    } else {
                        // 设置默认作者信息
                        UserVO defaultAuthor = new UserVO();
                        defaultAuthor.setId(content.getUserId());
                        defaultAuthor.setName("用户_" + content.getUserId());
                        defaultAuthor.setUsername("user_" + content.getUserId());
                        content.setAuthor(defaultAuthor);
                    }
                } catch (Exception e) {
                    log.error("调用用户服务异常: userId={}", content.getUserId(), e);
                    // 降级处理
                    UserVO defaultAuthor = new UserVO();
                    defaultAuthor.setId(content.getUserId());
                    defaultAuthor.setName("用户_" + content.getUserId());
                    defaultAuthor.setUsername("user_" + content.getUserId());
                    content.setAuthor(defaultAuthor);
                }
            }

            // 修复列表页的点赞/收藏状态
            if (userId != null) {
                content.setIsLiked(likeService.isLiked(userId, content.getId()));
                content.setIsFavorited(favoriteService.isFavorited(userId, content.getId()));
            } else {
                content.setIsLiked(false);
                content.setIsFavorited(false);
            }

            // 解析图片列表
            if (StrUtil.isNotBlank(content.getImages())) {
                try {
                    List<String> imageList = objectMapper.readValue(
                            content.getImages(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                    );
                    content.setImageList(imageList);
                } catch (Exception e) {
                    log.error("解析图片JSON失败: id={}", content.getId(), e);
                    content.setImageList(List.of());
                }
            }

            // 解析标签列表
            if (StrUtil.isNotBlank(content.getTags())) {
                content.setTagList(Arrays.asList(content.getTags().split(",")));
            }
        }

        return result;
    }

    @Override
    public Page<Content> getUserContents(Long userId, Integer pageNum, Integer pageSize, Integer status) {
        log.info("查询用户内容: userId={}, pageNum={}, pageSize={}, status={}", userId, pageNum, pageSize, status);

        Page<Content> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Content> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(Content::getUserId, userId)
                .orderByDesc(Content::getCreateTime);

        if (status != null) {
            wrapper.eq(Content::getStatus, status);
        }

        return page(page, wrapper);
    }

    @Override
    public Page<Content> getMyContents(Long userId, Integer pageNum, Integer pageSize, String category, String contentType, String keyword) {
        log.info("查询我的内容: userId={}, pageNum={}, pageSize={}, category={}, contentType={}, keyword={}",
                userId, pageNum, pageSize, category, contentType, keyword);

        Page<Content> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Content> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(Content::getUserId, userId)
                .orderByDesc(Content::getUpdateTime);

        if (StrUtil.isNotBlank(category)) {
            wrapper.eq(Content::getCategory, category);
        }
        if (StrUtil.isNotBlank(contentType)) {
            wrapper.eq(Content::getContentType, contentType);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Content::getTitle, keyword)
                    .or()
                    .like(Content::getContent, keyword));
        }

        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DS("futurestack")
    public boolean deleteContent(Long id, Long userId) {
        log.info("删除内容: id={}, userId={}", id, userId);

        Content content = getById(id);
        if (content == null) {
            throw new RuntimeException("内容不存在");
        }
        if (!content.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此内容");
        }

        // ✅ 真正物理删除（直接从数据库删除）
        return removeById(id);
    }

    @Override
    public List<Content> getHotContents(Integer limit) {
        log.info("获取热门内容: limit={}", limit);
        return contentMapper.selectHotContents(limit);
    }

    @Override
    public Page<Content> searchContents(String keyword, Integer pageNum, Integer pageSize) {
        log.info("搜索内容: keyword={}, pageNum={}, pageSize={}", keyword, pageNum, pageSize);

        Page<Content> page = new Page<>(pageNum, pageSize);

        if (StrUtil.isBlank(keyword)) {
            return page;
        }

        LambdaQueryWrapper<Content> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Content::getStatus, 1)
                .eq(Content::getIsPublic, 1)
                .and(w -> w.like(Content::getTitle, keyword)
                        .or()
                        .like(Content::getContent, keyword))
                .orderByDesc(Content::getPublishTime);

        return page(page, wrapper);
    }
}