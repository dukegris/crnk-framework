package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.MetaResourceBaseTest;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PostResourceReferenceTest extends MetaResourceBaseTest {

  @Test
  void schema() {
    Schema schema = new PostResourceReference(metaResource).schema();
    Assertions.assertTrue(schema instanceof ObjectSchema);
    Assertions.assertTrue(schema.getProperties().containsKey("type"));
    Assertions.assertTrue(schema.getProperties().containsKey("id"));
    Assertions.assertEquals(2, schema.getProperties().size());
    Assertions.assertEquals(schema.getRequired().get(0), "type");
    Assertions.assertEquals(1, schema.getRequired().size());
  }
}
