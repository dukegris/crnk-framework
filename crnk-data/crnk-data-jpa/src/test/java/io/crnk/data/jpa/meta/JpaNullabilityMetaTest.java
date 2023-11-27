package io.crnk.data.jpa.meta;

import io.crnk.data.jpa.model.AnnotationTestEntity;
import io.crnk.data.jpa.model.TestEntity;
import io.crnk.meta.MetaLookupImpl;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public class JpaNullabilityMetaTest {

	private JpaMetaProvider metaProvider;

	@BeforeEach
	public void setup() {
		metaProvider = new JpaMetaProvider(Collections.emptySet());
		MetaLookupImpl lookup = new MetaLookupImpl();
		lookup.addProvider(metaProvider);
	}

	@Test
	public void testPrimaryKeyNotNullable() {
		MetaEntity meta = metaProvider.discoverMeta(TestEntity.class);
		MetaKey primaryKey = meta.getPrimaryKey();
		MetaAttribute idField = primaryKey.getElements().get(0);
		Assertions.assertFalse(idField.isNullable());
	}

	@Test
	public void testPrimitiveValueNotNullable() {
		MetaEntity meta = metaProvider.discoverMeta(TestEntity.class);
		MetaAttribute field = meta.getAttribute("longValue");
		Assertions.assertFalse(field.isNullable());
	}

	@Test
	public void testObjectValueNullable() {
		MetaEntity meta = metaProvider.discoverMeta(TestEntity.class);
		MetaAttribute field = meta.getAttribute("stringValue");
		Assertions.assertTrue(field.isNullable());
	}

	@Test
	public void testColumnAnnotatedValueIsNullable() {
		MetaEntity meta = metaProvider.discoverMeta(AnnotationTestEntity.class);
		MetaAttribute field = meta.getAttribute("nullableValue");
		Assertions.assertTrue(field.isNullable());
	}

	@Test
	public void testColumnAnnotatedValueIsNotNullable() {
		MetaEntity meta = metaProvider.discoverMeta(AnnotationTestEntity.class);
		MetaAttribute field = meta.getAttribute("notNullableValue");
		Assertions.assertFalse(field.isNullable());
	}

	@Test
	public void testNonOptionalRelatedValue() {
		MetaEntity meta = metaProvider.discoverMeta(AnnotationTestEntity.class);
		MetaAttribute field = meta.getAttribute("nonOptionalRelatedValue");
		Assertions.assertFalse(field.isNullable());
	}

	@Test
	public void testOptionalRelatedValue() {
		MetaEntity meta = metaProvider.discoverMeta(AnnotationTestEntity.class);
		MetaAttribute field = meta.getAttribute("optionalRelatedValue");
		Assertions.assertTrue(field.isNullable());
	}

}
