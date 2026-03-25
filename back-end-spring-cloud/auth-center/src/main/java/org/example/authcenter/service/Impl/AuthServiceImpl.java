package org.example.authcenter.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.authcenter.dto.AuthDTO;
import org.example.authcenter.entity.User;
import org.example.authcenter.mapper.AuthMapper;
import org.example.authcenter.mapper.UserMapper;
import org.example.authcenter.service.AuthService;
import org.example.authcenter.vo.LoginVO;
import org.example.authcenter.vo.UserVO;
import org.example.commoncore.utils.JwtUtil;
import org.example.commondb.utils.RestBean;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/*
 * @Author:总会落叶
 * @Date:2026/3/19
 * @Description:
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl extends ServiceImpl<AuthMapper, User> implements AuthService {

    private final AuthMapper authMapper;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final PasswordEncoder passwordEncoder;

    // Redis Key 前缀
    private static final String ACCESS_TOKEN_PREFIX = "access_token:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String TOKEN_BLACKLIST_PREFIX = "blacklist:";
    /**
     * 用户登录
     * @param authDTO
     * @return
     */
    @Override
    public LoginVO login(AuthDTO authDTO) {
        //参数校验
        if(authDTO==null ||
          StringUtils.isBlank(authDTO.getUsername()) ||
          StringUtils.isBlank(authDTO.getPassword())){
            throw new RuntimeException("用户名或密码不能为空");
        }
        //根据用户名查询用户
        User user = lambdaQuery()
                .eq(User::getUsername, authDTO.getUsername())
                .one();
        //用户不存在
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        //用户存在，校验密码
        if(!passwordEncoder.matches(authDTO.getPassword(),user.getPassword())){
            throw new RuntimeException("密码错误");
        }
        // 4. 生成 Token
        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getId(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getId());

        // ✅ 添加日志，查看生成的 Token
        log.info("生成的 AccessToken: {}", accessToken);
        log.info("Token 包含的点数: {}", accessToken.chars().filter(ch -> ch == '.').count());

        // 5. 存储 Token 到 Redis
        Long expiration = jwtUtil.getRemainingTime(accessToken);
        String accessKey = ACCESS_TOKEN_PREFIX + accessToken;
        redisTemplate.opsForValue().set(accessKey, user.getId().toString(),
                expiration, TimeUnit.MILLISECONDS);

        String refreshKey = REFRESH_TOKEN_PREFIX + user.getId();
        redisTemplate.opsForValue().set(refreshKey, refreshToken, 7, TimeUnit.DAYS);

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return LoginVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiration / 1000)
                .userInfo(userVO)
                .build();
    }


    /**
     * 用户注册
     * @param authDTO
     * @return
     */
    @Override
    public Integer register(AuthDTO authDTO) {
        //判断用户是否存在
        long count = lambdaQuery()
                .eq(User::getUsername,authDTO.getUsername())
                .count();
        if(count>0){
            throw new RuntimeException("用户已存在");
        }

        //如果不存在，创建新用户
        User user = new User();
        user.setUsername(authDTO.getUsername());
        //密码加密
        String encodePassword = passwordEncoder.encode(authDTO.getPassword());
        user.setPassword(encodePassword);
        return authMapper.insert(user);
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        // 1. 验证刷新令牌
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("无效的刷新令牌");
        }

        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new RuntimeException("令牌类型错误");
        }

        String username = jwtUtil.getUsernameFromToken(refreshToken);
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);

        // 2. 验证 Redis 中的刷新令牌
        String refreshKey = REFRESH_TOKEN_PREFIX + userId;
        String storedRefreshToken = redisTemplate.opsForValue().get(refreshKey);

        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new RuntimeException("刷新令牌已失效，请重新登录");
        }

        // 3. 查询用户
        User user = authMapper.selectById(userId);
        if (user == null || user.getStatus() == 0) {
            throw new RuntimeException("用户不存在或已被禁用");
        }

        // 4. 生成新的访问令牌
        String newAccessToken = jwtUtil.generateAccessToken(username, userId, user.getRole());

        // 5. 存储新的访问令牌
        Long expiration = jwtUtil.getRemainingTime(newAccessToken);
        String accessKey = ACCESS_TOKEN_PREFIX + newAccessToken;
        redisTemplate.opsForValue().set(accessKey, userId.toString(),
                expiration, TimeUnit.MILLISECONDS);

        // 6. 构建返回对象
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        log.info("刷新Token成功: userId={}", userId);

        return LoginVO.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // 刷新令牌不变
                .tokenType("Bearer")
                .expiresIn(expiration / 1000)
                .userInfo(userVO)
                .build();
    }

    @Override
    public void logout(String accessToken, Long userId) {
        // 1. 删除刷新令牌
        String refreshKey = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.delete(refreshKey);

        // 2. 将访问令牌加入黑名单
        String blacklistKey = TOKEN_BLACKLIST_PREFIX + accessToken;
        Long remainingTime = jwtUtil.getRemainingTime(accessToken);
        if (remainingTime > 0) {
            redisTemplate.opsForValue().set(blacklistKey, userId.toString(),
                    remainingTime, TimeUnit.MILLISECONDS);
        }

        // 3. 删除访问令牌缓存
        String accessKey = ACCESS_TOKEN_PREFIX + accessToken;
        redisTemplate.delete(accessKey);

        log.info("用户登出成功: userId={}", userId);
    }

    @Override
    public RestBean<Object> verifyToken(String token) {
        // 1. 验证 Token 格式和签名
        if (!jwtUtil.validateToken(token)) {
            return RestBean.fail(401, "Token无效或已过期");
        }

        // 2. 检查黑名单
        String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
            return RestBean.fail(401, "Token已被注销");
        }

        // 3. 检查 Redis 缓存
        String accessKey = ACCESS_TOKEN_PREFIX + token;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(accessKey))) {
            return RestBean.fail(401, "Token已失效");
        }

        // 4. 解析用户信息
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);

        // 5. 返回验证结果
        return RestBean.success("验证通过", java.util.Map.of(
                "userId", userId,
                "username", username,
                "role", role,
                "valid", true
        ));
    }

}
