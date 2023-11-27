package io.crnk.validation;

import io.crnk.test.mock.ClassTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ValidationModuleTest {

	@Test
	public void hasProtectedConstructor() {
		ClassTestUtils.assertProtectedConstructor(ValidationModule.class);
	}

	@Test
	public void testName() {
		Assertions.assertEquals("validation", ValidationModule.create().getModuleName());
	}
}
