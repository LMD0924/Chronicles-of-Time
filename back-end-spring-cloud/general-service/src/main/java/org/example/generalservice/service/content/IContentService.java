package org.example.generalservice.service.content;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.generalservice.dto.content.ContentSaveDTO;
import org.example.generalservice.entity.content.Content;

import java.util.List;

/**
 * 内容服务接口
 *
 * @author 总会落叶
 * @date 2026-04-06
 */
public interface IContentService {

    /**
     * 保存或更新内容
     *
     * @param dto      内容DTO
     * @param userId   用户ID
     * @return 内容ID
     */
    Long saveContent(ContentSaveDTO dto, Long userId);

    /**
     * 获取内容详情
     *
     * @param id      内容ID
     * @param userId  当前用户ID（用于判断点赞收藏状态，可为null）
     * @return 内容实体
     */
    Content getContentDetail(Long id, Long userId);

    /**
     * 分页查询公开内容
     *
     * @param pageNum     页码
     * @param pageSize    每页大小
     * @param category    分类（可选）
     * @param contentType 内容类型（可选）
     * @param userId      当前用户ID（可选，用于判断点赞收藏状态）
     * @return 分页结果
     */
    Page<Content> getPublicContents(Integer pageNum, Integer pageSize, String category, String contentType, Long userId);

    /**
     * 查询用户的内容列表
     *
     * @param userId      用户ID
     * @param pageNum     页码
     * @param pageSize    每页大小
     * @param status      状态（可选）
     * @return 分页结果
     */
    Page<Content> getUserContents(Long userId, Integer pageNum, Integer pageSize, Integer status);

    /**
     * 查询当前登录用户的内容列表（带更多筛选条件）
     *
     * @param userId      用户ID
     * @param pageNum     页码
     * @param pageSize    每页大小
     * @param category    分类（可选）
     * @param contentType 内容类型（可选）
     * @param keyword     关键词（可选）
     * @return 分页结果
     */
    Page<Content> getMyContents(Long userId, Integer pageNum, Integer pageSize, String category, String contentType, String keyword);

    /**
     * 删除内容（逻辑删除）
     *
     * @param id      内容ID
     * @param userId  用户ID（用于权限验证）
     * @return 是否成功
     */
    boolean deleteContent(Long id, Long userId);

    /**
     * 获取热门内容
     *
     * @param limit 查询数量
     * @return 热门内容列表
     */
    List<Content> getHotContents(Integer limit);

    /**
     * 搜索内容
     *
     * @param keyword  关键词
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Page<Content> searchContents(String keyword, Integer pageNum, Integer pageSize);
}