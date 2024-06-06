package com.example.innerstructure.config;

import com.example.innerstructure.filter.CustomGenericFilter;
import com.example.innerstructure.filter.CustomOnceFilter;
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
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    /**
     * Multi SecurityFilterChain
     * <p>
     * 등록 : SecurityFilterChain 타입의 빈을 여러개 등록하면 됨
     * 선택 : 스프링이 선택하는 기준
     * 1. 등록된 인덱스 순서대로
     * 2. 시큐리티 필터 체인에 대한 RequestMatcher 값이 일치하는지 확인 (인가 설정이 아님.)
     * 3. @Order 어노테이션으로 순서 설정도 가능. 파라미터로 정수. 낮은 숫자부터 먼저 등록됨
     * <p>
     * 예시 : 0번 시큐리티 필터 체인에 /user 설정, 1번 시큐리티 필터 체인에 /admin 설정
     * 하지만 /admin 접근 불가
     * <p>
     * -> 원인 : 시큐리티 필터 체인의 범위 (RequestMatcher) 따로 설정되지 않음.
     * 0번 시큐리티 필터 체인이 먼저 반응함. 하지만 /admin 과 관련된 설정이 없었음. -> 요청 거부됨
     * -> 해결 : 시큐리티 필터 체인의 RequestMatcher 설정.
     */
   /* @Bean
    public SecurityFilterChain securityFilterChain1(HttpSecurity http) throws Exception{
        http
                .securityMatchers((auth) -> auth.requestMatchers("/user"));

        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/user").permitAll());
        return http.build();
    }
    @Bean
    public SecurityFilterChain securityFilterChain2(HttpSecurity http) throws Exception{
        http.securityMatchers((auth) -> auth.requestMatchers("/admin"));

        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/admin").permitAll());

        return http.build();
    }*/

    /**
     * GenericFilterBean -> 내부적으로 필터를 여러 번 통과하더라도, 통과한 수만큼 내부 로직이 실행된다.
     * OncePerRequestFilter -> 내부적으로 동일한 필터를 여러 번 통과하더라도 첫 한 번만 내부 로직이 실행된다.
     *
     * !!!주의!!! : OncePerRequestFilter 는 요청 수에 따라 1번의 내부 로직 수행.
     *              redirect(302 응답)는 사용자에게 재요청을 보내라고 응답을 주기 때문에, 사용자의 요청이 2번 보내지는 것과 동일하다.
     *              즉, OncePerRequestFilter 가 의미하는 동작을 이루기 위해서는 redirect 시에는 해당이 안 되고,
     *              forward 시에만 해당된다.
     */
   /* @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((auth) -> auth.anyRequest().permitAll());

        http
                .addFilterAfter(new CustomGenericFilter(), LogoutFilter.class);

        http
                .addFilterAfter(new CustomOnceFilter(), LogoutFilter.class);

        return http.build();
    }*/

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http.authorizeHttpRequests((auth) -> auth
                .requestMatchers("/", "/login", "/loginProc", "/logout").permitAll()
                .requestMatchers("/admin").hasRole("ADMIN")
                .anyRequest().authenticated());

        http
                .formLogin((auth) -> auth
                        .loginPage("/login")
                        .loginProcessingUrl("/loginProc")
                        .permitAll());

        /**
         * 기본적으로 /logout 에 대해, Get/Post 방식으로 로그아웃 진행
         * Get : /logout 접근 시 매핑된 뷰를 보여줌
         * Post : 실제 로그아웃 작업
         *
         * 주의 : csrf 토큰이 켜져있어야, Get 에서 로그아웃 확인 페이지로 접근 가능.
         *       꺼져있으면 확인 페이지를 보여주지 않고 바로 로그아웃 됨.
         *
         * /logout 엔드 포인트에 접근 가능하도록, authorizeHttpRequests 로 인가작업 필요함
         *
         * 1. 커스텀 로그아웃 URI
         *   => logoutUrl() 로 지정 가능함. 역시 인가작업 필요함
         * 2. 로그아웃 성공 후 URI
         *   => 반드시 지정해야 함.
         */
        http.
                logout((auth) -> auth
                        .logoutSuccessUrl("/"));

        /**
         * Customizer.withDefaults() 를 통해 default login, default logout 페이지를 띄울 수 있다.
         * DefaultLoginPageGeneratingFilter, DefaultLogoutPageGeneratingFilter 기본 활성화되어 있기 때문이다.
         * 주의 : csrf 설정이 켜져 있어야만 함.
         */
       /* http
                .formLogin(Customizer.withDefaults());*/


        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user1 = User.builder()
                .username("abc")
                .password(bCryptPasswordEncoder().encode("123"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user1);
    }
}
