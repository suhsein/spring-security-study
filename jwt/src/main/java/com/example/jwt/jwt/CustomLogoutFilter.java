package com.example.jwt.jwt;

import com.example.jwt.entity.RefreshEntity;
import com.example.jwt.repository.RefreshRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Arrays;

@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {
    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        // path and method verify
        String requestURI = request.getRequestURI();
        if (!requestURI.matches("^\\/logout$")) {
            chain.doFilter(request, response);
            return;
        }
        String requestMethod = request.getMethod();
        if(!requestMethod.equals("POST")){
            chain.doFilter(request, response);
            return;
        }

        // get refresh token
        String refresh = null;
        Cookie[] cookies = request.getCookies();

        refresh = Arrays.stream(cookies).filter((cookie) -> cookie.getName().equals("refresh"))
                .findFirst().get().getValue();

        System.out.println("refresh = " + refresh);


        // refresh null check
        if(refresh == null){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }


        // expired check
        try{
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 토큰이 refresh 인지 확인
        String category = jwtUtil.getCategory(refresh);
        if (!category.equals("refresh")) {
            System.out.println("not refresh");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // DB 에 저장되어 있는지 확인
        Boolean isExist = refreshRepository.existsByRefresh(refresh);
        System.out.println("isExist = " + isExist);

        if(!isExist){
            System.out.println("not exist in db");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        System.out.println("do logout");

        // 로그아웃 진행
        // Refresh 토큰 DB 에서 제거
        refreshRepository.deleteByRefresh(refresh);

        // Refresh 토큰 Cookie 삭제
        // max age 를 0 으로 하면 즉시 삭제. 이를 이용하여 클라이언트 브라우저의 쿠키를 삭제함
        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");

        response.addCookie(cookie);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
