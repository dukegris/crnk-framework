package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.MetaResourceBaseTest;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourcePostAttributesTest extends MetaResourceBaseTest {

  @Test
  void isInsertable() {
    MetaResource metaResource = getTestMetaResource();
    MetaResourceField additionalMetaResourceField = (MetaResourceField) metaResource.getChildren().get(1);
    additionalMetaResourceField.setInsertable(true);

    Schema schema = new ResourcePostAttributes(metaResource).schema();
    Assertions.assertTrue(schema instanceof ObjectSchema);

    Assertions.assertTrue(schema.getProperties().containsKey("attributes"));
    Assertions.assertEquals(1, schema.getProperties().size());

    Schema attributes = (Schema) schema.getProperties().get("attributes");
    Assertions.assertTrue(attributes instanceof ObjectSchema);
    Assertions.assertTrue(attributes.getProperties().containsKey("name"));
    Assertions.assertEquals(1, attributes.getProperties().size());

    Schema name = (Schema) attributes.getProperties().get("name");
    Assertions.assertEquals(
        "#/components/schemas/ResourceTypeNameResourceAttribute",
        name.get$ref());
  }

  @Test
  void notInsertable() {
    MetaResource metaResource = getTestMetaResource();
    MetaResourceField additionalMetaResourceField = (MetaResourceField) metaResource.getChildren().get(1);
    additionalMetaResourceField.setInsertable(false);

    Schema schema = new ResourcePostAttributes(metaResource).schema();
    Assertions.assertTrue(schema instanceof ObjectSchema);

    Assertions.assertTrue(schema.getProperties().containsKey("attributes"));
    Assertions.assertEquals(1, schema.getProperties().size());

    Schema attributes = (Schema) schema.getProperties().get("attributes");
    Assertions.assertTrue(attributes instanceof ObjectSchema);
    Assertions.assertEquals(0, attributes.getProperties().size());
  }
}
