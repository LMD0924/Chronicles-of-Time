/**
 * 文件说明：拾光记微服务后端通用内容服务内容社区源码，负责内容社区相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.generalservice.service.content.Impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.generalservice.entity.content.LikeRecord;
import org.example.generalservice.mapper.content.LikeRecordMapper;
import org.example.generalservice.mapper.content.ContentMapper;
import org.example.generalservice.service.content.ILikeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 点赞服务实现类
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
public class LikeServiceImpl extends ServiceImpl<LikeRecordMapper, LikeRecord> implements ILikeService {

    private final LikeRecordMapper likeRecordMapper;
    private final ContentMapper contentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean like(Long userId, Long contentId) {
        log.info("用户点赞: userId={}, contentId={}", userId, contentId);

        // 检查是否已点赞
        if (likeRecordMapper.checkUserLiked(userId, contentId) > 0) {
            log.warn("用户已点赞过: userId={}, contentId={}", userId, contentId);
            return false;
        }

        // 添加点赞记录
        LikeRecord likeRecord = new LikeRecord();
        likeRecord.setUserId(userId);
        likeRecord.setContentId(contentId);
        boolean saved = save(likeRecord);

        // 更新内容的点赞数
        if (saved) {
            contentMapper.incrementLikesCount(contentId);
        }

        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unlike(Long userId, Long contentId) {
        log.info("用户取消点赞: userId={}, contentId={}", userId, contentId);

        // 删除点赞记录
        boolean removed = lambdaUpdate()
                .eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getContentId, contentId)
                .remove();

        // 更新内容的点赞数
        if (removed) {
            contentMapper.decrementLikesCount(contentId);
        }

        return removed;
    }

    @Override
    public boolean isLiked(Long userId, Long contentId) {
        return likeRecordMapper.checkUserLiked(userId, contentId) > 0;
    }
}