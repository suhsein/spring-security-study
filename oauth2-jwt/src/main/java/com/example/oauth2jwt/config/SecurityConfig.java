package com.example.oauth2jwt.config;

import com.example.oauth2jwt.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // csrf disable
        http
                .csrf((csrf) -> csrf.disable());
        // form login disable
        http
                .formLogin((form) -> form.disable());
        // http basic disable
        http
                .httpBasic((basic) -> basic.disable());
        // OAuth2
        http.oauth2Login((oauth2) -> oauth2
                .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                        .userService(customOAuth2UserService))
                .loginPage("/login"));
        /**
         * userInfoEndpoint 설정 : 커스텀 서비스를 선택
         * 1. 액세스 토큰 사용해 유저정보 얻어오기  -> 얘는 default 구현체를 상속하므로, default 에서 loadUser() 로 처리됨
         * 2. 받아온 정보 매핑하기 -> 커스텀 서비스에서 구현한 내용
         */

        // 경로별 인가 작업
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/", "/login").permitAll()
                        .requestMatchers("/my").hasRole("USER")
                        .anyRequest().authenticated());
        // 세션 설정 STATELESS (JWT 사용하기 때문에)
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
