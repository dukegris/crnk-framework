package io.crnk.spring.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SpringSecurityModuleTest {

	@Test
	public void checkName() {
		SpringSecurityModule module = SpringSecurityModule.create();
		Assertions.assertEquals("spring.security", module.getModuleName());
	}
}
