package com.example.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class SpaController {

    @GetMapping(value = {
        "/",
        "/recipes",
        "/recipes/{path:[^\\.]*}",
        "/meal-plan",
        "/meal-plan/{path:[^\\.]*}",
        "/shopping-list",
        "/shopping-list/{path:[^\\.]*}",
        "/couple",
        "/couple/{path:[^\\.]*}",
        "/{path:[^\\.]*}"
    })
    public String forward(@PathVariable(required = false) String path) {
        return "forward:/index.html";
    }
}
