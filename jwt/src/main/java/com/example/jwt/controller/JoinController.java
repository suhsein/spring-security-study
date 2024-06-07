package com.example.jwt.controller;

import com.example.jwt.dto.JoinDto;
import com.example.jwt.service.JoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class JoinController {
    private final JoinService joinService;

    @PostMapping("/join")
    public String joinProcess(JoinDto joinDto) {
        joinService.join(joinDto);
        return "ok";
    }
}
