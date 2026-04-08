package org.example.generalservice.service.content;

/**
 * 点赞服务接口
 *
 * @author 总会落叶
 * @date 2026-04-06
 */
public interface ILikeService {

    /**
     * 点赞
     *
     * @param userId    用户ID
     * @param contentId 内容ID
     * @return 是否成功
     */
    boolean like(Long userId, Long contentId);

    /**
     * 取消点赞
     *
     * @param userId    用户ID
     * @param contentId 内容ID
     * @return 是否成功
     */
    boolean unlike(Long userId, Long contentId);

    /**
     * 检查用户是否已点赞
     *
     * @param userId    用户ID
     * @param contentId 内容ID
     * @return 是否已点赞
     */
    boolean isLiked(Long userId, Long contentId);
}