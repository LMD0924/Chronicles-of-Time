/**
 * 文件说明：拾光记微服务后端认证中心业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.authcenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.authcenter.dto.AuthDTO;
import org.example.authcenter.entity.User;
import org.example.authcenter.vo.LoginVO;
import org.example.authcenter.vo.UserVO;
import org.example.commondb.utils.RestBean;

/*
 * @Author:总会落叶
 * @Date:2026/3/19
 * @Description:
 */
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
public interface AuthService extends IService<User> {

    /**
     * 用户登录
     */
    LoginVO login(AuthDTO authDTO);

    /**
     * 用户注册
     */
    Integer register(AuthDTO authDTO);

    /**
     * 刷新Token
     * @param refreshToken 刷新令牌
     * @return 新的登录信息
     */
    LoginVO refreshToken(String refreshToken);

    /**
     * 用户登出
     * @param accessToken 访问令牌
     * @param userId 用户ID
     */
    void logout(String accessToken, Long userId);

    /**
     * 验证Token（供网关调用）
     * @param token 令牌
     * @return 验证结果
     */
    RestBean<Object> verifyToken(String token);
}
