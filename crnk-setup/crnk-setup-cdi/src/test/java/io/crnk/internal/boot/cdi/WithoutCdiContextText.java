package io.crnk.internal.boot.cdi;

import io.crnk.cdi.internal.CdiServiceDiscovery;
import io.crnk.core.repository.Repository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class WithoutCdiContextText {

	@Test
	public void checkNoCdiContext() {
		CdiServiceDiscovery discovery = new CdiServiceDiscovery();

		List<Repository> list = discovery.getInstancesByType(Repository.class);
		Assertions.assertTrue(list.isEmpty());
	}
}
