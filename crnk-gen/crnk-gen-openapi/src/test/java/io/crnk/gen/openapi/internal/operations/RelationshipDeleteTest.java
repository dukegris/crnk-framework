package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OperationType;
import io.swagger.v3.oas.models.Operation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RelationshipDeleteTest extends NestedOperationsBaseTest {
  @Test
  void operationType() {
    RelationshipDelete RelationshipDelete = new RelationshipDelete(metaResource, metaResourceField, relatedMetaResource);
    Assertions.assertEquals(OperationType.DELETE, RelationshipDelete.operationType());
  }

  @Test
  void isEnabledTrueWhenReadableAndFieldUpdatable() {
    RelationshipDelete RelationshipDelete = new RelationshipDelete(metaResource, metaResourceField, relatedMetaResource);
    Assertions.assertTrue(metaResource.isReadable());
    metaResourceField.setUpdatable(true);
    Assertions.assertTrue(RelationshipDelete.isEnabled());
  }

  @Test
  void isEnabledFalseWhenReadableAndFieldNotUpdatable() {
    RelationshipDelete RelationshipDelete = new RelationshipDelete(metaResource, metaResourceField, relatedMetaResource);
    Assertions.assertTrue(metaResource.isReadable());
    metaResource.setUpdatable(false);
    Assertions.assertFalse(RelationshipDelete.isEnabled());
  }

  @Test
  void isEnabledFalseWhenNotReadableAndFieldUpdatable() {
    RelationshipDelete RelationshipDelete = new RelationshipDelete(metaResource, metaResourceField, relatedMetaResource);
    metaResource.setReadable(false);
    metaResourceField.setUpdatable(true);
    Assertions.assertFalse(RelationshipDelete.isEnabled());
  }

  @Test
  void isEnabledFalseWhenNotReadableAndFieldNotUpdatable() {
    RelationshipDelete RelationshipDelete = new RelationshipDelete(metaResource, metaResourceField, relatedMetaResource);
    metaResource.setReadable(false);
    metaResourceField.setUpdatable(false);
    Assertions.assertFalse(RelationshipDelete.isEnabled());
  }

  @Test
  void getDescription() {
    RelationshipDelete RelationshipDelete = new RelationshipDelete(metaResource, metaResourceField, relatedMetaResource);
    Assertions.assertEquals("Delete ResourceType relationship to a RelatedResourceType resource", RelationshipDelete.getDescription());
  }

  @Test
  void operation() {
    Operation operation = new RelationshipDelete(metaResource, metaResourceField, relatedMetaResource).operation();
    Assertions.assertTrue(operation.getResponses().containsKey("200"));
  }

  @Test
  void path() {
    RelationshipDelete RelationshipDelete = new RelationshipDelete(metaResource, metaResourceField, relatedMetaResource);
    Assertions.assertEquals("/ResourcePath/{id}/relationships/someRelatedResource", RelationshipDelete.path());
  }
}
