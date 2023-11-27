package io.crnk.core.engine.internal.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

public class WrappedListTest {

	@Test
	public void test() {
		ArrayList<String> rawList = new ArrayList<>();
		WrappedList<String> list = new WrappedList<>(rawList);
		Assertions.assertSame(rawList, list.getWrappedList());
		list.setWrappedList(new ArrayList<String>());
		Assertions.assertEquals(0, list.size());
		Assertions.assertEquals(list, list);
		Assertions.assertEquals(list.hashCode(), list.hashCode());
		Assertions.assertTrue(list.isEmpty());
		Assertions.assertFalse(list.contains("something"));
		Assertions.assertFalse(list.iterator().hasNext());
		Assertions.assertEquals(0, list.toArray().length);
		Assertions.assertEquals(0, list.toArray(new String[0]).length);
		Assertions.assertTrue(list.add("1"));
		Assertions.assertEquals("[1]", list.toString());
		Assertions.assertTrue(list.remove("1"));
		Assertions.assertFalse(list.containsAll(Arrays.asList("1")));
		Assertions.assertTrue(list.addAll(Arrays.asList("1", "2")));
		Assertions.assertTrue(list.removeAll(Arrays.asList("2")));
		list.retainAll(Arrays.asList("1", "3"));
		Assertions.assertEquals(1, list.size());
		Assertions.assertEquals("1", list.get(0));
		Assertions.assertEquals("1", list.set(0, "2"));
		list.clear();
		Assertions.assertTrue(list.isEmpty());
		list.add("1");
		list.remove(0);
		Assertions.assertTrue(list.isEmpty());
		list.add("1");
		Assertions.assertEquals(0, list.indexOf("1"));
		Assertions.assertEquals(0, list.lastIndexOf("1"));
		Assertions.assertEquals(-1, list.indexOf("2"));
		Assertions.assertTrue(list.listIterator().hasNext());
		Assertions.assertFalse(list.listIterator(1).hasNext());
		Assertions.assertEquals(1, list.subList(0, 1).size());
		list.add(0, "2");
		list.addAll(0, Arrays.asList("4", "3"));
		Assertions.assertEquals("[4, 3, 2, 1]", list.toString());
	}
}
