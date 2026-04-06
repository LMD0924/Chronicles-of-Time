package org.example.generalservice.service.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.generalservice.entity.content.Content;
import org.example.generalservice.mapper.content.ContentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 内容服务类
 *
 * @author 总会落叶
 * @date 2026-04-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentService extends ServiceImpl<ContentMapper, Content> {

    private final ContentMapper contentMapper;

    /**
     * 添加或更新内容
     *
     * @param content 内容实体
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateContent(Content content) {
        log.info("保存内容: title={}, userId={}", content.getTitle(), content.getUserId());

        // 设置默认值
        if (content.getStatus() == null) {
            content.setStatus(1); // 默认已发布
        }
        if (content.getIsPublic() == null) {
            content.setIsPublic(1); // 默认公开
        }
        if (content.getViews() == null) {
            content.setViews(0L);
        }
        if (content.getLikesCount() == null) {
            content.setLikesCount(0L);
        }
        if (content.getFavoritesCount() == null) {
            content.setFavoritesCount(0L);
        }
        if (content.getCommentsCount() == null) {
            content.setCommentsCount(0L);
        }

        // 如果是发布状态且发布时间为空，设置发布时间
        if (content.getStatus() == 1 && content.getPublishTime() == null) {
            content.setPublishTime(LocalDateTime.now());
        }

        // 处理图片JSON
        if (content.getImageList() != null && !content.getImageList().isEmpty()) {
            content.setImages(com.alibaba.fastjson.JSON.toJSONString(content.getImageList()));
        }

        // 处理标签
        if (content.getTagList() != null && !content.getTagList().isEmpty()) {
            content.setTags(String.join(",", content.getTagList()));
        }

        return saveOrUpdate(content);
    }

    /**
     * 获取内容详情（增加浏览量）
     *
     * @param id 内容ID
     * @return 内容实体
     */
    @Transactional(rollbackFor = Exception.class)
    public Content getContentDetail(Long id) {
        log.info("获取内容详情: id={}", id);

        // 增加浏览量
        contentMapper.incrementViews(id);

        // 查询内容
        Content content = getById(id);
        if (content != null) {
            // 解析图片列表
            if (StringUtils.hasText(content.getImages())) {
                try {
                    List<String> imageList = com.alibaba.fastjson.JSON.parseArray(content.getImages(), String.class);
                    content.setImageList(imageList);
                } catch (Exception e) {
                    log.error("解析图片JSON失败", e);
                }
            }

            // 解析标签列表
            if (StringUtils.hasText(content.getTags())) {
                content.setTagList(Arrays.asList(content.getTags().split(",")));
            }
        }

        return content;
    }

    /**
     * 分页查询公开内容
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param category 分类（可选）
     * @param contentType 内容类型（可选）
     * @return 分页结果
     */
    public Page<Content> getPublicContents(Integer pageNum, Integer pageSize, String category, String contentType) {
        log.info("查询公开内容: pageNum={}, pageSize={}, category={}, contentType={}", pageNum, pageSize, category, contentType);

        Page<Content> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Content> wrapper = new LambdaQueryWrapper<>();

        // 只查询已发布且公开的内容
        wrapper.eq(Content::getStatus, 1)
                .eq(Content::getIsPublic, 1)
                .orderByDesc(Content::getIsTop)  // 置顶优先
                .orderByDesc(Content::getPublishTime);  // 按发布时间倒序

        if (StringUtils.hasText(category)) {
            wrapper.eq(Content::getCategory, category);
        }
        if (StringUtils.hasText(contentType)) {
            wrapper.eq(Content::getContentType, contentType);
        }

        return page(page, wrapper);
    }

    /**
     * 查询用户的内容列表
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param status   状态（可选）
     * @return 分页结果
     */
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

    /**
     * 删除内容（逻辑删除）
     *
     * @param id 内容ID
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteContent(Long id) {
        log.info("删除内容: id={}", id);

        Content content = new Content();
        content.setId(id);
        content.setStatus(2); // 设置为已删除状态

        return updateById(content);
    }

    /**
     * 获取热门内容
     *
     * @param limit 查询数量
     * @return 热门内容列表
     */
    public List<Content> getHotContents(Integer limit) {
        log.info("获取热门内容: limit={}", limit);
        return contentMapper.selectHotContents(limit);
    }

    /**
     * 搜索内容（支持标题和内容全文搜索）
     *
     * @param keyword  关键词
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    public Page<Content> searchContents(String keyword, Integer pageNum, Integer pageSize) {
        log.info("搜索内容: keyword={}, pageNum={}, pageSize={}", keyword, pageNum, pageSize);

        Page<Content> page = new Page<>(pageNum, pageSize);

        if (!StringUtils.hasText(keyword)) {
            return page;
        }

        // 使用MyBatis-Plus的全文搜索（需要数据库支持）
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