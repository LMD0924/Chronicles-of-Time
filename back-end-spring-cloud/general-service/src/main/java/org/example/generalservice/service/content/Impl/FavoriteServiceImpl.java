/**
 * 文件说明：拾光记微服务后端通用内容服务内容社区源码，负责内容社区相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.generalservice.service.content.Impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.generalservice.entity.content.FavoriteRecord;
import org.example.generalservice.mapper.content.FavoriteRecordMapper;
import org.example.generalservice.mapper.content.ContentMapper;
import org.example.generalservice.service.content.IFavoriteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 收藏服务实现类
 *
 * @author 总会落叶
 * @date 2026-04-06
 */
/**
 * 类说明：当前类是内容社区模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DS("cot_content")
public class FavoriteServiceImpl extends ServiceImpl<FavoriteRecordMapper, FavoriteRecord> implements IFavoriteService {

    private final FavoriteRecordMapper favoriteRecordMapper;
    private final ContentMapper contentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean favorite(Long userId, Long contentId) {
        log.info("用户收藏: userId={}, contentId={}", userId, contentId);

        // 检查是否已收藏
        if (favoriteRecordMapper.checkUserFavorited(userId, contentId) > 0) {
            log.warn("用户已收藏过: userId={}, contentId={}", userId, contentId);
            return false;
        }

        // 添加收藏记录
        FavoriteRecord favoriteRecord = new FavoriteRecord();
        favoriteRecord.setUserId(userId);
        favoriteRecord.setContentId(contentId);
        boolean saved = save(favoriteRecord);

        // 更新内容的收藏数
        if (saved) {
            contentMapper.incrementFavoritesCount(contentId);
        }

        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfavorite(Long userId, Long contentId) {
        log.info("用户取消收藏: userId={}, contentId={}", userId, contentId);

        // 删除收藏记录
        boolean removed = lambdaUpdate()
                .eq(FavoriteRecord::getUserId, userId)
                .eq(FavoriteRecord::getContentId, contentId)
                .remove();

        // 更新内容的收藏数
        if (removed) {
            contentMapper.decrementFavoritesCount(contentId);
        }

        return removed;
    }

    @Override
    public boolean isFavorited(Long userId, Long contentId) {
        return favoriteRecordMapper.checkUserFavorited(userId, contentId) > 0;
    }
}