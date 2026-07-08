/**
 * 文件说明：拾光记微服务后端认证中心业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.authcenter.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.authcenter.dto.AuthDTO;
import org.example.authcenter.entity.User;
import org.example.authcenter.mapper.AuthMapper;
import org.example.authcenter.service.AuthService;
import org.example.authcenter.vo.LoginVO;
import org.example.authcenter.vo.UserVO;
import org.example.commoncore.auth.RoleCodes;
import org.example.commoncore.utils.JwtUtil;
import org.example.commondb.utils.RestBean;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl extends ServiceImpl<AuthMapper, User> implements AuthService {

    private final AuthMapper authMapper;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final PasswordEncoder passwordEncoder;

    private static final String ACCESS_TOKEN_PREFIX = "access_token:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String TOKEN_BLACKLIST_PREFIX = "blacklist:";

    @Override
    public LoginVO login(AuthDTO authDTO) {
        if (authDTO == null || StringUtils.isBlank(authDTO.getUsername()) || StringUtils.isBlank(authDTO.getPassword())) {
            throw new RuntimeException("用户名或密码不能为空");
        }

        User user = lambdaQuery().eq(User::getUsername, authDTO.getUsername()).one();
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用");
        }
        if (!passwordEncoder.matches(authDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }



        // 权限随 token 一起返回给前端，后台管理端可据此控制菜单和按钮级权限。
        List<String> roles = loadRoles(user.getId());
        List<String> permissions = loadPermissions(user.getId());
        String accessToken = jwtUtil.generateAccessTokenWithRememberMe(user.getUsername(), user.getId(), roles, permissions, authDTO.getRememberMe());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getId());

        Long expiration = jwtUtil.getRemainingTime(accessToken);


        // Redis 中保存 access token 的剩余有效期，网关会以此判断会话是否仍在线。
        redisTemplate.opsForValue().set(ACCESS_TOKEN_PREFIX + accessToken, user.getId().toString(), expiration, TimeUnit.MILLISECONDS);
        redisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + user.getId(), refreshToken, 7, TimeUnit.DAYS);

        UserVO userVO = buildUserVO(user, roles, permissions);
        return LoginVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiration / 1000)
                .roles(roles)
                .permissions(permissions)
                .userInfo(userVO)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer register(AuthDTO authDTO) {
        if (authDTO == null || StringUtils.isBlank(authDTO.getUsername()) || StringUtils.isBlank(authDTO.getPassword())) {
            throw new RuntimeException("用户名或密码不能为空");
        }
        String username = authDTO.getUsername().trim();
        long count = lambdaQuery().eq(User::getUsername, username).count();
        if (count > 0) {
            throw new RuntimeException("用户已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(authDTO.getPassword()));
        user.setName(resolveDisplayName(authDTO, username));
        user.setEmail(StringUtils.isBlank(authDTO.getEmail()) ? null : authDTO.getEmail().trim());
        user.setPhone(StringUtils.isBlank(authDTO.getPhone()) ? null : authDTO.getPhone().trim());
        user.setUserType(1);
        user.setRegisterChannel("web");
        user.setStatus(1);
        int inserted = authMapper.insert(user);
        if (inserted > 0) {
            Long roleId = authMapper.selectRoleIdByCode(RoleCodes.USER);
            if (roleId != null) {
                authMapper.insertUserRole(user.getId(), user.getId(), roleId);
            }
        }
        return inserted;
    }


    @Override
    public LoginVO refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("无效的刷新令牌");
        }
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new RuntimeException("令牌类型错误");
        }

        String username = jwtUtil.getUsernameFromToken(refreshToken);
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        String storedRefreshToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new RuntimeException("刷新令牌已失效，请重新登录");
        }

        User user = authMapper.selectById(userId);
        if (user == null || (user.getStatus() != null && user.getStatus() == 0)) {
            throw new RuntimeException("用户不存在或已被禁用");
        }

        List<String> roles = loadRoles(userId);
        List<String> permissions = loadPermissions(userId);
        String newAccessToken = jwtUtil.generateAccessToken(username, userId, roles, permissions);
        Long expiration = jwtUtil.getRemainingTime(newAccessToken);
        redisTemplate.opsForValue().set(ACCESS_TOKEN_PREFIX + newAccessToken, userId.toString(), expiration, TimeUnit.MILLISECONDS);

        return LoginVO.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiration / 1000)
                .roles(roles)
                .permissions(permissions)
                .userInfo(buildUserVO(user, roles, permissions))
                .build();
    }


    @Override
    public void logout(String accessToken, Long userId) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
        Long remainingTime = jwtUtil.getRemainingTime(accessToken);
        if (remainingTime > 0) {
            redisTemplate.opsForValue().set(TOKEN_BLACKLIST_PREFIX + accessToken, userId.toString(), remainingTime, TimeUnit.MILLISECONDS);
        }
        redisTemplate.delete(ACCESS_TOKEN_PREFIX + accessToken);
        log.info("用户登出成功: userId={}", userId);
    }

    @Override
    public RestBean<Object> verifyToken(String token) {
        if (!jwtUtil.validateToken(token)) {
            return RestBean.fail(401, "Token无效或已过期");
        }
        if (Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token))) {
            return RestBean.fail(401, "Token已被注销");
        }
        if (Boolean.FALSE.equals(redisTemplate.hasKey(ACCESS_TOKEN_PREFIX + token))) {
            return RestBean.fail(401, "Token已失效");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        List<String> roles = jwtUtil.getRolesFromToken(token);
        List<String> permissions = jwtUtil.getPermissionsFromToken(token);
        return RestBean.success("验证通过", java.util.Map.of(
                "userId", userId,
                "username", username,
                "roles", roles,
                "permissions", permissions,
                "valid", true
        ));
    }

    private List<String> loadRoles(Long userId) {
        List<String> roles = authMapper.selectRoleCodesByUserId(userId);
        if (roles == null || roles.isEmpty()) {
            return new ArrayList<>(List.of(RoleCodes.USER));
        }
        return roles;
    }

    private List<String> loadPermissions(Long userId) {
        List<String> permissions = authMapper.selectPermissionCodesByUserId(userId);
        return permissions == null ? List.of() : permissions;
    }

    private UserVO buildUserVO(User user, List<String> roles, List<String> permissions) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setRoles(roles);
        userVO.setPermissions(permissions);
        userVO.setRole(roles == null || roles.isEmpty() ? RoleCodes.USER : roles.get(0));
        return userVO;
    }

    private String resolveDisplayName(AuthDTO authDTO, String username) {
        if (StringUtils.isNotBlank(authDTO.getName())) {
            return authDTO.getName().trim();
        }
        if (StringUtils.isNotBlank(authDTO.getDisplayName())) {
            return authDTO.getDisplayName().trim();
        }
        return username;
    }
}
