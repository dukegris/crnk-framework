package io.crnk.core.engine.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.core.utils.Nullable;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;

public class DocumentTest {

	@Test
	public void testDocumentEqualsContract() {
		EqualsVerifier.forClass(Document.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

	@Test
	public void getCollectionData() {
		Document doc = new Document();
		Assertions.assertFalse(doc.getCollectionData().isPresent());

		doc.setData(Nullable.nullValue());
		Assertions.assertTrue(doc.getCollectionData().get().isEmpty());

		Resource resource1 = Mockito.mock(Resource.class);
		doc.setData(Nullable.of(resource1));
		Assertions.assertEquals(1, doc.getCollectionData().get().size());

		Resource resource2 = Mockito.mock(Resource.class);
		doc.setData(Nullable.of(Arrays.asList(resource1, resource2)));
		Assertions.assertEquals(2, doc.getCollectionData().get().size());

	}

	@Test
	public void checkJsonApiServerInfoNotSerializedIfNull() throws JsonProcessingException {
		Document document = new Document();
		document.setJsonapi(null);
		Assertions.assertNull(document.getJsonapi());
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectWriter writer = objectMapper.writerFor(Document.class);
		String json = writer.writeValueAsString(document);
		Assertions.assertEquals("{}", json);
	}

	@Test
	public void checkJsonApiServerInfoSerialized() throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectWriter writer = objectMapper.writerFor(Document.class);

		ObjectNode info = (ObjectNode) objectMapper.readTree("{\"a\" : \"b\"}");
		Document document = new Document();
		document.setJsonapi(info);
		Assertions.assertSame(info, document.getJsonapi());

		String json = writer.writeValueAsString(document);
		Assertions.assertEquals("{\"jsonapi\":{\"a\":\"b\"}}", json);
	}
}
