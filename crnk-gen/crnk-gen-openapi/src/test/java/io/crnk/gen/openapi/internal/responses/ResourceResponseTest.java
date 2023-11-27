package io.crnk.gen.openapi.internal.responses;

import io.crnk.gen.openapi.internal.MetaResourceBaseTest;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourceResponseTest extends MetaResourceBaseTest {

  @Test
  void response() {
    ApiResponse apiResponse = new ResourceResponse(metaResource).response();
    Assertions.assertNotNull(apiResponse);
    Assertions.assertEquals("OK", apiResponse.getDescription());
    Content content = apiResponse.getContent();
    Assertions.assertEquals(1, content.size());
    Schema schema = content.get("application/vnd.api+json").getSchema();
    Assertions.assertEquals(
        "#/components/schemas/ResourceTypeResourceResponseSchema",
        schema.get$ref()
    );
  }
}