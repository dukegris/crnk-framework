package io.crnk.gen.openapi.internal.parameters;

import io.crnk.gen.openapi.internal.MetaResourceBaseTest;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IncludeTest extends MetaResourceBaseTest {

  @Test
  void parameter() {
    Parameter parameter = new Include(metaResource).parameter();
    Assertions.assertEquals("include", parameter.getName());
    Assertions.assertEquals("query", parameter.getIn());
    Assertions.assertEquals("ResourceType relationships to include (csv)", parameter.getDescription());
    Assertions.assertNull(parameter.getRequired());
    Schema schema = parameter.getSchema();
    Assertions.assertTrue(schema instanceof StringSchema);
  }

  // TODO: Test with relationships to other MetaResources
}
