package com.example.innerstructure.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class MainController {
    @GetMapping("/")
    public String mainP(Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        GrantedAuthority auth = authentication.getAuthorities().iterator().next();
        String role = auth.getAuthority();
        String id = authentication.getName();

        model.addAttribute("id", id);
        model.addAttribute("role", role);
        return "main";
    }
}
