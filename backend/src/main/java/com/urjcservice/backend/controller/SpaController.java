package com.urjcservice.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Profile("!test")
public class SpaController {

    // captures all routes except those containing a period (.) or /api
    @GetMapping(value = "/**/{path:[^\\.]*}")
    public String forward(@PathVariable("path") String path) {
        // redirect to index.html so that Angular can handle the routing
        return "forward:/index.html";
    }
}