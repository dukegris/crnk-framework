package io.crnk.meta;

import io.crnk.meta.internal.MetaUtils;
import io.crnk.test.mock.ClassTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MetaUtilsTest {

	@Test
	public void noPublicDefaultConstrcutor() {
		ClassTestUtils.assertPrivateConstructor(MetaUtils.class);
	}

	@Test
	public void testFirstToLower() {
		Assertions.assertEquals("test", MetaUtils.firstToLower("test"));
		Assertions.assertEquals("test", MetaUtils.firstToLower("Test"));
		Assertions.assertEquals("", MetaUtils.firstToLower(""));
	}
}
