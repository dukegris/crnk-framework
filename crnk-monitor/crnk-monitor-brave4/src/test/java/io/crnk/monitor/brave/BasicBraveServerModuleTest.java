package io.crnk.monitor.brave;

import brave.Tracing;
import io.crnk.test.mock.ClassTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class BasicBraveServerModuleTest {

	private BraveServerModule module;

	private Tracing tracing;

	@BeforeEach
	public void setup() {
		tracing = Mockito.mock(Tracing.class);
		module = BraveServerModule.create(tracing);
	}

	@Test
	public void hasProtectedConstructor() {
		ClassTestUtils.assertProtectedConstructor(BraveServerModule.class);
	}

	@Test
	public void testGetName() {
		Assertions.assertEquals("brave-server", module.getModuleName());
	}

	@Test
	public void testGetBrave() {
		Assertions.assertSame(tracing, module.getTracing());
	}
}
