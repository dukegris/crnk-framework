package io.crnk.gen.openapi.internal.parameters;

import io.crnk.gen.openapi.internal.MetaResourceBaseTest;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FieldFilterTest extends MetaResourceBaseTest {

  @Test
  void parameter() {
    Parameter parameter = new FieldFilter(metaResource, metaResourceField).parameter();
    Assertions.assertEquals("filter[id]", parameter.getName());
    Assertions.assertEquals("query", parameter.getIn());
    Assertions.assertEquals("Filter by id (csv)", parameter.getDescription());
    Assertions.assertNull(parameter.getRequired());
    Schema schema = parameter.getSchema();
    Assertions.assertTrue(schema instanceof StringSchema);
  }
}
