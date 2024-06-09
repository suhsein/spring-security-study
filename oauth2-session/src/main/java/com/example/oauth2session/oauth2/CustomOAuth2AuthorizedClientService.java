package com.example.oauth2session.oauth2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

/**
 * 소셜 로그인을 하는 사용자 -> 외부 서비스의 인증 서버로부터 발급받은 액세스 토큰을 저장해야 함.
 * 기본적으로 OAuth2AuthorizedClientService 가 인메모리로 액세스 토큰과 같은 정보들을 저장하도록 함.
 *
 * => 사용자가 많아질 경우, 스케일 아웃과 관련된 문제.
 *    서버에 인메모리로 저장하는 것이 아니라, DB를 사용해서 저장해야 한다.
 *
 * 결국 OAuth2AuthorizedClientService 를 커스텀 해야한다.
 * JPA 를 사용하려면, 전부 커스텀 해야함.
 * 대신 JDBC 방식은 구현체가 존재하므로, JDBC 의존성을 받고 JDBC 방식의 서비스 빈을 리턴하도록 등록함.
 *
 * 문제점 : JDBC 방식일 때, 서비스 안에서 { client_registration_id, principal_name } 을 pk로 잡음
 * client_registration_id 값은 naver, google 과 같은 값, principal_name 은 사용자의 이름 혹은 닉네임
 *
 * => 같은 서비스를 이용하는 같은 이름 혹은 닉네임을 가진 사용자가 두 명 이상이라면, 구분 X
 *    결국 pk 가 중복되므로 덮어씌워져 버린다.
 *
 * 이 부분과 관련된 답변은 CustomOAuth2AuthorizedService 를 직접 구현하는 것이라고 한다.
 * pk 를 다른 값으로 잡을 수 있도록 해야함.
 */
@Configuration
public class CustomOAuth2AuthorizedClientService {
    // jdbc 템플릿과, clientRegistrationRepository 를 주입 받음.
    // (수동 빈으로 clientRegistrationRepository 를 등록했고, 구현체로 인메모리 repo 를 사용하도록 했었음.)
    @Bean
    public OAuth2AuthorizedClientService oAuth2AuthorizedClientService(JdbcTemplate jdbcTemplate, ClientRegistrationRepository clientRegistrationRepository) {
        return new JdbcOAuth2AuthorizedClientService(jdbcTemplate, clientRegistrationRepository);
    }

}
