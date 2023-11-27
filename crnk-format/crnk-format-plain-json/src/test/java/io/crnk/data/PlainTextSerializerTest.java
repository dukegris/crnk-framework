package io.crnk.data;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.document.ErrorDataBuilder;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.internal.jackson.ErrorDataDeserializer;
import io.crnk.core.engine.internal.jackson.ErrorDataSerializer;
import io.crnk.core.utils.Nullable;
import io.crnk.format.plainjson.internal.PlainJsonDocument;
import io.crnk.format.plainjson.internal.PlainJsonDocumentDeserializer;
import io.crnk.format.plainjson.internal.PlainJsonDocumentSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PlainTextSerializerTest {


	private ObjectMapper objectMapper;

	@BeforeEach
	public void setup() {
		objectMapper = new ObjectMapper();
		SimpleModule simpleModule = new SimpleModule();
		simpleModule.addSerializer(new PlainJsonDocumentSerializer());
		simpleModule.addDeserializer(PlainJsonDocument.class, new PlainJsonDocumentDeserializer(objectMapper));
		simpleModule.addSerializer(new ErrorDataSerializer());
		simpleModule.addDeserializer(ErrorData.class, new ErrorDataDeserializer());
		objectMapper.registerModule(simpleModule);
	}

	@Test
	public void emptyDocument() throws IOException {
		PlainJsonDocument document = new PlainJsonDocument();
		String json = objectMapper.writeValueAsString(document);
		PlainJsonDocument copy = objectMapper.readValue(json, PlainJsonDocument.class);

		Assertions.assertNull(copy.getMeta());
		Assertions.assertNull(copy.getLinks());
		Assertions.assertNull(copy.getErrors());
		Assertions.assertNull(copy.getIncluded());
		Assertions.assertFalse(copy.getData().isPresent());
	}

	@Test
	public void meta() throws IOException {
		ObjectNode meta = (ObjectNode) objectMapper.readTree("{\"a\": \"b\"}");

		PlainJsonDocument document = new PlainJsonDocument();
		document.setMeta(meta);

		String json = objectMapper.writeValueAsString(document);
		PlainJsonDocument copy = objectMapper.readValue(json, PlainJsonDocument.class);
		Assertions.assertEquals(meta, copy.getMeta());
	}

	@Test
	public void links() throws IOException {
		ObjectNode links = (ObjectNode) objectMapper.readTree("{\"a\": \"b\"}");

		PlainJsonDocument document = new PlainJsonDocument();
		document.setLinks(links);

		String json = objectMapper.writeValueAsString(document);
		PlainJsonDocument copy = objectMapper.readValue(json, PlainJsonDocument.class);
		Assertions.assertEquals(links, copy.getLinks());
	}

	@Test
	public void errors() throws IOException {
		ErrorDataBuilder builder = new ErrorDataBuilder();
		builder.setStatus("test");
		ErrorData errorData = builder.build();

		PlainJsonDocument document = new PlainJsonDocument();
		document.setErrors(Arrays.asList(errorData));

		String json = objectMapper.writeValueAsString(document);
		PlainJsonDocument copy = objectMapper.readValue(json, PlainJsonDocument.class);
		Assertions.assertEquals(document.getErrors(), copy.getErrors());
	}


	@Test
	public void resource() throws IOException {
		ObjectNode attrValue = (ObjectNode) objectMapper.readTree("{\"a\": \"b\"}");
		ObjectNode someMeta = (ObjectNode) objectMapper.readTree("{\"someMeta\": \"1\"}");
		ObjectNode someLinks = (ObjectNode) objectMapper.readTree("{\"someLink\": \"2\"}");

		Relationship relationship = new Relationship();
		relationship.setData(Nullable.of(new ResourceIdentifier("a", "b")));
		relationship.setLinks(someLinks);
		relationship.setMeta(someMeta);

		Resource resource = new Resource();
		resource.setId("someId");
		resource.setType("someType");
		resource.setMeta(someMeta);
		resource.setLinks(someLinks);
		resource.getAttributes().put("someAttr", attrValue);
		resource.getRelationships().put("someRelation", relationship);

		ErrorDataBuilder builder = new ErrorDataBuilder();
		builder.setStatus("test");

		PlainJsonDocument document = new PlainJsonDocument();
		document.setData(Nullable.of(resource));

		String json = objectMapper.writeValueAsString(document);
		PlainJsonDocument copy = objectMapper.readValue(json, PlainJsonDocument.class);

		Resource resourceCopy = copy.getSingleData().get();
		Assertions.assertEquals(resource.getId(), resourceCopy.getId());
		Assertions.assertEquals(resource.getType(), resourceCopy.getType());
		Assertions.assertEquals(resource.getMeta(), resourceCopy.getMeta());
		Assertions.assertEquals(resource.getLinks(), resourceCopy.getLinks());
		Assertions.assertEquals(resource.getAttributes(), resourceCopy.getAttributes());
		Assertions.assertEquals(resource.getRelationships(), resourceCopy.getRelationships());
	}

	@Test
	public void included() throws IOException {
		ObjectNode attrValue = (ObjectNode) objectMapper.readTree("{\"a\": \"b\"}");
		ObjectNode someMeta = (ObjectNode) objectMapper.readTree("{\"someMeta\": \"1\"}");
		ObjectNode someLinks = (ObjectNode) objectMapper.readTree("{\"someLink\": \"2\"}");

		Relationship relationship = new Relationship();
		relationship.setData(Nullable.of(new ResourceIdentifier("a", "b")));
		relationship.setLinks(someLinks);
		relationship.setMeta(someMeta);

		Resource resource = new Resource();
		resource.setId("someId");
		resource.setType("someType");
		resource.setMeta(someMeta);
		resource.setLinks(someLinks);
		resource.getAttributes().put("someAttr", attrValue);
		resource.getRelationships().put("someRelation", relationship);

		// resource matching the relationship
		Resource related = new Resource();
		related.setId("a");
		related.setType("b");
		related.setMeta(someMeta);
		related.setLinks(someLinks);
		related.getAttributes().put("someAttr", attrValue);
		related.getRelationships().put("someRelation", relationship);

		ErrorDataBuilder builder = new ErrorDataBuilder();
		builder.setStatus("test");

		PlainJsonDocument document = new PlainJsonDocument();
		document.setData(Nullable.of(resource));
		document.setIncluded(Arrays.asList(related));

		String json = objectMapper.writeValueAsString(document);
		PlainJsonDocument copy = objectMapper.readValue(json, PlainJsonDocument.class);

		Resource resourceCopy = copy.getSingleData().get();
		Assertions.assertEquals(resource.getId(), resourceCopy.getId());
		Assertions.assertEquals(resource.getType(), resourceCopy.getType());
		Assertions.assertEquals(resource.getMeta(), resourceCopy.getMeta());
		Assertions.assertEquals(resource.getLinks(), resourceCopy.getLinks());
		Assertions.assertEquals(resource.getAttributes(), resourceCopy.getAttributes());
		Assertions.assertEquals(resource.getRelationships(), resourceCopy.getRelationships());

		List<Resource> includedCopy = copy.getIncluded();
		Assertions.assertEquals(1, includedCopy.size());
		Resource relatedCopy = includedCopy.get(0);
		Assertions.assertEquals(related.getId(), relatedCopy.getId());
		Assertions.assertEquals(related.getType(), relatedCopy.getType());
		Assertions.assertEquals(related.getMeta(), relatedCopy.getMeta());
		Assertions.assertEquals(related.getLinks(), relatedCopy.getLinks());
		Assertions.assertEquals(related.getAttributes(), relatedCopy.getAttributes());
		Assertions.assertEquals(related.getRelationships(), relatedCopy.getRelationships());
	}
}
