package io.crnk.core.module.discovery;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EmptyServiceDiscoveryTest {

	@Test
	public void test() {
		EmptyServiceDiscovery discovery = new EmptyServiceDiscovery();
		Assertions.assertEquals(0, discovery.getInstancesByType(null).size());
		Assertions.assertEquals(0, discovery.getInstancesByAnnotation(null).size());
	}
}
