package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OperationType;
import io.swagger.v3.oas.models.Operation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NestedDeleteTest extends NestedOperationsBaseTest {
  @Test
  void operationType() {
    NestedDelete NestedDelete = new NestedDelete(metaResource, metaResourceField, relatedMetaResource);
    Assertions.assertEquals(OperationType.DELETE, NestedDelete.operationType());
  }

  @Test
  void isEnabledTrueWhenReadableAndFieldUpdatable() {
    NestedDelete NestedDelete = new NestedDelete(metaResource, metaResourceField, relatedMetaResource);
    Assertions.assertTrue(metaResource.isReadable());
    metaResourceField.setUpdatable(true);
    Assertions.assertTrue(NestedDelete.isEnabled());
  }

  @Test
  void isEnabledFalseWhenReadableAndFieldNotUpdatable() {
    NestedDelete NestedDelete = new NestedDelete(metaResource, metaResourceField, relatedMetaResource);
    Assertions.assertTrue(metaResource.isReadable());
    metaResource.setUpdatable(false);
    Assertions.assertFalse(NestedDelete.isEnabled());
  }

  @Test
  void isEnabledFalseWhenNotReadableAndFieldUpdatable() {
    NestedDelete NestedDelete = new NestedDelete(metaResource, metaResourceField, relatedMetaResource);
    metaResource.setReadable(false);
    metaResourceField.setUpdatable(true);
    Assertions.assertFalse(NestedDelete.isEnabled());
  }

  @Test
  void isEnabledFalseWhenNotReadableAndFieldNotUpdatable() {
    NestedDelete NestedDelete = new NestedDelete(metaResource, metaResourceField, relatedMetaResource);
    metaResource.setReadable(false);
    metaResourceField.setUpdatable(false);
    Assertions.assertFalse(NestedDelete.isEnabled());
  }

  @Test
  void getDescription() {
    NestedDelete NestedDelete = new NestedDelete(metaResource, metaResourceField, relatedMetaResource);
    Assertions.assertEquals("Delete ResourceType relationship to a RelatedResourceType resource", NestedDelete.getDescription());
  }

  @Test
  void operation() {
    Operation operation = new NestedDelete(metaResource, metaResourceField, relatedMetaResource).operation();
    Assertions.assertTrue(operation.getResponses().containsKey("200"));
  }

  @Test
  void path() {
    NestedDelete NestedDelete = new NestedDelete(metaResource, metaResourceField, relatedMetaResource);
    Assertions.assertEquals("/ResourcePath/{id}/someRelatedResource", NestedDelete.path());
  }
}
