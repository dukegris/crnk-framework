package io.crnk.data.jpa.meta;

import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.data.jpa.AbstractJpaJerseyTest;
import io.crnk.data.jpa.model.AnnotationMappedSubtypeEntity;
import io.crnk.data.jpa.model.AnnotationTestEntity;
import io.crnk.data.jpa.model.RenamedTestEntity;
import io.crnk.data.jpa.model.SequenceEntity;
import io.crnk.data.jpa.model.TestEntity;
import io.crnk.data.jpa.model.VersionedEntity;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.resource.MetaJsonObject;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

public class JpaMetaEndToEndTest extends AbstractJpaJerseyTest {

    @Override
    @BeforeEach
    public void setup() {
        super.setup();
    }

    @Test
    public void test() {
        MetaResource testMeta = resourceMetaProvider.getMeta(TestEntity.class);
        Assertions.assertNotNull(testMeta);

        MetaAttribute embAttrMeta = testMeta.getAttribute(TestEntity.ATTR_embValue);
        Assertions.assertEquals(MetaJsonObject.class, embAttrMeta.getType().getClass());
    }

    @Test
    public void testProjectedLob() {
        MetaResource metaResource = resourceMetaProvider.getMeta(AnnotationTestEntity.class);
        MetaAttribute lobAttr = metaResource.getAttribute("lobValue");
        Assertions.assertTrue(lobAttr.isLob());
    }

    @Test
    public void testRenameResourceType() {
        MetaResource metaResource = resourceMetaProvider.getMeta(RenamedTestEntity.class);
        Assertions.assertEquals("renamedResource", metaResource.getResourceType());

        RenamedTestEntity test = new RenamedTestEntity();
        test.setId(1L);

        ResourceRepository<RenamedTestEntity, Serializable> repository = client.getRepositoryForType(RenamedTestEntity.class);
        repository.create(test);
        Assertions.assertEquals(1, repository.findAll(new QuerySpec(RenamedTestEntity.class)).size());
        repository.delete(1L);
        Assertions.assertEquals(0, repository.findAll(new QuerySpec(RenamedTestEntity.class)).size());
    }

    @Test
    public void testQueryRenameAttribute() {
        MetaResource metaResource = resourceMetaProvider.getMeta(RenamedTestEntity.class);
        Assertions.assertEquals("renamedResource", metaResource.getResourceType());

        RenamedTestEntity test = new RenamedTestEntity();
        test.setId(1L);
        test.setFullName("john");
        ResourceRepository<RenamedTestEntity, Serializable> repository = client.getRepositoryForType(RenamedTestEntity.class);
        repository.create(test);

        QuerySpec querySpec = new QuerySpec(RenamedTestEntity.class);
        querySpec.addFilter(PathSpec.of("fullName").filter(FilterOperator.EQ, "john"));
        Assertions.assertEquals(1, repository.findAll(querySpec).size());

        QuerySpec jsonQuerySpec = new QuerySpec(RenamedTestEntity.class);
        jsonQuerySpec.addFilter(new FilterSpec("{\"full-name\": \"john\" }"));
        Assertions.assertEquals(1, repository.findAll(jsonQuerySpec).size());

        repository.delete(1L);
    }

    @Test
    public void testProjectedLobOnMappedSuperclass() {
        MetaResourceBase metaResource = resourceMetaProvider.getMeta(AnnotationMappedSubtypeEntity.class);
        MetaAttribute lobAttr = metaResource.getAttribute("lobValue");
        Assertions.assertTrue(lobAttr.isLob());
    }

    @Test
    public void testProjectedColumnAnnotatedValueIsNotNullable() {
        MetaResourceBase meta = resourceMetaProvider.getMeta(AnnotationTestEntity.class);
        MetaAttribute field = meta.getAttribute("notNullableValue");
        Assertions.assertFalse(field.isNullable());
    }

    @Test
    public void testProjectedColumnAnnotatedValueIsNullable() {
        MetaResourceBase meta = resourceMetaProvider.getMeta(AnnotationTestEntity.class);
        MetaAttribute field = meta.getAttribute("nullableValue");
        Assertions.assertTrue(field.isNullable());
    }

    @Test
    public void testProjectedVersion() {
        MetaResource metaResource = resourceMetaProvider.getMeta(VersionedEntity.class);
        MetaAttribute versionAttr = metaResource.getAttribute("version");
        Assertions.assertTrue(versionAttr.isVersion());
    }

    @Test
    public void testCascaded() {
        MetaResource meta = resourceMetaProvider.getMeta(TestEntity.class);
        MetaAttribute oneRelatedAttr = meta.getAttribute("oneRelatedValue");
        MetaAttribute eagerRelatedAttr = meta.getAttribute("eagerRelatedValue");
        Assertions.assertTrue(oneRelatedAttr.isCascaded());
        Assertions.assertFalse(eagerRelatedAttr.isCascaded());
    }

    @Test
    public void testAttributeInsertableUpdatable() {
        MetaResource versionMeta = resourceMetaProvider.getMeta(VersionedEntity.class);
        MetaAttribute idAttr = versionMeta.getAttribute("id");
        MetaAttribute valueAttr = versionMeta.getAttribute("longValue");
        Assertions.assertTrue(idAttr.isInsertable());
        Assertions.assertFalse(idAttr.isUpdatable());
        Assertions.assertTrue(valueAttr.isInsertable());
        Assertions.assertTrue(valueAttr.isUpdatable());

        MetaResourceBase annotationMeta = resourceMetaProvider.getMeta(AnnotationTestEntity.class);
        MetaAttribute fieldAnnotatedAttr = annotationMeta.getAttribute("fieldAnnotatedValue");
        MetaAttribute columnAnnotatedAttr = annotationMeta.getAttribute("columnAnnotatedValue");
        Assertions.assertTrue(fieldAnnotatedAttr.isInsertable());
        Assertions.assertFalse(fieldAnnotatedAttr.isUpdatable());
        Assertions.assertFalse(fieldAnnotatedAttr.isSortable());
        Assertions.assertFalse(fieldAnnotatedAttr.isFilterable());
        Assertions.assertFalse(columnAnnotatedAttr.isInsertable());
        Assertions.assertTrue(columnAnnotatedAttr.isUpdatable());
        Assertions.assertTrue(columnAnnotatedAttr.isSortable());
        Assertions.assertTrue(columnAnnotatedAttr.isFilterable());

        MetaAttribute embeddableValueAttr = annotationMeta.getAttribute("embeddableValue");
        Assertions.assertFalse(embeddableValueAttr.isInsertable());
        Assertions.assertTrue(embeddableValueAttr.isUpdatable());
        Assertions.assertTrue(embeddableValueAttr.isSortable());
        Assertions.assertFalse(embeddableValueAttr.isFilterable());

        MetaResourceBase superMeta = resourceMetaProvider.getMeta(AnnotationMappedSubtypeEntity.class);
        fieldAnnotatedAttr = superMeta.getAttribute("fieldAnnotatedValue");
        columnAnnotatedAttr = superMeta.getAttribute("columnAnnotatedValue");
        MetaAttribute lobAttr = superMeta.getAttribute("lobValue");
        Assertions.assertTrue(fieldAnnotatedAttr.isInsertable());
        Assertions.assertFalse(fieldAnnotatedAttr.isUpdatable());
        Assertions.assertFalse(fieldAnnotatedAttr.isSortable());
        Assertions.assertFalse(fieldAnnotatedAttr.isFilterable());
        Assertions.assertFalse(columnAnnotatedAttr.isInsertable());
        Assertions.assertTrue(columnAnnotatedAttr.isUpdatable());
        Assertions.assertTrue(columnAnnotatedAttr.isSortable());
        Assertions.assertTrue(columnAnnotatedAttr.isFilterable());
        Assertions.assertTrue(lobAttr.isInsertable());
        Assertions.assertTrue(lobAttr.isUpdatable());
        Assertions.assertFalse(lobAttr.isSortable());
        Assertions.assertFalse(lobAttr.isFilterable());

    }

    @Test
    public void testProjectedSequencePrimaryKey() {
        MetaResource metaResource = resourceMetaProvider.getMeta(SequenceEntity.class);
        Assertions.assertTrue(metaResource.getPrimaryKey().isGenerated());
    }
}
