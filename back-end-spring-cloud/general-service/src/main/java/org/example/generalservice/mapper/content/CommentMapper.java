/**
 * 文件说明：拾光记微服务后端通用内容服务内容社区源码，负责内容社区相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.generalservice.mapper.content;

import com.baomidou.dynamic.datasource.annotation.DS;
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
/**
 * 类说明：当前类是内容社区模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Mapper
@DS("cot_content")
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 获取内容的所有顶级评论
     *
     * @param contentId 内容ID
     * @return 顶级评论列表
     */
    @Select("SELECT * FROM content_comment WHERE content_id = #{contentId} AND parent_id = 0 AND status = 1 ORDER BY created_at DESC")
    List<Comment> selectTopComments(@Param("contentId") Long contentId);

    /**
     * 获取评论的子回复
     *
     * @param parentId 父评论ID
     * @return 子评论列表
     */
    @Select("SELECT * FROM content_comment WHERE parent_id = #{parentId} AND status = 1 ORDER BY created_at ASC")
    List<Comment> selectChildComments(@Param("parentId") Long parentId);

    /**
     * 增加评论点赞数
     *
     * @param id 评论ID
     * @return 影响行数
     */
    @Update("UPDATE content_comment SET like_count = like_count + 1 WHERE id = #{id}")
    int incrementLikesCount(@Param("id") Long id);

    /**
     * 减少评论点赞数
     *
     * @param id 评论ID
     * @return 影响行数
     */
    @Update("UPDATE content_comment SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{id}")
    int decrementLikesCount(@Param("id") Long id);
}