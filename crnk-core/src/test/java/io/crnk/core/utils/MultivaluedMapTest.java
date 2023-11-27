package io.crnk.core.utils;

import io.crnk.core.engine.internal.utils.MultivaluedMap;
import io.crnk.core.module.TestResource;
import io.crnk.core.queryspec.PathSpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class MultivaluedMapTest {

	@Test
	public void testFromCollection() {
		TestResource t1 = new TestResource();
		t1.setId(1);

		TestResource t2 = new TestResource();
		t2.setId(2);

		List<TestResource> list = new ArrayList<>();
		list.add(t1);
		list.add(t2);

		MultivaluedMap<Object, TestResource> map = MultivaluedMap.fromCollection(list, PathSpec.of("id"));
		Assertions.assertEquals(t1, map.getUnique(1));
		Assertions.assertEquals(t2, map.getUnique(2));
	}
}
