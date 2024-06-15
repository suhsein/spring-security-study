package com.example.jwt.jwt;

import com.example.jwt.entity.RefreshEntity;
import com.example.jwt.repository.RefreshRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Date;

@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;

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
         // 유저 정보
        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        Long refreshExpiredMs = 60 * 60 * 24 * 1000L;

        // 토큰 생성
        String accessToken = jwtUtil.createJwt("access", username, role, 10*60*1000L);
        String refreshToken = jwtUtil.createJwt("refresh", username, role, refreshExpiredMs);

        // Refresh 토큰 저장
        addRefreshEntity(username, refreshToken, refreshExpiredMs);

        response.addHeader("access", accessToken);
        response.addCookie(createCookie("refresh", refreshToken));
        response.setStatus(HttpStatus.OK.value());

        // 로그인 성공 시 여기서 응답 반환. 더 이상 필터 체인을 거칠 이유가 없음.
        // 그렇지 않으면 끝까지 도달 -> 컨트롤러에 매핑된 서블릿을 찾게 되는데 매핑 안해줬으므로 없음. => 오류 발생
    }

    // 로그인 실패 시 실행하는 메서드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        // 로그인 실패 시 401 코드 반환 : Unauthorized code
        response.setStatus(401);
    }

    private void addRefreshEntity(String username, String refresh, Long expiredMs) {
        Date date = new Date(System.currentTimeMillis() + expiredMs);

        RefreshEntity refreshEntity = new RefreshEntity();
        refreshEntity.setUsername(username);
        refreshEntity.setRefresh(refresh);
        refreshEntity.setExpiration(date.toString());

        refreshRepository.save(refreshEntity);
    }

    private Cookie createCookie(String key, String value){
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        cookie.setHttpOnly(true);
        return cookie;
    }
}
