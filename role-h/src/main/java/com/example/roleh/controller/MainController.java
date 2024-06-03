package com.example.roleh.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Iterator;

@Controller
public class MainController {
    @GetMapping("/")
    public String mainP(Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String id = authentication.getName();

        Iterator<? extends GrantedAuthority> iter = authentication.getAuthorities().iterator();
        GrantedAuthority auth = iter.next();

        String role = auth.getAuthority();

        model.addAttribute("id", id);
        model.addAttribute("role", role);
        return "main";
    }
}
