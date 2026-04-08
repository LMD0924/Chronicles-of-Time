package org.example.generalservice.mapper.content;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.generalservice.entity.content.Content;

import java.util.List;

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
     *
     * @param id 内容ID
     * @return 影响行数
     */
    @Update("UPDATE content SET views = views + 1 WHERE id = #{id}")
    int incrementViews(@Param("id") Long id);

    /**
     * 增加点赞数
     *
     * @param id 内容ID
     * @return 影响行数
     */
    @Update("UPDATE content SET likes_count = likes_count + 1 WHERE id = #{id}")
    int incrementLikesCount(@Param("id") Long id);

    /**
     * 减少点赞数
     *
     * @param id 内容ID
     * @return 影响行数
     */
    @Update("UPDATE content SET likes_count = likes_count - 1 WHERE id = #{id}")
    int decrementLikesCount(@Param("id") Long id);

    /**
     * 增加收藏数
     *
     * @param id 内容ID
     * @return 影响行数
     */
    @Update("UPDATE content SET favorites_count = favorites_count + 1 WHERE id = #{id}")
    int incrementFavoritesCount(@Param("id") Long id);

    /**
     * 减少收藏数
     *
     * @param id 内容ID
     * @return 影响行数
     */
    @Update("UPDATE content SET favorites_count = favorites_count - 1 WHERE id = #{id}")
    int decrementFavoritesCount(@Param("id") Long id);

    /**
     * 增加评论数
     *
     * @param id 内容ID
     * @return 影响行数
     */
    @Update("UPDATE content SET comments_count = comments_count + 1 WHERE id = #{id}")
    int incrementCommentsCount(@Param("id") Long id);

    /**
     * 减少评论数
     *
     * @param id 内容ID
     * @return 影响行数
     */
    @Update("UPDATE content SET comments_count = comments_count - 1 WHERE id = #{id}")
    int decrementCommentsCount(@Param("id") Long id);

    /**
     * 查询热门内容（按浏览量排序）
     *
     * @param limit 查询数量
     * @return 热门内容列表
     */
    @Select("SELECT * FROM content WHERE status = 1 AND is_public = 1 ORDER BY views DESC LIMIT #{limit}")
    List<Content> selectHotContents(@Param("limit") Integer limit);
}