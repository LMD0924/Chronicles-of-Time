package org.example.generalservice.mapper.content;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.generalservice.entity.content.FavoriteRecord;


/**
 * 收藏记录Mapper接口
 *
 * @author 总会落叶
 * @date 2026-04-06
 */
@Mapper
@DS("futurestack")
public interface FavoriteRecordMapper extends BaseMapper<FavoriteRecord> {

    /**
     * 检查用户是否已收藏
     *
     * @param userId    用户ID
     * @param contentId 内容ID
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) FROM favorite_record WHERE user_id = #{userId} AND content_id = #{contentId}")
    int checkUserFavorited(@Param("userId") Long userId, @Param("contentId") Long contentId);
}