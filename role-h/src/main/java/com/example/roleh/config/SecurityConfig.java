package com.example.roleh.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
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

    /**
     * 계층 권한 빈 등록
     * 기존 setHierarchy() deprecated 됨
     */
    @Bean
    public static RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("""
                ROLE_C > ROLE_B
                ROLE_B > ROLE_A
                """);
    }

    /**
     * 권한 계층
     * A < B < C
     *
     * 설정해둔 !!!계층 권한 빈!!!을 보고 낮은 계층의 권한이 인가된 경우에는,
     * 그보다 높은 계층 권한에게도 자동으로 인가 가능
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/").hasRole("A")
                        .requestMatchers("/manager").hasRole("B")
                        .requestMatchers("/admin").hasRole("C")
                        .anyRequest().authenticated()
                );

        http
                .formLogin((auth) -> auth
                        .loginPage("/login")
                        .loginProcessingUrl("/loginProc")
                        .permitAll());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user1 = User.builder()
                .username("user1")
                .password(bCryptPasswordEncoder().encode("1234"))
                .roles("C")
                .build();

        UserDetails user2 = User.builder()
                .username("user2")
                .password(bCryptPasswordEncoder().encode("1234"))
                .roles("B")
                .build();

        UserDetails user3 = User.builder()
                .username("user3")
                .password(bCryptPasswordEncoder().encode("1234"))
                .roles("A")
                .build();

        return new InMemoryUserDetailsManager(user1, user2, user3);
    }
}
