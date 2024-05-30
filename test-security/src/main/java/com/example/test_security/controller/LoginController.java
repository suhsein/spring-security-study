package com.example.test_security.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class LoginController {
    @GetMapping("/login")
    public String loginP(){
        return "/login";
    }

}
