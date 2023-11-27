package io.crnk.core.engine.internal.document.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DocumentMappingConfigTest {

	@Test
	public void checkAccessors() {
		DocumentMappingConfig config = new DocumentMappingConfig();
		Assertions.assertNotNull(config.getResourceMapping());

		ResourceMappingConfig resourceMappingConfig = new ResourceMappingConfig();
		config.setResourceMapping(resourceMappingConfig);
		Assertions.assertSame(resourceMappingConfig, config.getResourceMapping());
	}
}
