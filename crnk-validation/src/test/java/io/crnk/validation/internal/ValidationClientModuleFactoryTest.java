package io.crnk.validation.internal;

import io.crnk.client.module.ClientModuleFactory;
import io.crnk.core.module.Module;
import io.crnk.meta.MetaModule;
import io.crnk.validation.ValidationModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;

public class ValidationClientModuleFactoryTest {

	@Test
	public void test() {
		ServiceLoader<ClientModuleFactory> loader = ServiceLoader.load(ClientModuleFactory.class);
		Iterator<ClientModuleFactory> iterator = loader.iterator();

		Set<Class> moduleClasses = new HashSet<>();
		while (iterator.hasNext()) {
			ClientModuleFactory moduleFactory = iterator.next();
			Module module = moduleFactory.create();
			moduleClasses.add(module.getClass());
		}

		Assertions.assertEquals(2, moduleClasses.size());
		Assertions.assertTrue(moduleClasses.contains(ValidationModule.class));
		Assertions.assertTrue(moduleClasses.contains(MetaModule.class));
	}
}
