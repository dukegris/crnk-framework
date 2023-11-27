package io.crnk.core.engine.internal.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CompareUtilsTest {

	@Test
	public void testPrivateConstructor() {
		CoreClassTestUtils.assertPrivateConstructor(CompareUtils.class);
	}

	@Test
	public void test() {
		Assertions.assertTrue(CompareUtils.isEquals("a", "a"));
		Assertions.assertFalse(CompareUtils.isEquals(null, "a"));
		Assertions.assertFalse(CompareUtils.isEquals("a", null));
		Assertions.assertTrue(CompareUtils.isEquals(null, null));
		Assertions.assertFalse(CompareUtils.isEquals("b", "a"));
	}

}
