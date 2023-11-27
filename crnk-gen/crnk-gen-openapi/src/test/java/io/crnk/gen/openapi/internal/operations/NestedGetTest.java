package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OperationType;
import io.swagger.v3.oas.models.Operation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NestedGetTest extends NestedOperationsBaseTest {
  @Test
  void operationType() {
    NestedGet NestedGet = new NestedGet(metaResource, metaResourceField, relatedMetaResource);
    Assertions.assertEquals(OperationType.GET, NestedGet.operationType());
  }

  @Test
  void isEnabledTrueWhenReadableAndFieldReadable() {
    NestedGet NestedGet = new NestedGet(metaResource, metaResourceField, relatedMetaResource);
    Assertions.assertTrue(metaResource.isReadable());
    metaResourceField.setReadable(true);
    Assertions.assertTrue(NestedGet.isEnabled());
  }

  @Test
  void isEnabledFalseWhenReadableAndFieldNotReadable() {
    NestedGet NestedGet = new NestedGet(metaResource, metaResourceField, relatedMetaResource);
    Assertions.assertTrue(metaResource.isReadable());
    metaResource.setReadable(false);
    Assertions.assertFalse(NestedGet.isEnabled());
  }

  @Test
  void isEnabledFalseWhenNotReadableAndFieldReadable() {
    NestedGet NestedGet = new NestedGet(metaResource, metaResourceField, relatedMetaResource);
    metaResource.setReadable(false);
    metaResourceField.setReadable(true);
    Assertions.assertFalse(NestedGet.isEnabled());
  }

  @Test
  void isEnabledFalseWhenNotReadableAndFieldNotReadable() {
    NestedGet NestedGet = new NestedGet(metaResource, metaResourceField, relatedMetaResource);
    metaResource.setReadable(false);
    metaResourceField.setReadable(false);
    Assertions.assertFalse(NestedGet.isEnabled());
  }

  @Test
  void getDescription() {
    NestedGet NestedGet = new NestedGet(metaResource, metaResourceField, relatedMetaResource);
    Assertions.assertEquals("Retrieve RelatedResourceType related to a ResourceType resource", NestedGet.getDescription());
  }

  @Test
  void operation() {
    Operation operation = new NestedGet(metaResource, metaResourceField, relatedMetaResource).operation();
    Assertions.assertTrue(operation.getResponses().containsKey("200"));
  }

  @Test
  void path() {
    NestedGet NestedGet = new NestedGet(metaResource, metaResourceField, relatedMetaResource);
    Assertions.assertEquals("/ResourcePath/{id}/someRelatedResource", NestedGet.path());
  }
}
