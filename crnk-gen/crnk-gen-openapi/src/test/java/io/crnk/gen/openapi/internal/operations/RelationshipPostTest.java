package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OperationType;
import io.swagger.v3.oas.models.Operation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RelationshipPostTest extends NestedOperationsBaseTest {
  @Test
  void operationType() {
    RelationshipPost RelationshipPost = new RelationshipPost(metaResource, metaResourceField, relatedMetaResource);
    Assertions.assertEquals(OperationType.POST, RelationshipPost.operationType());
  }

  @Test
  void isEnabledTrueWhenReadableAndFieldInsertable() {
    RelationshipPost RelationshipPost = new RelationshipPost(metaResource, metaResourceField, relatedMetaResource);
    Assertions.assertTrue(metaResource.isReadable());
    metaResourceField.setInsertable(true);
    Assertions.assertTrue(RelationshipPost.isEnabled());
  }

  @Test
  void isEnabledFalseWhenReadableAndFieldNotInsertable() {
    RelationshipPost RelationshipPost = new RelationshipPost(metaResource, metaResourceField, relatedMetaResource);
    Assertions.assertTrue(metaResource.isReadable());
    metaResource.setInsertable(false);
    Assertions.assertFalse(RelationshipPost.isEnabled());
  }

  @Test
  void isEnabledFalseWhenNotReadableAndFieldInsertable() {
    RelationshipPost RelationshipPost = new RelationshipPost(metaResource, metaResourceField, relatedMetaResource);
    metaResource.setReadable(false);
    metaResourceField.setInsertable(true);
    Assertions.assertFalse(RelationshipPost.isEnabled());
  }

  @Test
  void isEnabledFalseWhenNotReadableAndFieldNotInsertable() {
    RelationshipPost RelationshipPost = new RelationshipPost(metaResource, metaResourceField, relatedMetaResource);
    metaResource.setReadable(false);
    metaResourceField.setInsertable(false);
    Assertions.assertFalse(RelationshipPost.isEnabled());
  }

  @Test
  void getDescription() {
    RelationshipPost RelationshipPost = new RelationshipPost(metaResource, metaResourceField, relatedMetaResource);
    Assertions.assertEquals("Create ResourceType relationship to a RelatedResourceType resource", RelationshipPost.getDescription());
  }

  @Test
  void operation() {
    Operation operation = new RelationshipPost(metaResource, metaResourceField, relatedMetaResource).operation();
    Assertions.assertTrue(operation.getResponses().containsKey("200"));
  }

  @Test
  void path() {
    RelationshipPost RelationshipPost = new RelationshipPost(metaResource, metaResourceField, relatedMetaResource);
    Assertions.assertEquals("/ResourcePath/{id}/relationships/someRelatedResource", RelationshipPost.path());
  }
}
