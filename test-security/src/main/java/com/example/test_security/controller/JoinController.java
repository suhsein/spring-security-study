package com.example.test_security.controller;

import com.example.test_security.dto.JoinDto;
import com.example.test_security.service.JoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class JoinController {
    private final JoinService joinService;

    @GetMapping("/join")
    public String joinP() {
        return "join";
    }

    @PostMapping("/joinProc")
    public String join(JoinDto joinDto) {
        joinService.joinProcess(joinDto);
        return "redirect:/login";
    }
}
