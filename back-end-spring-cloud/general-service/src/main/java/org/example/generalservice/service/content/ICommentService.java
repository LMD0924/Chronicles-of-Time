/**
 * 文件说明：拾光记微服务后端通用内容服务内容社区源码，负责内容社区相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.generalservice.service.content;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.generalservice.entity.content.Comment;

import java.util.List;

/**
 * 评论服务接口
 *
 * @author 总会落叶
 * @date 2026-04-06
 */
/**
 * 类说明：当前类是内容社区模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
public interface ICommentService {

    /**
     * 添加评论
     *
     * @param comment 评论实体
     * @return 是否成功
     */
    boolean addComment(Comment comment);

    /**
     * 删除评论
     *
     * @param id      评论ID
     * @param userId  用户ID（用于权限验证）
     * @return 是否成功
     */
    boolean deleteComment(Long id, Long userId);

    /**
     * 获取内容的评论列表（包含子评论）
     *
     * @param contentId 内容ID
     * @return 评论列表
     */
    List<Comment> getCommentList(Long contentId, Long userId);

    /**
     * 分页获取内容的评论
     *
     * @param contentId 内容ID
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @return 分页结果
     */
    Page<Comment> getCommentPage(Long contentId, Integer pageNum, Integer pageSize);

    /**
     * 点赞评论
     *
     * @param id 评论ID
     * @return 是否成功
     */
    boolean likeComment(Long commentId, Long userId);

    /**
     * 取消点赞评论
     *
     * @param id 评论ID
     * @return 是否成功
     */
    boolean unlikeComment(Long commentId, Long userId);

    boolean isCommentLiked(Long commentId, Long userId);
}