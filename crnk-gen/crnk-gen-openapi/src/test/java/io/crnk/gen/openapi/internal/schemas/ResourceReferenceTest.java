package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.MetaResourceBaseTest;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourceReferenceTest extends MetaResourceBaseTest {

  @Test
  void schema() {
    Schema schema = new ResourceReference(metaResource).schema();
    Assertions.assertTrue(schema instanceof ObjectSchema);
    Assertions.assertTrue(schema.getProperties().containsKey("id"));
    Assertions.assertTrue(schema.getProperties().containsKey("type"));
    Assertions.assertEquals(2, schema.getProperties().size());
    Assertions.assertTrue(schema.getRequired().contains("id"));
    Assertions.assertTrue(schema.getRequired().contains("type"));
    Assertions.assertEquals(2, schema.getRequired().size());
  }
}
