package io.crnk.meta;

import io.crnk.client.module.ClientModuleFactory;
import io.crnk.core.module.Module;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.ServiceLoader;

public class MetaClientModuleFactoryTest {

	@Test
	public void test() {
		ServiceLoader<ClientModuleFactory> loader = ServiceLoader.load(ClientModuleFactory.class);
		Iterator<ClientModuleFactory> iterator = loader.iterator();
		Assertions.assertTrue(iterator.hasNext());
		ClientModuleFactory moduleFactory = iterator.next();
		Assertions.assertFalse(iterator.hasNext());
		Module module = moduleFactory.create();
		Assertions.assertTrue(module instanceof MetaModule);
	}
}
