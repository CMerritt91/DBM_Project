package dbmproject.controllers;

import org.springframework.web.bind.annotation.GetMapping;

public class loginController {
    @GetMapping("/login")
    public String login() { 
        return "login"; }
}
