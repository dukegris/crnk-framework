package io.crnk.spring.setup.core;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.spring.app.BasicSpringBoot3Application;
import io.crnk.test.mock.models.RenamedIdResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = BasicSpringBoot3Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@TestPropertySource(properties = {"crnk.enforceIdName=false"})
public class SpringEnforceIdNameDisabledTest {

	@Autowired
	private ObjectProvider<ResourceRegistry> resourceRegistry;

	@Test
	public void check() {
		RegistryEntry entry = resourceRegistry.getIfAvailable().getEntry(RenamedIdResource.class);
		ResourceField idField = entry.getResourceInformation().getIdField();
		Assertions.assertEquals("notId", idField.getJsonName());
	}
}
