package io.crnk.core.engine.internal.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.internal.information.resource.RawResourceFieldAccessor;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.meta.MetaInformation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class RawResourceFieldAccessorTest {

	private Resource resource;

	@BeforeEach
	public void setup() throws IOException {
		String json = "{'id': 'someId', 'type': 'test', 'attributes': {'name': 'Doe'},'meta': {'name': 'someMeta'},'links': {'name': 'someLink'}, 'relationships': {'address': {'data': {'id':'zurich', 'type' : 'address'}}}}".replace('\'', '\"');

		ObjectMapper mapper = new ObjectMapper();
		resource = mapper.readerFor(Resource.class).readValue(json);
	}

	@Test
	public void attribute() {
		RawResourceFieldAccessor accessor = new RawResourceFieldAccessor("name", ResourceFieldType.ATTRIBUTE, String.class);
		Assertions.assertEquals("Doe", accessor.getValue(resource));
	}

	@Test
	public void id() {
		RawResourceFieldAccessor accessor = new RawResourceFieldAccessor("id", ResourceFieldType.ID, String.class);
		Assertions.assertEquals("someId", accessor.getValue(resource));
	}

	@Test
	public void relationship() {
		RawResourceFieldAccessor accessor = new RawResourceFieldAccessor("address", ResourceFieldType.RELATIONSHIP, ResourceIdentifier.class);
		ResourceIdentifier value = (ResourceIdentifier) accessor.getValue(resource);
		Assertions.assertEquals("zurich", value.getId());
	}

	@Test
	public void meta() {
		RawResourceFieldAccessor accessor = new RawResourceFieldAccessor("meta", ResourceFieldType.META_INFORMATION, TestMeta.class);
		TestMeta value = (TestMeta) accessor.getValue(resource);
		Assertions.assertEquals("someMeta", value.name);
	}

	@Test
	public void links() {
		RawResourceFieldAccessor accessor = new RawResourceFieldAccessor("links", ResourceFieldType.LINKS_INFORMATION, TestLinks.class);
		TestLinks value = (TestLinks) accessor.getValue(resource);
		Assertions.assertEquals("someLink", value.name);
	}

	public static class TestMeta implements MetaInformation {
		public String name;
	}

	public static class TestLinks implements LinksInformation {
		public String name;
	}

	@Test
	public void writeNotSupported() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
		RawResourceFieldAccessor accessor = new RawResourceFieldAccessor("name", ResourceFieldType.ATTRIBUTE, String.class);
		Object value = "Doe";
		accessor.setValue(resource, value);
		});
	}
}
