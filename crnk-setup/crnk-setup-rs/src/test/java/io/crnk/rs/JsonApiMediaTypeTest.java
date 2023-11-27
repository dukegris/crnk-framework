package io.crnk.rs;

import io.crnk.rs.type.JsonApiMediaType;
import io.crnk.test.mock.ClassTestUtils;
import org.junit.jupiter.api.Test;

public class JsonApiMediaTypeTest {

	@Test
	public void hasPrivateConstructor() {
		ClassTestUtils.assertPrivateConstructor(JsonApiMediaType.class);
	}

}
