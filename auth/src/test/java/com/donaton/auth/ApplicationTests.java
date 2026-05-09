package com.donaton.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // <--- Esto buscará application-test.properties
class ApplicationTests {

	@Test
	void contextLoads() {
		// Esta prueba ahora pasará porque H2 se creará en memoria
	}

}