package com.example.test_security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
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

        /**
         * form 방식 로그인
         *
         * 권한 없는 페이지에 대해서 로그인 페이지를 보여줄 수 있도록 함.
         * .loginPage() 로그인 페이지 url
         * .loginProcessingUrl() 로그인 폼의 action url
         */
      /*  http
                .formLogin((auth) -> auth
                        .loginPage("/login")
                        .loginProcessingUrl("/loginProc")
                        .permitAll());*/

        /**
         * http basic 방식 인증
         *
         * 권한 없는 페이지에 대하여 form 방식 대신 사용할 수 있음
         * 아이디와 비밀번호를 Base64 방식으로 인코딩한 후,
         * !!!HTTP 인증 헤더에 부착하여!!! 서버 측으로 요청을 보내는 방식
         *
         * 사용 예 :
         * 마이크로서비스 아키텍쳐 구현 시에 Eureka 서버나 config 서버는 private 망에서 통신하지만,
         * 더 엄격한 보안을 위해 httpBasic 방식의 인증 사용
         */
        http
                .httpBasic(Customizer.withDefaults());


        /**
         * csrf 개발 환경에서만 꺼두기
         * csrf 켜져 있으면, 로그인 전/후로 csrf 토큰을 필요로 하게 됨
         *
         * API 서버의 경우, 보통 세션을 STATELESS 로 관리하기 때문에 스프링 시큐리티 csrf 설정을 꺼둬도 된다.
         */
//        http
//                .csrf((auth) -> auth.disable());


        /**
         * 다중 로그인 설정
         * maximumSessions(정수) -> 하나의 아이디에 대한 다중 로그인 허용 개수
         * maximumSessionPreventsLogin(불린) -> 다중 로그인 개수를 초과했을 경우 처리 방법
         *      * true : 초과 시 새로운 로그인 차단
         *      * false : 초과 시 기존 세션 하나 삭제
         */
        http
                .sessionManagement((auth) -> auth
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(true));

        // 세션 고정 보호
        http
                .sessionManagement((auth)-> auth
                        .sessionFixation().changeSessionId());
        return http.build();
    }

}
