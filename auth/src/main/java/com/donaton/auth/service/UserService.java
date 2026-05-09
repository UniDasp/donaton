package com.donaton.auth.service;

import com.donaton.auth.model.User;
import com.donaton.auth.repository.UserRepository;
import com.donaton.auth.security.JwtService;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repository;
    private final JwtService jwtService;

    public UserService(UserRepository repository, JwtService jwtService) {
        this.repository = repository;
        this.jwtService = jwtService;
    }

    public User registrar(User user) {
        return repository.save(user);
    }

    public String login(String email, String password) {

        User user = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        String token = jwtService.generateToken(user.getEmail());

        System.out.println("TOKEN GENERADO: " + token);

        return token;
    }
}