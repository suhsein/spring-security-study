package com.example.jwt.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
    @GetMapping("/")
    public String mainP(){
        // JWT 필터 통과 후 세션 확인
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String name = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        return "main Controller : " + name + role;
    }
}
