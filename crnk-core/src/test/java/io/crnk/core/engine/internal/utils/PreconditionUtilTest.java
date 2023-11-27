package io.crnk.core.engine.internal.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

public class PreconditionUtilTest {

	@Test
	public void testConstructorIsPrivate()
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		Constructor<PreconditionUtil> constructor = PreconditionUtil.class.getDeclaredConstructor();
		Assertions.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	@Test
	public void testSatisfied() {
		PreconditionUtil.assertEquals(null, null, null);
		PreconditionUtil.assertEquals(null, 1, 1);
		PreconditionUtil.assertTrue(null, true);
		PreconditionUtil.assertFalse(null, false);
		PreconditionUtil.assertNotNull(null, "test");
		PreconditionUtil.assertNull(null, null);
	}

	@Test
	public void testObjectEqualsNotSatisfied() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			PreconditionUtil.assertEquals("message", new Object(), new Object());
		});
	}


	@Test
	public void testEqualsNotSatisfied() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			PreconditionUtil.assertEquals(null, 1, 2);
		});
	}

	@Test
	public void testEqualsNotSatisfied2() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			PreconditionUtil.assertEquals(null, "a", "b");
		});
	}

	@Test
	public void testTrueNotSatisfied() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			PreconditionUtil.assertTrue(null, false);
		});
	}

	@Test
	public void testFalseNotSatisfied() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			PreconditionUtil.assertFalse(null, true);
		});
	}

	@Test
	public void testNotNullNotSatisfied() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			PreconditionUtil.assertNotNull(null, null);
		});
	}

	@Test
	public void testNullNotSatisfied() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			PreconditionUtil.assertNull(null, "not null");
		});
	}
}
