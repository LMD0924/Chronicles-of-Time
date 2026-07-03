/**
 * 文件说明：拾光记微服务后端通用内容服务内容社区源码，负责内容社区相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.generalservice.mapper.content;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.generalservice.entity.content.LikeRecord;


/**
 * 点赞记录Mapper接口
 *
 * @author 总会落叶
 * @date 2026-04-06
 */
/**
 * 类说明：当前类是内容社区模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Mapper
@DS("cot_content")
public interface LikeRecordMapper extends BaseMapper<LikeRecord> {

    /**
     * 检查用户是否已点赞
     *
     * @param userId    用户ID
     * @param contentId 内容ID
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) FROM content_reaction WHERE biz_type = 'article' AND reaction_type = 'like' AND user_id = #{userId} AND biz_id = #{contentId}")
    int checkUserLiked(@Param("userId") Long userId, @Param("contentId") Long contentId);
}