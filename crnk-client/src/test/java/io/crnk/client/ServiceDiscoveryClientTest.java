package io.crnk.client;

import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.test.mock.models.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;

public class ServiceDiscoveryClientTest {

	@Test
	public void notUsedInConstructor() throws NoSuchFieldException, IllegalAccessException {
		CrnkClient client = new CrnkClient("");
		Field field = CrnkClient.class.getDeclaredField("serviceDiscovery");
		field.setAccessible(true);
		Object serviceDiscovery = field.get(client);
		Assertions.assertNull(serviceDiscovery);
		Assertions.assertNotNull(client.getServiceDiscovery());
	}

	@Test
	public void allowOverrideServiceDiscovery() {
		ServiceDiscovery mock = Mockito.mock(ServiceDiscovery.class);
		CrnkClient client = new CrnkClient("");
		client.setServiceDiscovery(mock);
		Assertions.assertSame(mock, client.getServiceDiscovery());
		client.getRepositoryForType(Task.class);
		Assertions.assertSame(mock, client.getServiceDiscovery());
	}
}
