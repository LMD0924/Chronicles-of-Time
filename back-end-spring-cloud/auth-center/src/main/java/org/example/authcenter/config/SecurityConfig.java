package org.example.authcenter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/*
 * @Author:总会落叶
 * @Date:2026/3/19
 * @Description:
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 密码加密器（用于注册登录时的密码加密和比对）
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 关闭 CSRF
                .csrf(csrf -> csrf.disable())

                // ⭐ 关键：放行所有请求，不拦截任何接口
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()  // 所有请求都放行
                )

                // 关闭表单登录
                .formLogin(form -> form.disable())

                // 关闭 HTTP Basic 认证
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}