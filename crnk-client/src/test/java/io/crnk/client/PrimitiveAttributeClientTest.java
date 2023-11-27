package io.crnk.client;

import java.util.Optional;

import io.crnk.core.repository.ResourceRepository;
import io.crnk.test.mock.models.PrimitiveAttributeResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PrimitiveAttributeClientTest extends AbstractClientTest {


	private ResourceRepository<PrimitiveAttributeResource, Object> repository;

	@BeforeEach
	public void setup() {
		super.setup();

		repository = client.getRepositoryForType(PrimitiveAttributeResource.class);
	}

	@Test
	public void testOptionalEmpty() {
		PrimitiveAttributeResource attr = new PrimitiveAttributeResource();
		attr.setId(1L);
		attr.setOptionalValue(Optional.empty());
		PrimitiveAttributeResource created = repository.create(attr);
		Assertions.assertFalse(created.getOptionalValue().isPresent());
	}

	@Test
	public void testOptionalSet() {
		PrimitiveAttributeResource attr = new PrimitiveAttributeResource();
		attr.setId(1L);
		attr.setOptionalValue(Optional.of("Hello"));
		PrimitiveAttributeResource created = repository.create(attr);
		Assertions.assertEquals("Hello", created.getOptionalValue().get());
	}
}
