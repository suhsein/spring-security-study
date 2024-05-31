package com.example.test_security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // SecurityFilterChain 빈으로 등록. 인가 작업
        // requestMatchers 의 url 패턴은 와일드 카드 패턴 참고
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/", "/login", "loginProc","/join", "/joinProc").permitAll()
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .requestMatchers("/my/**").hasAnyRole("ADMIN", "USER")
                        .anyRequest().authenticated());

        // 권한 없는 페이지에 대해서 로그인 페이지를 보여줄 수 있도록 함.
        // .loginPage() 로그인 페이지 url
        // .loginProcessingUrl() 로그인 폼의 action url
        http
                .formLogin((auth) -> auth
                        .loginPage("/login")
                        .loginProcessingUrl("/loginProc")
                        .permitAll());

        // 개발 환경에서만 꺼두기
        http
                .csrf((auth) -> auth.disable());

        return http.build();
    }
}
