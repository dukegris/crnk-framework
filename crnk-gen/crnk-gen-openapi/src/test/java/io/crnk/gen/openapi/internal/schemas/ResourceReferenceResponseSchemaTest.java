package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.MetaResourceBaseTest;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourceReferenceResponseSchemaTest extends MetaResourceBaseTest {

  @Test
  void schema() {
    Schema schema = new ResourceReferenceResponseSchema(metaResource).schema();
    Assertions.assertTrue(schema instanceof ObjectSchema);
    Assertions.assertTrue(schema.getProperties().containsKey("data"));
    Assertions.assertEquals(1, schema.getProperties().size());

    Schema data = (Schema) schema.getProperties().get("data");
    Assertions.assertEquals(
        "#/components/schemas/ResourceTypeResourceReference",
        data.get$ref()
    );
  }
}
