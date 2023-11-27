package io.crnk.client.response;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.resource.meta.JsonMetaInformation;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.repository.ScheduleRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JsonMetaInformationTest {

	private ObjectMapper mapper;

	private JsonNode node;

	@BeforeEach
	public void setup() throws IOException {
		mapper = new ObjectMapper();
		node = mapper.reader().readTree("{\"value\": \"test\"}");
	}

	@Test
	public void testAsNode() {
		JsonMetaInformation info = new JsonMetaInformation(node, mapper);
		Assertions.assertSame(node, info.asJsonNode());
	}

	@Test
	public void testParse() {
		JsonMetaInformation info = new JsonMetaInformation(node, mapper);

		Task.TaskMeta meta = info.as(Task.TaskMeta.class);
		Assertions.assertEquals("test", meta.value);
	}

	@Test
	public void testInterfaceProxy() {
		JsonMetaInformation info = new JsonMetaInformation(node, mapper);

		MetaInterface meta = info.as(MetaInterface.class);
		Assertions.assertEquals("test", meta.getValue());
	}

	interface MetaInterface extends MetaInformation {

		String getValue();
	}


	@Test
	public void testParseException() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			JsonMetaInformation info = new JsonMetaInformation(node, mapper);
			info.as(ScheduleRepository.ScheduleListMeta.class);
		});
	}


}
