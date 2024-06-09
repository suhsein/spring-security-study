package com.example.oauth2session.config;

import com.example.oauth2session.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;


/**
 * 각 필터가 동작하는 주소(관습)
 * * OAuth2AuthorizationRequestRedirectFilter
 *   : "/oauth2/authorization/서비스명"
 * * OAuth2LoginAuthenticationFilter : 외부 인증 서버에 설정할 redirect_url
 *   : "/login/oauth2/code/서비스명"
 *
 * 인증 과정
 *
 * 1. 로그인 시도
 * "/oauth2/authorization/서비스명"
 * => 로그인 시도 url. 외부 소셜 로그인 지원 서비스로 이동하고, 해당 서비스 로그인 페이지 응답.
 *   로그인 성공하면 미리 등록한 우리 서버 특정 경로로 리다이렉트
 *
 * 2. 로그인 성공 시 인증 서버로부터 토큰 발급
 * "/login/oauth2/code/서비스명"
 * => 로그인 성공 시 redirect url 로써 외부 소셜 로그인 지원 서비스의 인증 서버에서 Code 를 날려주는 주소이다.
 *   해당 url 에 대해 OAuth2LoginAuthenticationFilter 가 처리함.
 *   받은 Code + 등록 정보를 가지고, 외부 소셜 로그인 지원 서비스로부터 Access 토큰을 발급 받는다.
 *
 * 3. 리소스 서버로부터 유저 정보 가져오기
 * => OAuth2LoginAuthenticationProvider 가 Access 토큰을 사용한다.
 *   외부 소셜 로그인 지원 서비스의 인증 서버의 리소스 서버로부터 발급받은 Access 토큰을 사용하여 유저 정보를 획득한다.
 *   리소스 서버는 토큰을 검증하기 위해 토큰에 유저 정보를 더해서 돌려준다.
 *
 * 4. 토큰 + 유저 정보 처리 후 로그인 완료.
 * => OAuth2UserDetailsService 에서 받은 토큰 + 유저 정보를 처리하고, OAuth2UserDetails 에 넘겨서 로그인을 마무리.
 *   이후에 세션 저장과 같은 나머지 시큐리티 로직(필터)들이 동작한다.
 *
 *
 *  oauth2client, oauth2login 방식이 있음. oauth2client 방식은 각각의 필터와 내부 등록 정보들 일일이 세팅 필요
 *  하지만 oauth2login 방식 사용 시
 *  1,2,3 번은 특정 변수만 등록하면 인증 과정 자체가 캡슐화 되어있어서 자동으로 동작함.
 *  그렇기 때문에 4번의 OAuth2UserDetailsService, OAuth2UserDetails 만 구현해주면 된다.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .csrf((csrf) -> csrf.disable());
        http
                .formLogin((login) -> login.disable());
        http
                .httpBasic((basic) -> basic.disable());
        http
                .oauth2Login((oauth2) -> oauth2
                        .loginPage("/login")
                        .clientRegistrationRepository(clientRegistrationRepository)
                        .authorizedClientService(oAuth2AuthorizedClientService) // jdbc 방식 서비스
                        .userInfoEndpoint((userInfoEndpointConfig) ->
                                userInfoEndpointConfig.userService(customOAuth2UserService)));
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/", "/oath2/**", "/login/**").permitAll()
                        .anyRequest().authenticated());

        return http.build();
    }
}
