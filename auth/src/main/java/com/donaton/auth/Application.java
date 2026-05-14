package com.donaton.auth;

import com.donaton.auth.model.Role;
import com.donaton.auth.model.User;
import com.donaton.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	CommandLineRunner seedTestUsers(UserRepository userRepository) {
		return args -> {
			seedUser(userRepository, "admin@donaton.test", "admin123", Role.ADMIN);
			seedUser(userRepository, "user@donaton.test", "user123", Role.USER);
		};
	}

	private void seedUser(UserRepository userRepository, String email, String password, Role role) {
		userRepository.findByEmail(email)
				.orElseGet(() -> userRepository.save(new User(null, email, password, role)));
	}

}
