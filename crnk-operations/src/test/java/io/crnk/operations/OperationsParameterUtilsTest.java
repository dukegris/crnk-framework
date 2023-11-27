package io.crnk.operations;

import io.crnk.operations.internal.OperationParameterUtils;
import io.crnk.test.mock.ClassTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

public class OperationsParameterUtilsTest {


	@Test
	public void testHasPrivateConstructor() {
		ClassTestUtils.assertPrivateConstructor(OperationParameterUtils.class);
	}

	@Test
	public void testParseSingleParameter() {
		Map<String, Set<String>> map = OperationParameterUtils.parseParameters("test?a=b");
		Assertions.assertEquals(1, map.size());
		Assertions.assertEquals("b", map.get("a").iterator().next());
		Assertions.assertEquals(1, map.get("a").size());
	}

	@Test
	public void testParseRepeatedParameter() {
		Map<String, Set<String>> map = OperationParameterUtils.parseParameters("test?a=b&a=c");
		Assertions.assertEquals(1, map.size());
		Assertions.assertEquals(2, map.get("a").size());
		Assertions.assertTrue(map.get("a").contains("b"));
		Assertions.assertTrue(map.get("a").contains("c"));
	}

	@Test
	public void testParseMultipleParameter() {
		Map<String, Set<String>> map = OperationParameterUtils.parseParameters("test?a=b&c=d");
		Assertions.assertEquals(2, map.size());
		Assertions.assertEquals(1, map.get("a").size());
		Assertions.assertEquals(1, map.get("c").size());

		Assertions.assertEquals("b", map.get("a").iterator().next());
		Assertions.assertEquals("d", map.get("c").iterator().next());
	}

	@Test
	public void testParseNoParameter() {
		Assertions.assertTrue(OperationParameterUtils.parseParameters("test").isEmpty());
		Assertions.assertTrue(OperationParameterUtils.parseParameters("test?").isEmpty());
	}

	@Test
	public void testParsePath() {
		Assertions.assertEquals("test", OperationParameterUtils.parsePath("test?a=b"));
		Assertions.assertEquals("test", OperationParameterUtils.parsePath("test"));
	}
}
