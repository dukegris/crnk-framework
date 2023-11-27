package io.crnk.data.jpa.util;

import io.crnk.data.jpa.internal.query.AnyUtils;
import io.crnk.data.jpa.meta.JpaMetaProvider;
import io.crnk.data.jpa.model.TestAnyType;
import io.crnk.meta.MetaLookupImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collections;

public class AnyTypeUtilsTest {

	private JpaMetaProvider metaProvider;

	@BeforeEach
	public void setup() {
		metaProvider = new JpaMetaProvider(Collections.emptySet());
		MetaLookupImpl lookup = new MetaLookupImpl();
		lookup.addProvider(metaProvider);
		metaProvider.discoverMeta(TestAnyType.class);
	}

	@Test
	public void testNotInstantiable()
			throws InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException,
			IllegalArgumentException, InvocationTargetException {
		Constructor<AnyUtils> constructor = AnyUtils.class.getDeclaredConstructor();
		Assertions.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	@Test
	public void testSet() {
		TestAnyType anyValue = new TestAnyType();
		AnyUtils.setValue(metaProvider.getPartition(), anyValue, "stringValue");
		Assertions.assertEquals("stringValue", anyValue.getStringValue());
		AnyUtils.setValue(metaProvider.getPartition(), anyValue, 12);
		Assertions.assertEquals(12, anyValue.getIntValue().intValue());
		Assertions.assertNull(anyValue.getStringValue());
		AnyUtils.setValue(metaProvider.getPartition(), anyValue, null);
		Assertions.assertNull(anyValue.getIntValue());
	}
}
