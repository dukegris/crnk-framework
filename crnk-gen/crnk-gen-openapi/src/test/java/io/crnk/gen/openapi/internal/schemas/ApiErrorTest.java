package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class ApiErrorTest {

  @Test
  void schema() {
    Schema schema = new ApiError().schema();
    Assertions.assertTrue(schema instanceof ObjectSchema);
    Assertions.assertTrue(schema.getProperties().containsKey("id"));
    Assertions.assertTrue(schema.getProperties().containsKey("links"));
    Assertions.assertTrue(schema.getProperties().containsKey("status"));
    Assertions.assertTrue(schema.getProperties().containsKey("code"));
    Assertions.assertTrue(schema.getProperties().containsKey("title"));
    Assertions.assertTrue(schema.getProperties().containsKey("title"));
    Assertions.assertTrue(schema.getProperties().containsKey("detail"));
    Assertions.assertTrue(schema.getProperties().containsKey("source"));
    Assertions.assertTrue(schema.getProperties().containsKey("meta"));
    Assertions.assertEquals(8, schema.getProperties().size());
  }
}
