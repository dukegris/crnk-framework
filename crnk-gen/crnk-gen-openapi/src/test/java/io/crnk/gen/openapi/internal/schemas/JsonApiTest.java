package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class JsonApiTest {

  @Test
  void schema() {
    Schema schema = new JsonApi().schema();
    Assertions.assertTrue(schema instanceof ObjectSchema);
    Assertions.assertTrue(schema.getProperties().containsKey("version"));
    Assertions.assertEquals(1, schema.getProperties().size());
  }
}
