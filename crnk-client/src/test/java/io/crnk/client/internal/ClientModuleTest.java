package io.crnk.client.internal;

import io.crnk.client.module.ClientModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClientModuleTest {

	@Test
	public void testName() {
		ClientModule module = new ClientModule();
		Assertions.assertEquals("client", module.getModuleName());
	}
}
