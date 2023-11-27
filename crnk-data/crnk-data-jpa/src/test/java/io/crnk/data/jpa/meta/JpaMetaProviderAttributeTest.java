package io.crnk.data.jpa.meta;

import io.crnk.data.jpa.model.NamingTestEntity;
import io.crnk.data.jpa.model.RelatedEntity;
import io.crnk.data.jpa.model.TestEntity;
import io.crnk.data.jpa.model.WriteOnlyAttributeTestEntity;
import io.crnk.meta.MetaLookupImpl;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaCollectionType;
import io.crnk.meta.model.MetaMapType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

public class JpaMetaProviderAttributeTest {

	private JpaMetaProvider metaProvider;

	@BeforeEach
	public void setup() {
		metaProvider = new JpaMetaProvider(Collections.emptySet());
		MetaLookupImpl lookup = new MetaLookupImpl();
		lookup.addProvider(metaProvider);
	}

	@Test
	public void testPrimaryKey() {
		MetaEntity meta = metaProvider.discoverMeta(TestEntity.class);
		MetaAttribute attr = meta.getAttribute("id");
		Assertions.assertFalse(attr.isAssociation());
		Assertions.assertEquals("id", attr.getName());
		Assertions.assertEquals(TestEntity.class.getName() + ".id", attr.getId());
		Assertions.assertFalse(attr.isDerived());
		Assertions.assertFalse(attr.isVersion());
		Assertions.assertFalse(attr.isLazy());
		Assertions.assertFalse(attr.isCascaded());
		Assertions.assertNull(attr.getOppositeAttribute());
	}

	@Test
	public void testAttributeOrder() {
		MetaEntity meta = metaProvider.discoverMeta(TestEntity.class);

		List<? extends MetaAttribute> attributes = meta.getAttributes();

		Assertions.assertEquals("stringValue", attributes.get(0).getName());
		Assertions.assertEquals("superRelatedValue", attributes.get(1).getName());
		Assertions.assertEquals("id", attributes.get(2).getName());
		Assertions.assertEquals("longValue", attributes.get(3).getName());
		Assertions.assertEquals("bytesValue", attributes.get(4).getName());
		Assertions.assertEquals("embValue", attributes.get(5).getName());
		Assertions.assertEquals("mapValue", attributes.get(6).getName());
		Assertions.assertEquals("oneRelatedValue", attributes.get(7).getName());
		Assertions.assertEquals("eagerRelatedValue", attributes.get(8).getName());
		Assertions.assertEquals("manyRelatedValues", attributes.get(9).getName());
	}

	@Test
	public void testCascaded() {
		MetaEntity meta = metaProvider.discoverMeta(TestEntity.class);

		MetaAttribute oneRelatedAttr = meta.getAttribute("oneRelatedValue");
		MetaAttribute eagerRelatedAttr = meta.getAttribute("eagerRelatedValue");
		Assertions.assertTrue(oneRelatedAttr.isCascaded());
		Assertions.assertFalse(eagerRelatedAttr.isCascaded());
	}

	@Test
	public void testWriteOnlyAttributesIngoredAsNotYetSupported() {
		MetaEntity meta = metaProvider.discoverMeta(WriteOnlyAttributeTestEntity.class);

		Assertions.assertTrue(meta.hasAttribute("id"));

		// not yet supported
		Assertions.assertFalse(meta.hasAttribute("writeOnlyValue"));
	}

	@Test
	public void testFirstCharacterOfNameIsLowerCase() {
		MetaEntity meta = metaProvider.discoverMeta(NamingTestEntity.class);

		Assertions.assertTrue(meta.hasAttribute("id"));
		Assertions.assertTrue(meta.hasAttribute("sEcondUpperCaseValue"));
		Assertions.assertFalse(meta.hasAttribute("SEcondUpperCaseValue"));
	}

	@Test
	public void testMapAttr() {
		MetaEntity meta = metaProvider.discoverMeta(TestEntity.class);
		MetaAttribute attr = meta.getAttribute(TestEntity.ATTR_mapValue);
		Assertions.assertFalse(attr.isAssociation());
		Assertions.assertEquals(TestEntity.ATTR_mapValue, attr.getName());
		Assertions.assertEquals(TestEntity.class.getName() + "." + TestEntity.ATTR_mapValue, attr.getId());
		Assertions.assertFalse(attr.isDerived());
		Assertions.assertFalse(attr.isVersion());
		Assertions.assertFalse(attr.isLazy());
		Assertions.assertNull(attr.getOppositeAttribute());

		MetaMapType mapType = attr.getType().asMap();
		Assertions.assertTrue(mapType.isMap());
		Assertions.assertEquals(String.class, mapType.getKeyType().getImplementationClass());
		Assertions.assertEquals(String.class, mapType.getElementType().getImplementationClass());
		Assertions.assertEquals(String.class, attr.getType().getElementType().getImplementationClass());
	}

	@Test
	public void testRelationMany() {
		MetaEntity meta = metaProvider.discoverMeta(TestEntity.class);
		MetaAttribute attr = meta.getAttribute(TestEntity.ATTR_manyRelatedValues);
		Assertions.assertTrue(attr.isAssociation());
		Assertions.assertEquals(TestEntity.ATTR_manyRelatedValues, attr.getName());
		Assertions.assertEquals(TestEntity.class.getName() + "." + TestEntity.ATTR_manyRelatedValues, attr.getId());
		Assertions.assertFalse(attr.isDerived());
		Assertions.assertFalse(attr.isVersion());
		Assertions.assertTrue(attr.isLazy());
		Assertions.assertNotNull(attr.getOppositeAttribute());
		Assertions.assertFalse(attr.isOwner());
		Assertions.assertTrue(attr.getOppositeAttribute().isOwner());

		MetaCollectionType colType = attr.getType().asCollection();
		Assertions.assertTrue(colType.isCollection());
		Assertions.assertEquals(RelatedEntity.class, colType.getElementType().getImplementationClass());
		Assertions.assertEquals(RelatedEntity.class, attr.getType().getElementType().getImplementationClass());
	}
}
