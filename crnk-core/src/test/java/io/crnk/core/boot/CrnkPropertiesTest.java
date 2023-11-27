package io.crnk.core.boot;

import io.crnk.core.engine.internal.utils.CoreClassTestUtils;
import org.junit.jupiter.api.Test;

public class CrnkPropertiesTest {


	@Test
	public void hasPrimvateConstructor() {
		CoreClassTestUtils.assertPrivateConstructor(CrnkProperties.class);
	}
}
