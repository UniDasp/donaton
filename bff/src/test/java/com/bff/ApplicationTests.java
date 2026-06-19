package com.bff;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.cloud.discovery.enabled=false",
    "spring.cloud.openfeign.circuitbreaker.enabled=false"
})
class ApplicationTests {

	@Test
	void contextLoads() {
	}

}
