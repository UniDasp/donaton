package com.donaton.auth.controller;

import com.donaton.auth.dto.AuthDTO;
import com.donaton.auth.model.User;
import com.donaton.auth.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService service;

    public AuthController(UserService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return service.registrar(user);
    }

    @PostMapping("/login")
    public String login(@RequestBody AuthDTO dto) {
        return service.login(dto.getEmail(), dto.getPassword()).toString();
    }
}