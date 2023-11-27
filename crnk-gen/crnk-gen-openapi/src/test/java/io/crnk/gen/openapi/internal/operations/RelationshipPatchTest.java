package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OperationType;
import io.swagger.v3.oas.models.Operation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RelationshipPatchTest extends NestedOperationsBaseTest {
  @Test
  void operationType() {
    RelationshipPatch RelationshipPatch = new RelationshipPatch(metaResource, metaResourceField, relatedMetaResource);
    Assertions.assertEquals(OperationType.PATCH, RelationshipPatch.operationType());
  }

  @Test
  void isEnabledTrueWhenReadableAndFieldUpdatable() {
    RelationshipPatch RelationshipPatch = new RelationshipPatch(metaResource, metaResourceField, relatedMetaResource);
    Assertions.assertTrue(metaResource.isReadable());
    metaResourceField.setUpdatable(true);
    Assertions.assertTrue(RelationshipPatch.isEnabled());
  }

  @Test
  void isEnabledFalseWhenReadableAndFieldNotUpdatable() {
    RelationshipPatch RelationshipPatch = new RelationshipPatch(metaResource, metaResourceField, relatedMetaResource);
    Assertions.assertTrue(metaResource.isReadable());
    metaResource.setUpdatable(false);
    Assertions.assertFalse(RelationshipPatch.isEnabled());
  }

  @Test
  void isEnabledFalseWhenNotReadableAndFieldUpdatable() {
    RelationshipPatch RelationshipPatch = new RelationshipPatch(metaResource, metaResourceField, relatedMetaResource);
    metaResource.setReadable(false);
    metaResourceField.setUpdatable(true);
    Assertions.assertFalse(RelationshipPatch.isEnabled());
  }

  @Test
  void isEnabledFalseWhenNotReadableAndFieldNotUpdatable() {
    RelationshipPatch RelationshipPatch = new RelationshipPatch(metaResource, metaResourceField, relatedMetaResource);
    metaResource.setReadable(false);
    metaResourceField.setUpdatable(false);
    Assertions.assertFalse(RelationshipPatch.isEnabled());
  }

  @Test
  void getDescription() {
    RelationshipPatch RelationshipPatch = new RelationshipPatch(metaResource, metaResourceField, relatedMetaResource);
    Assertions.assertEquals("Update ResourceType relationship to a RelatedResourceType resource", RelationshipPatch.getDescription());
  }

  @Test
  void operation() {
    Operation operation = new RelationshipPatch(metaResource, metaResourceField, relatedMetaResource).operation();
    Assertions.assertTrue(operation.getResponses().containsKey("200"));
  }

  @Test
  void path() {
    RelationshipPatch RelationshipPatch = new RelationshipPatch(metaResource, metaResourceField, relatedMetaResource);
    Assertions.assertEquals("/ResourcePath/{id}/relationships/someRelatedResource", RelationshipPatch.path());
  }
}
