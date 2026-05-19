package com.donaton.auth;

import com.donaton.auth.model.Role;
import com.donaton.auth.model.User;
import com.donaton.auth.repository.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
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
	CommandLineRunner seedTestUsers(UserRepository userRepository, JdbcTemplate jdbcTemplate) {
		return args -> {
			ensureRoleConstraint(jdbcTemplate);
			seedUser(userRepository, "admin@donaton.test", "admin123", Role.ADMIN);
			seedUser(userRepository, "user@donaton.test", "user123", Role.USER);
			seedUser(userRepository, "ong@donaton.test", "ong123", Role.ONG);
		};
	}

	private void ensureRoleConstraint(JdbcTemplate jdbcTemplate) {
		// Hibernate ddl-auto=update no actualiza CHECK constraints existentes. En entornos dev,
		// aseguramos que el constraint de role acepte todos los roles actuales.
		try {
			jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check");
			jdbcTemplate.execute("ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN ('USER','ADMIN','ONG'))");
		} catch (Exception ignored) {
			// Si la tabla/constraint no existe aún, o el usuario no tiene permisos, ignoramos.
		}
	}

	private void seedUser(UserRepository userRepository, String email, String password, Role role) {
		userRepository.findByEmail(email)
				.orElseGet(() -> userRepository.save(new User(null, email, password, role)));
	}

}
