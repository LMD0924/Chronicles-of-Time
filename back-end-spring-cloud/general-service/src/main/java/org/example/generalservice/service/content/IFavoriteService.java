package org.example.generalservice.service.content;

/**
 * 收藏服务接口
 *
 * @author 总会落叶
 * @date 2026-04-06
 */
public interface IFavoriteService {

    /**
     * 收藏
     *
     * @param userId    用户ID
     * @param contentId 内容ID
     * @return 是否成功
     */
    boolean favorite(Long userId, Long contentId);

    /**
     * 取消收藏
     *
     * @param userId    用户ID
     * @param contentId 内容ID
     * @return 是否成功
     */
    boolean unfavorite(Long userId, Long contentId);

    /**
     * 检查用户是否已收藏
     *
     * @param userId    用户ID
     * @param contentId 内容ID
     * @return 是否已收藏
     */
    boolean isFavorited(Long userId, Long contentId);
}