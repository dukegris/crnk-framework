package io.crnk.gen.openapi.internal.responses;

import io.swagger.v3.oas.models.responses.ApiResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class StaticResponsesTest {

  @Test
  void generateStandardApiResponses() {
    Map<String, ApiResponse> apiResponseMap = StaticResponses.generateStandardApiResponses();
    Assertions.assertEquals(16, apiResponseMap.size());
    Assertions.assertNotNull(apiResponseMap.get("NoContent"));
    Assertions.assertNotNull(apiResponseMap.get("400"));
    Assertions.assertNotNull(apiResponseMap.get("401"));
    Assertions.assertNotNull(apiResponseMap.get("403"));
    Assertions.assertNotNull(apiResponseMap.get("404"));
    Assertions.assertNotNull(apiResponseMap.get("405"));
    Assertions.assertNotNull(apiResponseMap.get("409"));
    Assertions.assertNotNull(apiResponseMap.get("412"));
    Assertions.assertNotNull(apiResponseMap.get("415"));
    Assertions.assertNotNull(apiResponseMap.get("422"));
    Assertions.assertNotNull(apiResponseMap.get("500"));
    Assertions.assertNotNull(apiResponseMap.get("501"));
    Assertions.assertNotNull(apiResponseMap.get("502"));
    Assertions.assertNotNull(apiResponseMap.get("503"));
    Assertions.assertNotNull(apiResponseMap.get("504"));
    Assertions.assertNotNull(apiResponseMap.get("505"));
  }
}
