package org.example.generalservice.mapper.content;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.generalservice.entity.content.Content;

import java.util.List;
import java.util.Map;

/**
 * 内容Mapper接口
 *
 * @author 总会落叶
 * @date 2026-04-06
 */
@Mapper
@DS("futurestack")
public interface ContentMapper extends BaseMapper<Content> {

    /**
     * 增加浏览量
     */
    int incrementViews(@Param("id") Long id);

    /**
     * 增加点赞数
     */
    int incrementLikesCount(@Param("id") Long id);

    /**
     * 减少点赞数
     */
    int decrementLikesCount(@Param("id") Long id);

    /**
     * 增加收藏数
     */
    int incrementFavoritesCount(@Param("id") Long id);

    /**
     * 减少收藏数
     */
    int decrementFavoritesCount(@Param("id") Long id);

    /**
     * 增加评论数
     */
    int incrementCommentsCount(@Param("id") Long id);

    /**
     * 减少评论数
     */
    int decrementCommentsCount(@Param("id") Long id);

    /**
     * 查询热门内容（按浏览量排序）
     */
    List<Content> selectHotContents(@Param("limit") Integer limit);

    // ==================== 知识图谱相关方法 ====================

    /**
     * 获取用户自己的所有标签及使用次数
     */
    List<Map<String, Object>> selectUserTagsWithCount(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 获取用户自己的所有分类及使用次数
     */
    List<Map<String, Object>> selectUserCategoriesWithCount(@Param("userId") Long userId);

    /**
     * 获取用户自己的标签关联的文章ID列表
     */
    List<Long> selectUserContentIdsByTag(@Param("userId") Long userId, @Param("tag") String tag);

    /**
     * 获取用户自己的标签共现关系
     */
    List<Map<String, Object>> selectUserTagCooccurrence(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 获取用户自己的文章总数
     */
    Long selectUserContentCount(@Param("userId") Long userId);

    /**
     * 获取全局所有标签及使用次数
     */
    List<Map<String, Object>> selectAllTagsWithCount(@Param("limit") Integer limit);

    /**
     * 获取全局所有分类及使用次数
     */
    List<Map<String, Object>> selectAllCategoriesWithCount();

    /**
     * 获取全局标签共现关系
     */
    List<Map<String, Object>> selectAllTagCooccurrence(@Param("limit") Integer limit);
}