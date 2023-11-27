package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OperationType;
import io.swagger.v3.oas.models.Operation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourceDeleteTest extends OperationsBaseTest {
  @Test
  void operationType() {
    ResourceDelete ResourceDelete = new ResourceDelete(metaResource);
    Assertions.assertEquals(OperationType.DELETE, ResourceDelete.operationType());
  }

  @Test
  void isEnabledTrueWhenDeletable() {
    ResourceDelete ResourceDelete = new ResourceDelete(metaResource);
    Assertions.assertTrue(metaResource.isDeletable());
    Assertions.assertTrue(ResourceDelete.isEnabled());
  }

  @Test
  void isEnabled() {
    ResourceDelete ResourceDelete = new ResourceDelete(metaResource);
    metaResource.setDeletable(false);
    Assertions.assertFalse(ResourceDelete.isEnabled());
  }

  @Test
  void getDescription() {
    ResourceDelete ResourceDelete = new ResourceDelete(metaResource);
    Assertions.assertEquals("Delete a ResourceName", ResourceDelete.getDescription());
  }

  @Test
  void operation() {
    Operation operation = new ResourceDelete(metaResource).operation();
    Assertions.assertTrue(operation.getResponses().containsKey("200"));
  }

  @Test
  void path() {
    ResourceDelete ResourceDelete = new ResourceDelete(metaResource);
    Assertions.assertEquals("/ResourcePath/{id}", ResourceDelete.path());
  }
}
