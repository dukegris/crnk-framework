package io.crnk.gen.asciidoc;

import io.crnk.gen.asciidoc.internal.AsciidocUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AsciidocUtilsTest {

	@Test
	public void testHrefRewrite() {
		Assertions.assertEquals("Test http://www.google.com[Google]", AsciidocUtils.fromHtml("Test <a href=\"http://www.google.com\">Google</a>"));
	}
}
