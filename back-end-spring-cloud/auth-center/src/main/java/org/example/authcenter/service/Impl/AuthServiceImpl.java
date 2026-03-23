package org.example.authcenter.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.example.authcenter.dto.AuthDTO;
import org.example.authcenter.entity.User;
import org.example.authcenter.mapper.AuthMapper;
import org.example.authcenter.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
/*
 * @Author:总会落叶
 * @Date:2026/3/19
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl extends ServiceImpl<AuthMapper, User> implements AuthService {

    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    /**
     * 用户登录
     * @param authDTO
     * @return
     */
    @Override
    public String login(AuthDTO authDTO) {
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
        return "登录成功";
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
}
