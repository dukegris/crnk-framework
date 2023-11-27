package io.crnk.data.jpa.information;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.information.DefaultInformationBuilder;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.properties.NullPropertiesProvider;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;
import io.crnk.core.resource.annotations.SerializeType;
import io.crnk.data.jpa.internal.JpaResourceInformationProvider;
import io.crnk.data.jpa.meta.JpaMetaProvider;
import io.crnk.data.jpa.model.AnnotationMappedSuperclassEntity;
import io.crnk.data.jpa.model.AnnotationTestEntity;
import io.crnk.data.jpa.model.JpaResourcePathTestEntity;
import io.crnk.data.jpa.model.JpaTransientTestEntity;
import io.crnk.data.jpa.model.JsonapiResourcePathTestEntity;
import io.crnk.data.jpa.model.ManyToManyOppositeEntity;
import io.crnk.data.jpa.model.ManyToManyTestEntity;
import io.crnk.data.jpa.model.OneToOneTestEntity;
import io.crnk.data.jpa.model.ReadOnlyAnnotatedEntity;
import io.crnk.data.jpa.model.RelatedEntity;
import io.crnk.data.jpa.model.RenamedTestEntity;
import io.crnk.data.jpa.model.TestEmbeddable;
import io.crnk.data.jpa.model.TestEntity;
import io.crnk.data.jpa.model.TestMappedSuperclass;
import io.crnk.data.jpa.model.TestSubclassWithSuperclassGenericsInterface;
import io.crnk.data.jpa.model.VersionedEntity;
import io.crnk.data.jpa.util.ResourceFieldComparator;
import io.crnk.legacy.registry.DefaultResourceInformationProviderContext;
import io.crnk.meta.MetaLookupImpl;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaDataObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JpaResourceInformationProviderTest {

    private JpaResourceInformationProvider builder;

    private JpaMetaProvider jpaMetaProvider;

    @BeforeEach
    public void setup() {
        jpaMetaProvider = new JpaMetaProvider(Collections.emptySet());
        MetaLookupImpl lookup = new MetaLookupImpl();
        lookup.addProvider(jpaMetaProvider);
        builder = new JpaResourceInformationProvider(new NullPropertiesProvider());
        builder.init(new DefaultResourceInformationProviderContext(builder, new DefaultInformationBuilder(new TypeParser()),
                new TypeParser(), () -> new ObjectMapper()));
    }

    @Test
    public void checkNotAcceptMappedSuperClass() throws SecurityException, IllegalArgumentException {
        Assertions.assertFalse(builder.accept(TestMappedSuperclass.class));
    }

    @Test
    public void checkAcceptEntity() throws SecurityException, IllegalArgumentException {
        Assertions.assertTrue(builder.accept(TestEntity.class));
    }

    @Test
    public void checkResourceAccessAnnotations() {
        ResourceInformation information = builder.build(ReadOnlyAnnotatedEntity.class);
        Assertions.assertTrue(information.getAccess().isReadable());
        Assertions.assertFalse(information.getAccess().isPostable());
        Assertions.assertFalse(information.getAccess().isDeletable());
        Assertions.assertFalse(information.getAccess().isPatchable());
        for (ResourceField field : information.getFields()) {
            Assertions.assertTrue(field.getAccess().isReadable());
            Assertions.assertFalse(field.getAccess().isPostable());
            Assertions.assertFalse(field.getAccess().isDeletable());
            Assertions.assertFalse(field.getAccess().isPatchable());
        }
    }

    @Test
    public void test() throws SecurityException, IllegalArgumentException {

        ResourceInformation info = builder.build(TestEntity.class);
        ResourceField idField = info.getIdField();
        assertNotNull(idField);
        assertEquals("id", idField.getJsonName());
        assertEquals("id", idField.getUnderlyingName());
        assertEquals(Long.class, idField.getType());
        assertEquals(Long.class, idField.getGenericType());

        List<ResourceField> attrFields = new ArrayList<>(info.getAttributeFields());
        attrFields.sort(ResourceFieldComparator.INSTANCE);
        assertEquals(5, attrFields.size());
        ResourceField embField = attrFields.get(1);
        assertEquals(TestEntity.ATTR_embValue, embField.getJsonName());
        assertEquals(TestEntity.ATTR_embValue, embField.getUnderlyingName());
        assertEquals(TestEmbeddable.class, embField.getType());
        assertEquals(TestEmbeddable.class, embField.getGenericType());
        Assertions.assertTrue(embField.getAccess().isPostable());
        Assertions.assertTrue(embField.getAccess().isPatchable());
        Assertions.assertTrue(embField.getAccess().isSortable());
        Assertions.assertTrue(embField.getAccess().isFilterable());

        ArrayList<ResourceField> relFields = new ArrayList<>(info.getRelationshipFields());
        relFields.sort(ResourceFieldComparator.INSTANCE);
        assertEquals(6, relFields.size());
        boolean found = false;
        for (ResourceField relField : relFields) {
            if (relField.getUnderlyingName().equals(TestEntity.ATTR_oneRelatedValue)) {
                assertEquals(TestEntity.ATTR_oneRelatedValue, relField.getJsonName());
                assertEquals(RelatedEntity.class, relField.getType());
                assertEquals(RelatedEntity.class, relField.getGenericType());
                found = true;
            }
        }
        Assertions.assertTrue(found);
    }

    @Test
    public void testPrimitiveTypesProperlyRecognized() {
        ResourceInformation info = builder.build(TestEntity.class);
        ResourceField field = info.findAttributeFieldByName("longValue");
        Assertions.assertNotNull(field);
        Assertions.assertEquals(long.class, field.getType());
        Assertions.assertEquals(long.class, field.getGenericType());
    }

    @Test
    public void testIdAccess() {
        ResourceInformation info = builder.build(TestEntity.class);
        ResourceField idField = info.getIdField();
        Assertions.assertTrue(idField.getAccess().isPostable());
        Assertions.assertFalse(idField.getAccess().isPatchable());
        Assertions.assertTrue(idField.getAccess().isSortable());
        Assertions.assertTrue(idField.getAccess().isFilterable());
    }

    @Test
    public void testJpaTransient() {
        // available on resource-layer
        ResourceInformation information = builder.build(JpaTransientTestEntity.class);
        Assertions.assertNotNull(information.findFieldByName("id"));
        Assertions.assertNotNull(information.findFieldByName("task"));

        // not available on jpa-layer
        MetaDataObject entityMeta = jpaMetaProvider.discoverMeta(JpaTransientTestEntity.class);
        Assertions.assertTrue(entityMeta.hasAttribute("id"));
        Assertions.assertFalse(entityMeta.hasAttribute("task"));
    }

    @Test
    public void testStringAttributeAccess() {
        ResourceInformation info = builder.build(TestEntity.class);
        ResourceField field = info.findAttributeFieldByName("stringValue");
        Assertions.assertTrue(field.getAccess().isPostable());
        Assertions.assertTrue(field.getAccess().isPatchable());
        Assertions.assertTrue(field.getAccess().isSortable());
        Assertions.assertTrue(field.getAccess().isFilterable());
    }

    @Test
    public void testLongAttributeAccess() {
        ResourceInformation info = builder.build(VersionedEntity.class);
        ResourceField field = info.findAttributeFieldByName("longValue");
        Assertions.assertTrue(field.getAccess().isPostable());
        Assertions.assertTrue(field.getAccess().isPatchable());
    }

    @Test
    public void testVersionAccess() {
        ResourceInformation info = builder.build(VersionedEntity.class);
        ResourceField field = info.findAttributeFieldByName("version");
        // must not be immutable to support optimistic locking
        Assertions.assertTrue(field.getAccess().isPostable());
        Assertions.assertTrue(field.getAccess().isPatchable());
    }

    @Test
    public void testOneToOneRelation() {
        ResourceInformation info = builder.build(OneToOneTestEntity.class);
        ResourceField field = info.findRelationshipFieldByName("oneRelatedValue");
        Assertions.assertEquals(ResourceFieldType.RELATIONSHIP, field.getResourceFieldType());
        Assertions.assertEquals("related", field.getOppositeResourceType());
        Assertions.assertEquals(SerializeType.LAZY, field.getSerializeType());
    }

    @Test
    public void testManyToManyRelation() {
        ResourceInformation info = builder.build(ManyToManyTestEntity.class);
        ResourceField field = info.findRelationshipFieldByName("opposites");
        Assertions.assertEquals(ResourceFieldType.RELATIONSHIP, field.getResourceFieldType());
        Assertions.assertEquals("manyToManyOpposite", field.getOppositeResourceType());
        Assertions.assertEquals(SerializeType.LAZY, field.getSerializeType());
        Assertions.assertEquals(RelationshipRepositoryBehavior.FORWARD_OWNER, field.getRelationshipRepositoryBehavior());
    }

    @Test
    public void testManyToManyOppositeRelation() {
        ResourceInformation info = builder.build(ManyToManyOppositeEntity.class);
        ResourceField field = info.findRelationshipFieldByName("tests");
        Assertions.assertEquals(ResourceFieldType.RELATIONSHIP, field.getResourceFieldType());
        Assertions.assertEquals("manyToManyTest", field.getOppositeResourceType());
        Assertions.assertEquals(SerializeType.LAZY, field.getSerializeType());
        Assertions.assertEquals("opposites", field.getOppositeName());
        Assertions.assertEquals(RelationshipRepositoryBehavior.FORWARD_OPPOSITE, field.getRelationshipRepositoryBehavior());
    }

    @Test
    public void testManyToOneRelation() {
        ResourceInformation info = builder.build(TestEntity.class);
        ResourceField field = info.findRelationshipFieldByName("oneRelatedValue");
        Assertions.assertEquals(ResourceFieldType.RELATIONSHIP, field.getResourceFieldType());
        Assertions.assertEquals("related", field.getOppositeResourceType());
        Assertions.assertEquals(SerializeType.LAZY, field.getSerializeType());
        Assertions.assertNull(field.getOppositeName());
        Assertions.assertEquals(RelationshipRepositoryBehavior.FORWARD_OWNER, field.getRelationshipRepositoryBehavior());
    }

    @Test
    public void testOneToManyRelation() {
        ResourceInformation info = builder.build(TestEntity.class);
        ResourceField field = info.findRelationshipFieldByName("manyRelatedValues");
        Assertions.assertEquals(ResourceFieldType.RELATIONSHIP, field.getResourceFieldType());
        Assertions.assertEquals("related", field.getOppositeResourceType());
        Assertions.assertEquals(SerializeType.LAZY, field.getSerializeType());
        Assertions.assertEquals("testEntity", field.getOppositeName());
        Assertions.assertEquals(RelationshipRepositoryBehavior.FORWARD_OPPOSITE, field.getRelationshipRepositoryBehavior());
    }

	@Test
	public void testManyToOneRelationWithSuperclassGenericsInterface() {
		ResourceInformation info = builder.build(TestSubclassWithSuperclassGenericsInterface.class);
		ResourceField field = info.findRelationshipFieldByName("generic");
		Assertions.assertEquals(ResourceFieldType.RELATIONSHIP, field.getResourceFieldType());
		Assertions.assertEquals("testSubclassWithSuperclassGenericsInterface", field.getOppositeResourceType());
		Assertions.assertEquals(SerializeType.LAZY, field.getSerializeType());
		Assertions.assertNull(field.getOppositeName());
		Assertions.assertEquals(RelationshipRepositoryBehavior.FORWARD_OWNER, field.getRelationshipRepositoryBehavior());
	}

    @Test
    public void testAttributeAnnotations() throws SecurityException, IllegalArgumentException {
        ResourceInformation info = builder.build(AnnotationTestEntity.class);

        ResourceField lobField = info.findAttributeFieldByName("lobValue");
        ResourceField fieldAnnotatedField = info.findAttributeFieldByName("fieldAnnotatedValue");
        ResourceField columnAnnotatedField = info.findAttributeFieldByName("columnAnnotatedValue");

        Assertions.assertFalse(lobField.getAccess().isSortable());
        Assertions.assertFalse(lobField.getAccess().isFilterable());
        Assertions.assertTrue(lobField.getAccess().isPostable());
        Assertions.assertTrue(lobField.getAccess().isPatchable());

        Assertions.assertFalse(fieldAnnotatedField.getAccess().isSortable());
        Assertions.assertFalse(fieldAnnotatedField.getAccess().isFilterable());
        Assertions.assertTrue(fieldAnnotatedField.getAccess().isPostable());
        Assertions.assertFalse(fieldAnnotatedField.getAccess().isPatchable());

        Assertions.assertTrue(columnAnnotatedField.getAccess().isSortable());
        Assertions.assertTrue(columnAnnotatedField.getAccess().isFilterable());
        Assertions.assertFalse(columnAnnotatedField.getAccess().isPostable());
        Assertions.assertTrue(columnAnnotatedField.getAccess().isPatchable());

        MetaDataObject meta = jpaMetaProvider.discoverMeta(AnnotationTestEntity.class).asDataObject();
        Assertions.assertTrue(meta.getAttribute("lobValue").isLob());
        Assertions.assertFalse(meta.getAttribute("fieldAnnotatedValue").isLob());
    }

    @Test
    public void testRenamedResourceType() {
        ResourceInformation info = builder.build(RenamedTestEntity.class);
        Assertions.assertEquals("renamedResource", info.getResourceType());
    }

    @Test
    public void testJpaResourceAnnotationPath() {
        ResourceInformation info = builder.build(JpaResourcePathTestEntity.class);
        Assertions.assertEquals("jpaResourceTestEntity", info.getResourceType());
        Assertions.assertEquals("jpa-resource-test-entity", info.getResourcePath());
    }


    @Test
    public void testJsonapiResourceAnnotationPath() {
        ResourceInformation info = builder.build(JsonapiResourcePathTestEntity.class);
        Assertions.assertEquals("jsonapiResourceTestEntity", info.getResourceType());
        Assertions.assertEquals("jsonapi-resource-test-entity", info.getResourcePath());
    }


    @Test
    public void testReadOnlyField() throws SecurityException, IllegalArgumentException {
        ResourceInformation info = builder.build(AnnotationTestEntity.class);

        ResourceField field = info.findAttributeFieldByName("readOnlyValue");

        Assertions.assertFalse(field.getAccess().isPostable());
        Assertions.assertFalse(field.getAccess().isPatchable());

        MetaDataObject meta = jpaMetaProvider.discoverMeta(AnnotationTestEntity.class).asDataObject();
        MetaAttribute attribute = meta.getAttribute("readOnlyValue");

        Assertions.assertFalse(attribute.isInsertable());
        Assertions.assertFalse(attribute.isUpdatable());
    }

    @Test
    public void testMappedSuperclass() throws SecurityException, IllegalArgumentException {
        ResourceInformation info = builder.build(AnnotationMappedSuperclassEntity.class);

        Assertions.assertNull(info.getResourceType());

        ResourceField lobField = info.findAttributeFieldByName("lobValue");
        ResourceField fieldAnnotatedField = info.findAttributeFieldByName("fieldAnnotatedValue");
        ResourceField columnAnnotatedField = info.findAttributeFieldByName("columnAnnotatedValue");

        Assertions.assertFalse(lobField.getAccess().isSortable());
        Assertions.assertFalse(lobField.getAccess().isFilterable());
        Assertions.assertTrue(lobField.getAccess().isPostable());
        Assertions.assertTrue(lobField.getAccess().isPatchable());

        Assertions.assertFalse(fieldAnnotatedField.getAccess().isSortable());
        Assertions.assertFalse(fieldAnnotatedField.getAccess().isFilterable());
        Assertions.assertTrue(fieldAnnotatedField.getAccess().isPostable());
        Assertions.assertFalse(fieldAnnotatedField.getAccess().isPatchable());

        Assertions.assertTrue(columnAnnotatedField.getAccess().isSortable());
        Assertions.assertTrue(columnAnnotatedField.getAccess().isFilterable());
        Assertions.assertFalse(columnAnnotatedField.getAccess().isPostable());
        Assertions.assertTrue(columnAnnotatedField.getAccess().isPatchable());

        MetaDataObject meta = jpaMetaProvider.discoverMeta(AnnotationMappedSuperclassEntity.class).asDataObject();
        Assertions.assertTrue(meta.getAttribute("lobValue").isLob());
        Assertions.assertFalse(meta.getAttribute("fieldAnnotatedValue").isLob());
    }
}
