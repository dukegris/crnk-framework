package io.crnk.core.engine.internal.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UrlUtilsTest {

	@Test
	public void testPrivateConstructor() {
		CoreClassTestUtils.assertPrivateConstructor(UrlUtils.class);
	}

	@Test
	public void testRemoveTrailingSlash() {
		Assertions.assertNull(UrlUtils.removeTrailingSlash(null));
		Assertions.assertEquals("/test", UrlUtils.removeTrailingSlash("/test/"));
		Assertions.assertEquals("test", UrlUtils.removeTrailingSlash("test/"));
		Assertions.assertEquals("/test", UrlUtils.removeTrailingSlash("/test"));
		Assertions.assertEquals("test", UrlUtils.removeTrailingSlash("test"));
	}

	@Test
	public void testRemoveLeadingSlash() {
		Assertions.assertNull(UrlUtils.removeLeadingSlash(null));
		Assertions.assertEquals("test/", UrlUtils.removeLeadingSlash("/test/"));
		Assertions.assertEquals("test/", UrlUtils.removeLeadingSlash("test/"));
		Assertions.assertEquals("test", UrlUtils.removeLeadingSlash("/test"));
		Assertions.assertEquals("test", UrlUtils.removeLeadingSlash("test"));
	}
}
