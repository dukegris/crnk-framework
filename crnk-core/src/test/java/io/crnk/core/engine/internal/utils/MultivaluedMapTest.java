package io.crnk.core.engine.internal.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class MultivaluedMapTest {

	@Test
	public void testBasics() {
		MultivaluedMap map = new MultivaluedMap();
		Assertions.assertTrue(map.isEmpty());
		Assertions.assertFalse(map.containsKey("a"));
		map.add("a", "b");
		Assertions.assertTrue(map.containsKey("a"));
		map.set("a", Arrays.asList("b"));
		Assertions.assertTrue(map.containsKey("a"));
		Assertions.assertEquals(Arrays.asList("b"), map.getList("a"));
	}

	@Test
	public void getUniqueThrowsExceptionOnDuplicate() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
		MultivaluedMap map = new MultivaluedMap();
		map.set("a", Arrays.asList("b", "c"));
		map.getUnique("a");
		});
	}

	@Test
	public void getUniqueReturnsResult() {
		MultivaluedMap map = new MultivaluedMap();
		map.set("a", Arrays.asList("b"));
		Assertions.assertEquals("b", map.getUnique("a"));
	}
}
