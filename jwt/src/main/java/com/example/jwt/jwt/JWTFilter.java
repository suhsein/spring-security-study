package com.example.jwt.jwt;

import com.example.jwt.dto.CustomUserDetails;
import com.example.jwt.entity.UserEntity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;

    /**
     * 1. 요청 시 JWT 토큰을 가졌는지 확인. -> Authorization 헤더로부터 JWT 를 가져옴
     * 2. JWT 토큰을 검증을 위한 일시적 세션 저장
     *  만들어둔 JWTUtil 클래스의 검증 메서드들을 사용하여 principal 값을 가져오고,
     *  이를 사용해 UserDetails 객체를 만들어 SecurityContextHolder 에 저장한다. (세션 저장소)
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // request 에서 Authorization 헤더 찾음
        String authorization = response.getHeader("Authorization");

        // Authorization 헤더 검증
        if(authorization == null || !authorization.startsWith("Bearer ")){
            System.out.println("token null");
            filterChain.doFilter(request, response);

            return;
        }

        System.out.println("authorization now");

        // 인증 스킴 제거 후 토큰만 가져옴
        String token = authorization.split(" ")[1];

        // 토큰 소멸 시간 검증
        if(jwtUtil.getExpired(token)){
            System.out.println("token expired");
            filterChain.doFilter(request, response);

            return;
        }

        // 토큰에서 username 과 role 획득
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        // userEntity 를 생성하여 값 set
        // 비밀번호 값은 JWT 에 저장되지 않음. 하지만 password set 필요
        // 매번 DB 에 접근하는 것은 효율면에서 좋지 않고, 정확한 비밀번호를 저장할 필요도 없음
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setPassword("temppassword");
        userEntity.setRole(role);

        // UserDetails 에 회원 정보 객체 담기
        CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);

        // 스프링 시큐리티 인증 토큰 생성
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        // 세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}
