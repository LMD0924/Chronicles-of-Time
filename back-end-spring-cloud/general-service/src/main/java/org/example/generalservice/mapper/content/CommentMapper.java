package org.example.generalservice.mapper.content;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.generalservice.entity.content.Comment;

import java.util.List;

/**
 * 评论Mapper接口
 *
 * @author 总会落叶
 * @date 2026-04-06
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 获取内容的所有顶级评论
     *
     * @param contentId 内容ID
     * @return 顶级评论列表
     */
    @Select("SELECT * FROM comment WHERE content_id = #{contentId} AND parent_id = 0 AND status = 1 ORDER BY create_time DESC")
    List<Comment> selectTopComments(@Param("contentId") Long contentId);

    /**
     * 获取评论的子回复
     *
     * @param parentId 父评论ID
     * @return 子评论列表
     */
    @Select("SELECT * FROM comment WHERE parent_id = #{parentId} AND status = 1 ORDER BY create_time ASC")
    List<Comment> selectChildComments(@Param("parentId") Long parentId);

    /**
     * 增加评论点赞数
     *
     * @param id 评论ID
     * @return 影响行数
     */
    @Update("UPDATE comment SET likes_count = likes_count + 1 WHERE id = #{id}")
    int incrementLikesCount(@Param("id") Long id);

    /**
     * 减少评论点赞数
     *
     * @param id 评论ID
     * @return 影响行数
     */
    @Update("UPDATE comment SET likes_count = likes_count - 1 WHERE id = #{id}")
    int decrementLikesCount(@Param("id") Long id);
}