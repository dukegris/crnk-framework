package io.crnk.core.engine.http;

import io.crnk.core.engine.internal.utils.CoreClassTestUtils;
import org.junit.jupiter.api.Test;

public class HttpStatusTest {

	@Test
	public void testPrivateConstructor() {
		CoreClassTestUtils.assertPrivateConstructor(HttpStatus.class);
	}
}
