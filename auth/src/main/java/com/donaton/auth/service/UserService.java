package com.donaton.auth.service;

import com.donaton.auth.model.User;
import com.donaton.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.donaton.auth.security.JwtService;

@Service
public class UserService {

    private final UserRepository repository;

    private final JwtService jwtService;

    public UserService(UserRepository repository, JwtService jwtService) {
        this.repository = repository;
        this.jwtService = jwtService;
    }

<<<<<<< HEAD
=======

>>>>>>> 57e09ad6a70872f9ecd709579f6093d39f15fae3
    public User registrar(User user) {
        return repository.save(user);
    }

<<<<<<< HEAD
    public User login(String email, String password) {
=======
    public String login(String email, String password) {
>>>>>>> 57e09ad6a70872f9ecd709579f6093d39f15fae3

        User user = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Contraseña incorrecta");
        }

<<<<<<< HEAD
        return user;
=======
        String token = jwtService.generateToken(user.getEmail());

        System.out.println("TOKEN GENERADO: " + token); // 👈 aquí

        return token;
>>>>>>> 57e09ad6a70872f9ecd709579f6093d39f15fae3
    }
}