package com.example.innerstructure.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class MainController {
  /*  @GetMapping("/user")
    public String userP() {
        return "user page";
    }

    @GetMapping("/admin")
    public String adminP() {
        return "admin page";
    }*/

    @GetMapping("/testfilterbefore")
    public String testFilterBefore() {
        return "redirect:/testfilterafter";
    }

    @GetMapping("/testfilterafter")
    @ResponseBody
    public String testFilterAfter(){
        return "hi hi hi";
    }
}
