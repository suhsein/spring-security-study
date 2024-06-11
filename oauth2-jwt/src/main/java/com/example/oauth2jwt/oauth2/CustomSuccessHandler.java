package com.example.oauth2jwt.oauth2;


import com.example.oauth2jwt.dto.CustomOAuth2User;
import com.example.oauth2jwt.jwt.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
        String username = customUserDetails.getUsername();
        String role = customUserDetails.getAuthorities().iterator().next().toString();

        String token = jwtUtil.createJwt(username, role, 60 * 60 * 60L);

        response.addCookie(new Cookie("Authorization", token));
        response.sendRedirect("http://localhost:3000/");
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);

        cookie.setMaxAge(60 * 60 * 60);

        // https. 개발 환경에서는 X
        // cookie.setSecure(true);
        //
        cookie.setPath("/");
        // XSS 방지
        cookie.setHttpOnly(true);

        return cookie;
    }
}
