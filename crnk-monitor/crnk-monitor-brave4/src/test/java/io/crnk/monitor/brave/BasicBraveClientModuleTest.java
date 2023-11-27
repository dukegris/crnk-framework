package io.crnk.monitor.brave;

import brave.Tracing;
import io.crnk.client.http.HttpAdapter;
import io.crnk.test.mock.ClassTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class BasicBraveClientModuleTest {

	private BraveClientModule module;

	private Tracing tracing;

	@BeforeEach
	public void setup() {
		tracing = Mockito.mock(Tracing.class);
		module = BraveClientModule.create(tracing);
	}

	@Test
	public void hasProtectedConstructor() {
		ClassTestUtils.assertProtectedConstructor(BraveClientModule.class);
	}

	@Test
	public void testGetName() {
		Assertions.assertEquals("brave-client", module.getModuleName());
	}

	@Test
	public void testSetInvalidAdapter() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {		
			module.setHttpAdapter(Mockito.mock(HttpAdapter.class));
		});
	}

	@Test
	public void testGetBrave() {
		Assertions.assertSame(tracing, module.getHttpTracing().tracing());
	}
}
