package io.crnk.gen.openapi.internal.parameters;

import io.crnk.gen.openapi.internal.MetaResourceBaseTest;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PrimaryKeyTest extends MetaResourceBaseTest {

  @Test
  void parameter() {
    Parameter parameter = new PrimaryKey(metaResource).parameter();
    Assertions.assertEquals("id", parameter.getName());
    Assertions.assertEquals("path", parameter.getIn());
    Assertions.assertTrue(parameter.getRequired());
    Schema schema = parameter.getSchema();
    Assertions.assertEquals(
        "#/components/schemas/ResourceTypeIdResourceAttribute",
        schema.get$ref()
    );
  }
}
