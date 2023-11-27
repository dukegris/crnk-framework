package io.crnk.gen.openapi.internal.parameters;

import io.crnk.gen.openapi.internal.MetaResourceBaseTest;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FieldsTest extends MetaResourceBaseTest {

	@Test
	void parameter() {
		Parameter parameter = new Fields(metaResource).parameter();
		Assertions.assertEquals("fields", parameter.getName());
		Assertions.assertEquals("query", parameter.getIn());
		Assertions.assertEquals("ResourceType fields to include (csv)", parameter.getDescription());
		Assertions.assertNull(parameter.getRequired());
		Schema schema = parameter.getSchema();
		Assertions.assertTrue(schema instanceof StringSchema);
		Assertions.assertEquals("id,name,resourceRelation", schema.getDefault());  // TODO: Should Primary Key Field(s) be included?
	}
}
