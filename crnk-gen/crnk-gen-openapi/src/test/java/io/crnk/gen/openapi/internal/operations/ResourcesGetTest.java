package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OperationType;
import io.swagger.v3.oas.models.Operation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourcesGetTest extends OperationsBaseTest {
  @Test
  void operationType() {
    ResourcesGet resourcePost = new ResourcesGet(metaResource);
    Assertions.assertEquals(OperationType.GET, resourcePost.operationType());
  }

  @Test
  void isEnabledTrueWhenReadable() {
    ResourcesGet resourcePost = new ResourcesGet(metaResource);
    Assertions.assertTrue(metaResource.isReadable());
    Assertions.assertTrue(resourcePost.isEnabled());
  }

  @Test
  void isEnabled() {
    ResourcesGet resourcePost = new ResourcesGet(metaResource);
    metaResource.setReadable(false);
    Assertions.assertFalse(resourcePost.isEnabled());
  }

  @Test
  void getDescription() {
    ResourcesGet resourcePost = new ResourcesGet(metaResource);
    Assertions.assertEquals("Retrieve a List of ResourceType resources", resourcePost.getDescription());
  }

  @Test
  void operation() {
    Operation operation = new ResourcesGet(metaResource).operation();
    Assertions.assertTrue(operation.getResponses().containsKey("200"));
  }

  @Test
  void path() {
    ResourcesGet resourcePost = new ResourcesGet(metaResource);
    Assertions.assertEquals("/ResourcePath", resourcePost.path());
  }
}
