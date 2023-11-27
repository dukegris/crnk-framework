package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class InfoTest {

  @Test
  void schema() {
    Schema schema = new Info().schema();
    Assertions.assertTrue(schema instanceof ObjectSchema);
    Assertions.assertTrue(schema.getProperties().containsKey("jsonapi"));
    Assertions.assertTrue(schema.getProperties().containsKey("links"));
    Assertions.assertTrue(schema.getProperties().containsKey("meta"));
    Assertions.assertEquals(3, schema.getProperties().size());
  }
}
