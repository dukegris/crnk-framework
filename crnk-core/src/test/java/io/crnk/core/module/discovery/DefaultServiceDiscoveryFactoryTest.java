package io.crnk.core.module.discovery;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DefaultServiceDiscoveryFactoryTest {

	@Test
	public void test() {
		DefaultServiceDiscoveryFactory factory = new DefaultServiceDiscoveryFactory();
		ServiceDiscovery instance = factory.getInstance();
		Assertions.assertTrue(instance instanceof TestServiceDiscovery);
	}
}
