/**
 * 文件说明：拾光记微服务后端认证中心数据访问映射源码，负责数据访问映射相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.authcenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.authcenter.entity.User;

import java.util.Date;
import java.util.List;

/**
 * 类说明：当前类是数据访问映射模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Mapper
public interface AuthMapper extends BaseMapper<User> {

    @Select("SELECT r.role_code FROM iam_user_role ur JOIN iam_role r ON ur.role_id = r.id WHERE ur.user_id = #{userId} AND r.status = 1 ORDER BY r.sort_order")
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    @Select("SELECT permission_code FROM (" +
            "SELECT p.permission_code, MIN(p.sort_order) AS sort_order " +
            "FROM iam_user_role ur " +
            "JOIN iam_role_permission rp ON ur.role_id = rp.role_id " +
            "JOIN iam_permission p ON rp.permission_id = p.id " +
            "WHERE ur.user_id = #{userId} AND p.status = 1 " +
            "GROUP BY p.permission_code" +
            ") t ORDER BY t.sort_order, t.permission_code")
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);

    @Select("SELECT id FROM iam_role WHERE role_code = #{roleCode} AND status = 1 LIMIT 1")
    Long selectRoleIdByCode(@Param("roleCode") String roleCode);

    @Insert("INSERT IGNORE INTO iam_user_role (id, user_id, role_id, tenant_id) VALUES (#{id}, #{userId}, #{roleId}, 0)")
    int insertUserRole(@Param("id") Long id, @Param("userId") Long userId, @Param("roleId") Long roleId);

    @Delete("DELETE FROM iam_user_role WHERE user_id = #{userId}")
    int deleteUserRoles(@Param("userId") Long userId);

    @Insert("INSERT INTO iam_refresh_token (id, user_id, token_jti, expires_at) VALUES (#{id}, #{userId}, #{tokenJti}, #{expiresAt})")
    int insertTokenSession(@Param("id") Long id, @Param("userId") Long userId,
                           @Param("tokenJti") String tokenJti, @Param("expiresAt") Date expiresAt);

    @Select("SELECT COUNT(1) FROM iam_refresh_token WHERE user_id = #{userId} AND token_jti = #{tokenJti} AND revoked_at IS NULL AND expires_at > NOW()")
    int countActiveTokenSessions(@Param("userId") Long userId, @Param("tokenJti") String tokenJti);

    @Update("UPDATE iam_refresh_token SET revoked_at = NOW() WHERE user_id = #{userId} AND token_jti = #{tokenJti} AND revoked_at IS NULL")
    int revokeTokenSession(@Param("userId") Long userId, @Param("tokenJti") String tokenJti);
}
