package com.urjcservice.backend.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserControler {
    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    
    @GetMapping("/private")
    public String privatePage() {
        return "private";
    }
}
