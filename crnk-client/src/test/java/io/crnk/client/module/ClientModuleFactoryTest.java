package io.crnk.client.module;

import io.crnk.client.CrnkClient;
import io.crnk.core.engine.information.resource.ResourceInformationProviderModule;
import io.crnk.core.engine.internal.jackson.JacksonModule;
import io.crnk.core.module.Module;
import io.crnk.test.mock.ClientTestModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ClientModuleFactoryTest {

	private void checkSharedAssertions(CrnkClient client) {
		client.findModules();
		List<Module> modules = client.getModuleRegistry().getModules();

		Assertions.assertEquals(4, modules.size());
		Assertions.assertEquals(ClientModule.class, modules.get(0).getClass());
		Assertions.assertEquals(ResourceInformationProviderModule.class, modules.get(1).getClass());
		Assertions.assertEquals(JacksonModule.class, modules.get(2).getClass());
		Assertions.assertEquals(ClientTestModule.class, modules.get(3).getClass());
	}

	@Test
	public void shouldDiscoverModules() {
		CrnkClient client = new CrnkClient("http://something");
		checkSharedAssertions(client);
	}

	@Test
	public void shouldDiscoverObjectLinkModules() {
		CrnkClient client = new CrnkClient("http://something", CrnkClient.ClientType.OBJECT_LINKS);
		checkSharedAssertions(client);
	}
}
