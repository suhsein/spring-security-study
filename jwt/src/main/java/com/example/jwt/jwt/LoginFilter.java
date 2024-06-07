package com.example.jwt.jwt;

import ch.qos.logback.core.spi.ErrorCodes;
import com.example.jwt.dto.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    // 검증 : 인증 정보 추출 후, AuthenticationManager 검증 메서드 호출할 때 인증 정보로부터 생성한 토큰을 넘겨주면 됨.
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        // 클라이언트 요청에서 username, password 추출
        String username = obtainUsername(request);
        String password = obtainPassword(request);

        System.out.println("username = " + username);

        // 스프링 시큐리티에서 username 과 password 를 검증하기 위해서는 token 에 담아야 함.
        // UsernamePasswordAuthenticationToken 는 일종의 DTO 이다.
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);

        // token 에 담은 값을 검증을 위한 AuthenticationManager 로 전달. 성공 시 Authentication 반환
        return authenticationManager.authenticate(authToken);
    }

    // 로그인 성공 시 실행하는 메서드 (여기서 JWT 를 발급하면 됨)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        /**
         *
         * 1. 인증 성공 후, principal 을 읽기
         * ProviderManager -> Authentication Providers -> 그 중 하나가 DaoAuthenticationProvider => 인증 작업 수행
         *
         * DaoAuthenticationProvider 에 의한 인증 후 반환 값 = Authentication 객체
         * Authentication 의 principal = user 식별 객체 = UserDetails
         * (username/password 로 인증을 할 때, principal 은 주로 UserDetails 객체가 됨.)
         *
         * -> Authentication 의 principal 을 CustomUserDetails 객체로 변환 후,
         *    CustomUserDetails 의 메서드를 사용해 값을 가져올 수 있도록 한다.
         *
         * 2. JWT 발급 받기
         * Jwts 사용해 JWTUtil 클래스의 메서드들 만들었음. (페이로드를 가져오는 메서드, JWT 발급 메서드)
         * 이 중 JWT 발급 메서드를 사용해 토큰을 생성한다.
         *
         * 그리고 응답할 때 Authorization 헤더에 JWT 를 포함시킬 때는 반드시 인증 스킴을 앞에 명시해줘야 한다.
         * 현재 JWT 를 사용하고, 토큰 기반의 인증을 하기 때문에 토큰 기반의 인증을 제공하는 Bearer 를 붙여주면 된다.
         * (즉, Authorization: "Bearer {JWT 값}")
         *
         */

        // UserDetails
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        String username = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = customUserDetails.getAuthorities();
        Iterator<? extends GrantedAuthority> iter = authorities.iterator();
        GrantedAuthority auth = iter.next();

        String role = auth.getAuthority();

        String token = jwtUtil.createJwt(username, role, 60 * 60 * 10L);

        response.addHeader("Authorization", "Bearer " + token);
    }

    // 로그인 실패 시 실행하는 메서드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        // 로그인 실패 시 401 코드 반환 : Unauthorized code
        response.setStatus(401);
    }
}
