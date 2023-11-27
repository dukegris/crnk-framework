package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;


class LinkTest {

  @Test
  void schema() {
    Schema schema = new Link().schema();
    Assertions.assertTrue(schema instanceof ComposedSchema);

    List<Schema> oneOf = ((ComposedSchema) schema).getOneOf();
    Assertions.assertEquals(2, oneOf.size());

    StringSchema stringSchema = (StringSchema) oneOf.get(0);
    Assertions.assertEquals("uri", stringSchema.getFormat());

    ObjectSchema objectSchema = (ObjectSchema) oneOf.get(1);
    Assertions.assertEquals(Collections.singletonList("href"), objectSchema.getRequired());
    Assertions.assertTrue(objectSchema.getProperties().containsKey("href"));
    Assertions.assertTrue(objectSchema.getProperties().containsKey("meta"));
    Assertions.assertEquals(2, objectSchema.getProperties().size());
  }
}
