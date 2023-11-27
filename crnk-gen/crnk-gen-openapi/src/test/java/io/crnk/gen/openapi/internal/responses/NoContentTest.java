package io.crnk.gen.openapi.internal.responses;

import io.swagger.v3.oas.models.responses.ApiResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NoContentTest {

  @Test
  void response() {
    ApiResponse apiResponse = new NoContent().response();
    Assertions.assertEquals("No Content", apiResponse.getDescription());
    Assertions.assertNull(apiResponse.getContent());
  }
}