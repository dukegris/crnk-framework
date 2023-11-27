package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class LinksTest {
  @Test
  void schema() {
    Schema schema = new Links().schema();
    Assertions.assertTrue(schema instanceof ComposedSchema);

    List<Schema> allOf = ((ComposedSchema) schema).getAllOf();
    Assertions.assertEquals(2, allOf.size());
    Assertions.assertTrue(allOf.get(0) instanceof ObjectSchema);
    Assertions.assertEquals(new Pagination().$ref().get$ref(), allOf.get(1).get$ref());
  }
}
