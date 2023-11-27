package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class MetaTest {

  @Test
  void schema() {
    Schema schema = new Meta().schema();
    Assertions.assertTrue(schema instanceof ObjectSchema);
    Assertions.assertTrue((boolean) schema.getAdditionalProperties());
    Assertions.assertNull(schema.getProperties());
  }
}
