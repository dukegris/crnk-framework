package io.crnk.gen.openapi.internal;

import io.crnk.gen.openapi.internal.schemas.Failure;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class OASErrorsTest {
  @Test
  public void test() {
    Map<String, ApiResponse> apiResponseCodes = OASErrors.generateStandardApiErrorResponses();

    for (Map.Entry<String, ApiResponse> entry : apiResponseCodes.entrySet()) {
      Assertions.assertTrue(entry.getKey().startsWith("4") || entry.getKey().startsWith("5"));

      ApiResponse apiResponse = entry.getValue();
      Assertions.assertNotNull(apiResponse.getDescription());

      Schema schema = apiResponse.getContent().get("application/vnd.api+json").getSchema();
      Assertions.assertEquals(new Failure().$ref(), schema);
    }
  }
}
