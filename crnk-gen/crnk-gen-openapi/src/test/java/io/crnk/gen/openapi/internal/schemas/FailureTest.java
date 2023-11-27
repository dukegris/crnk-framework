package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class FailureTest {

  @Test
  void schema() {
    Schema schema = new Failure().schema();
    Assertions.assertTrue(schema instanceof ObjectSchema);
    Assertions.assertTrue(schema.getProperties().containsKey("jsonapi"));
    Assertions.assertTrue(schema.getProperties().containsKey("errors"));
    Assertions.assertTrue(schema.getProperties().containsKey("links"));
    Assertions.assertTrue(schema.getProperties().containsKey("meta"));
    Assertions.assertEquals(4, schema.getProperties().size());
  }
}
