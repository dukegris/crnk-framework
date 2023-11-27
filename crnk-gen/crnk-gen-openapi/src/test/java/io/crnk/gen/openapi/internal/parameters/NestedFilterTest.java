package io.crnk.gen.openapi.internal.parameters;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NestedFilterTest {

  @Test
  void parameter() {
    Parameter parameter = new NestedFilter().parameter();
    Assertions.assertEquals("filter", parameter.getName());
    Assertions.assertEquals("query", parameter.getIn());
    Assertions.assertEquals("Customizable query (experimental)", parameter.getDescription());
    Assertions.assertNull(parameter.getRequired());
    Schema schema = parameter.getSchema();
    Assertions.assertTrue(schema instanceof ObjectSchema);
    Assertions.assertEquals(true, schema.getAdditionalProperties());
    Assertions.assertEquals(3, schema.getProperties().size());
    ObjectSchema andSchema = (ObjectSchema) schema.getProperties().get("AND");
    Assertions.assertEquals(true, andSchema.getAdditionalProperties());
    Assertions.assertTrue(andSchema.getNullable());
    ObjectSchema orSchema = (ObjectSchema) schema.getProperties().get("OR");
    Assertions.assertEquals(true, orSchema.getAdditionalProperties());
    Assertions.assertTrue(orSchema.getNullable());
    ObjectSchema notSchema = (ObjectSchema) schema.getProperties().get("NOT");
    Assertions.assertEquals(true, notSchema.getAdditionalProperties());
    Assertions.assertTrue(notSchema.getNullable());
  }
}
