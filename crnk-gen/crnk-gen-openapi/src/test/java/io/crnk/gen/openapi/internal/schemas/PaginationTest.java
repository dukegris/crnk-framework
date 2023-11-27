package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;


class PaginationTest {

  @Test
  void schema() {
    Schema schema = new Pagination().schema();
    Assertions.assertTrue(schema instanceof ObjectSchema);

    Stream.of("first", "last", "prev", "next").forEach(
        key -> {
          Schema subSchema = (Schema) schema.getProperties().get(key);
          Assertions.assertTrue(subSchema instanceof StringSchema);
          Assertions.assertTrue(subSchema.getNullable());
          Assertions.assertEquals("uri", subSchema.getFormat());
        }
    );
    Assertions.assertEquals(4, schema.getProperties().size());
  }
}
