package io.crnk.core.engine.internal.utils;

import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class CoreClassTestUtils {

	public static void assertPrivateConstructor(Class<?> clazz) {
		Constructor[] constructors = clazz.getDeclaredConstructors();
		Assertions.assertEquals(1, constructors.length);
		Assertions.assertTrue(Modifier.isPrivate(constructors[0].getModifiers()));

		// ensure coverage
		try {
			constructors[0].setAccessible(true);
			Assertions.assertNotNull(constructors[0].newInstance());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static void assertProtectedConstructor(Class<?> clazz) {
		try {
			Constructor constructor = clazz.getDeclaredConstructor();
			Assertions.assertTrue(Modifier.isProtected(constructor.getModifiers()));
			constructor.setAccessible(true);
			Assertions.assertNotNull(constructor.newInstance());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
