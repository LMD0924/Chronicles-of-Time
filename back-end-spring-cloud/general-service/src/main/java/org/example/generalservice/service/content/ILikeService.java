/**
 * 文件说明：拾光记微服务后端通用内容服务内容社区源码，负责内容社区相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.generalservice.service.content;

/**
 * 点赞服务接口
 *
 * @author 总会落叶
 * @date 2026-04-06
 */
/**
 * 类说明：当前类是内容社区模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
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