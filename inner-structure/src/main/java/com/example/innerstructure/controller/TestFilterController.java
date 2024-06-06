package com.example.innerstructure.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestFilterController {
    @GetMapping("/testfilterbefore")
    public String testFilterBefore() {
        return "redirect:/testfilterafter";
    }

    @GetMapping("/testfilterafter")
    @ResponseBody
    public String testFilterAfter() {
        return "hi hi hi";
    }
}
