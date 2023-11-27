package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OperationType;
import io.swagger.v3.oas.models.Operation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourceGetTest extends OperationsBaseTest {
  @Test
  void operationType() {
    ResourceGet resourcePost = new ResourceGet(metaResource);
    Assertions.assertEquals(OperationType.GET, resourcePost.operationType());
  }

  @Test
  void isEnabledTrueWhenReadable() {
    ResourceGet resourcePost = new ResourceGet(metaResource);
    Assertions.assertTrue(metaResource.isReadable());
    Assertions.assertTrue(resourcePost.isEnabled());
  }

  @Test
  void isEnabled() {
    ResourceGet resourcePost = new ResourceGet(metaResource);
    metaResource.setReadable(false);
    Assertions.assertFalse(resourcePost.isEnabled());
  }

  @Test
  void getDescription() {
    ResourceGet resourcePost = new ResourceGet(metaResource);
    Assertions.assertEquals("Retrieve a ResourceType resource", resourcePost.getDescription());
  }

  @Test
  void operation() {
    Operation operation = new ResourceGet(metaResource).operation();
    Assertions.assertTrue(operation.getResponses().containsKey("200"));
  }

  @Test
  void path() {
    ResourceGet resourcePost = new ResourceGet(metaResource);
    Assertions.assertEquals("/ResourcePath/{id}", resourcePost.path());
  }
}
