package com.bojan.bootcamp_01;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class Bootcamp01ApplicationTests {

	@Test
	void contextLoads() {
	}

}
