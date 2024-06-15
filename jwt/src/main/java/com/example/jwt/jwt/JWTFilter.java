package com.example.jwt.jwt;

import com.example.jwt.dto.CustomUserDetails;
import com.example.jwt.entity.UserEntity;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

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
        String accessToken = request.getHeader("access");

        // 토큰이 없으면 다음 필터로 넘김
        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 만료 여부 확인, 만료 시 다음 필터로 넘기지 않음
        try{
            jwtUtil.isExpired(accessToken);
        } catch(ExpiredJwtException e){
            // response body
            PrintWriter writer = response.getWriter();
            writer.print("access token is expired");

            // response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 토큰이 access 인지 확인 (발급 시 페이로드에 명시)
        String category = jwtUtil.getCategory(accessToken);
        if (!category.equals("access")) {
            // response body
            PrintWriter writer = response.getWriter();
            writer.print("invalid access token");

            // response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }


        // username, role 값을 획득
        String username = jwtUtil.getUsername(accessToken);
        String role = jwtUtil.getRole(accessToken);

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setRole(role);
        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}
