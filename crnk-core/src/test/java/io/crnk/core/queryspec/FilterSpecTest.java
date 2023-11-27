package io.crnk.core.queryspec;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class FilterSpecTest {

	@Test
	public void testBasic() {
		FilterSpec spec = new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, "test");
		Assertions.assertEquals("test", spec.getValue());
		Assertions.assertEquals(FilterOperator.EQ, spec.getOperator());
		Assertions.assertEquals(Arrays.asList("name"), spec.getAttributePath());
		Assertions.assertFalse(spec.hasExpressions());
		spec.setValue("newValue");
		Assertions.assertEquals("newValue", spec.getValue());
	}

	@Test
	public void fromPathSpec() {
		FilterSpec filter = PathSpec.of("a", "b").filter(FilterOperator.EQ, "12");
		Assertions.assertEquals("12", filter.getValue());
		Assertions.assertEquals(FilterOperator.EQ, filter.getOperator());
		Assertions.assertEquals("a.b", filter.getPath().toString());
	}

	@Test
	public void testNullOperatorThrowsException() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new FilterSpec(Arrays.asList("name"), null, "test");
		});
	}

	@Test
	public void testAndOperatorWithValueThrowsException() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new FilterSpec(Arrays.asList("name"), FilterOperator.AND, "test");
		});
	}

	@Test
	public void testOrOperatorWithValueThrowsException() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new FilterSpec(Arrays.asList("name"), FilterOperator.OR, "test");
		});
	}

	@Test
	public void testNotOperatorWithValueThrowsException() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new FilterSpec(Arrays.asList("name"), FilterOperator.NOT, "test");
		});
	}

	@Test
	public void testCloneBasic() {
		FilterSpec spec = new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, "test");
		Assertions.assertEquals(spec, spec.clone());
	}

	@Test
	public void testCloneExpressions() {
		FilterSpec spec1 = new FilterSpec(Arrays.asList("name1"), FilterOperator.EQ, "test");
		FilterSpec spec2 = new FilterSpec(Arrays.asList("name2"), FilterOperator.EQ, "test");
		FilterSpec andSpec = FilterSpec.and(spec1, spec2);
		Assertions.assertEquals(andSpec, andSpec.clone());
	}

	@Test
	public void testToString() {
		Assertions.assertEquals("name EQ test",
				new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, "test").toString());
		Assertions.assertEquals("name1.name2 EQ test",
				new FilterSpec(Arrays.asList("name1", "name2"), FilterOperator.EQ, "test").toString());
		Assertions.assertEquals("name NEQ test",
				new FilterSpec(Arrays.asList("name"), FilterOperator.NEQ, "test").toString());
		Assertions.assertEquals("NOT(name NEQ test)",
				FilterSpec.not(new FilterSpec(Arrays.asList("name"), FilterOperator.NEQ, "test")).toString());
		Assertions.assertEquals("(name1 NEQ test1) AND (name2 EQ test2)",
				FilterSpec.and(new FilterSpec(Arrays.asList("name1"), FilterOperator.NEQ, "test1"),
						new FilterSpec(Arrays.asList("name2"), FilterOperator.EQ, "test2")).toString());
		Assertions.assertEquals("NOT((name1 NEQ test1) AND (name2 EQ test2))",
				new FilterSpec(FilterOperator.NOT,
						Arrays.asList(new FilterSpec(Arrays.asList("name1"), FilterOperator.NEQ, "test1"),
								new FilterSpec(Arrays.asList("name2"), FilterOperator.EQ, "test2"))).toString());

		Assertions.assertEquals("NOT(name2 EQ test2)", new FilterSpec(FilterOperator.NOT,
				Arrays.asList(new FilterSpec(Arrays.asList("name2"), FilterOperator.EQ, "test2"))).toString());

	}

	@Test
	public void testAndTwoExpr() {
		FilterSpec spec1 = new FilterSpec(Arrays.asList("name1"), FilterOperator.EQ, "test");
		FilterSpec spec2 = new FilterSpec(Arrays.asList("name2"), FilterOperator.EQ, "test");
		FilterSpec andSpec = FilterSpec.and(spec1, spec2);
		Assertions.assertTrue(andSpec.hasExpressions());
		Assertions.assertEquals(FilterOperator.AND, andSpec.getOperator());
		Assertions.assertEquals(2, andSpec.getExpression().size());
	}

	@Test
	public void testAndOneExpr() {
		FilterSpec spec1 = new FilterSpec(Arrays.asList("name1"), FilterOperator.EQ, "test");
		FilterSpec andSpec = FilterSpec.and(spec1);
		Assertions.assertSame(spec1, andSpec);
	}

	@Test
	public void testOrTwoExpr() {
		FilterSpec spec1 = new FilterSpec(Arrays.asList("name1"), FilterOperator.EQ, "test");
		FilterSpec spec2 = new FilterSpec(Arrays.asList("name2"), FilterOperator.EQ, "test");
		FilterSpec orSpec = FilterSpec.or(spec1, spec2);
		Assertions.assertTrue(orSpec.hasExpressions());
		Assertions.assertEquals(FilterOperator.OR, orSpec.getOperator());
		Assertions.assertEquals(2, orSpec.getExpression().size());
	}

	@Test
	public void testOrTwoExprList() {
		FilterSpec spec1 = new FilterSpec(Arrays.asList("name1"), FilterOperator.EQ, "test");
		FilterSpec spec2 = new FilterSpec(Arrays.asList("name2"), FilterOperator.EQ, "test");
		FilterSpec orSpec = FilterSpec.or(Arrays.asList(spec1, spec2));
		Assertions.assertTrue(orSpec.hasExpressions());
		Assertions.assertEquals(FilterOperator.OR, orSpec.getOperator());
		Assertions.assertEquals(2, orSpec.getExpression().size());
	}

	@Test
	public void testOrOneExpr() {
		FilterSpec spec1 = new FilterSpec(Arrays.asList("name1"), FilterOperator.EQ, "test");
		FilterSpec orSpec = FilterSpec.or(spec1);
		Assertions.assertSame(spec1, orSpec);
	}

	@Test
	public void testEquals() {
		FilterSpec spec1A = new FilterSpec(Arrays.asList("name1"), FilterOperator.EQ, "test");
		FilterSpec spec2A = new FilterSpec(Arrays.asList("name2"), FilterOperator.EQ, "test");
		FilterSpec orSpecA = FilterSpec.or(spec1A, spec2A);

		FilterSpec spec1B = new FilterSpec(Arrays.asList("name1"), FilterOperator.EQ, "test");
		FilterSpec spec2B = new FilterSpec(Arrays.asList("name2"), FilterOperator.EQ, "test");
		FilterSpec orSpecB = FilterSpec.or(spec1B, spec2B);
		FilterSpec notSpec = FilterSpec.not(spec1A);

		Assertions.assertEquals(orSpecA, orSpecB);
		Assertions.assertEquals(spec1A, spec1A);
		Assertions.assertEquals(spec1A, spec1B);
		Assertions.assertEquals(spec2A, spec2B);
		Assertions.assertEquals(orSpecA.hashCode(), orSpecB.hashCode());
		Assertions.assertEquals(spec1A.hashCode(), spec1B.hashCode());
		Assertions.assertEquals(spec2A.hashCode(), spec2B.hashCode());
		Assertions.assertNotEquals(spec1A, spec2B);
		Assertions.assertNotEquals(spec1A, "somethingDifferent");
		Assertions.assertNotEquals(spec1A, null);
		Assertions.assertNotEquals(spec2A, spec1B);
		Assertions.assertNotEquals(orSpecA, spec1B);
		Assertions.assertNotEquals(spec2B, orSpecA);
		Assertions.assertNotEquals(spec2B, notSpec);
		Assertions.assertEquals(notSpec, notSpec);
		Assertions.assertEquals(orSpecB, orSpecB);
		Assertions.assertNotEquals(notSpec, orSpecB);
	}

	@Test
	public void testNormalize() {
		FilterSpec spec1A = new FilterSpec(Arrays.asList("name1"), FilterOperator.EQ, "test");
		FilterSpec spec2A = new FilterSpec(Arrays.asList("name2"), FilterOperator.EQ, "test");
		FilterSpec orSpecA = FilterSpec.or(spec1A, spec2A);

		FilterSpec spec1B = new FilterSpec(Arrays.asList("name2"), FilterOperator.EQ, "test");
		FilterSpec spec2B = new FilterSpec(Arrays.asList("name1"), FilterOperator.EQ, "test");
		FilterSpec orSpecB = FilterSpec.or(spec1B, spec2B);
		Assertions.assertNotEquals(orSpecA, orSpecB);

		// A does not change since sorted alphabetically
		Assertions.assertEquals(orSpecA, orSpecA.normalize());

		// norm B equals A
		FilterSpec norm = orSpecB.normalize();
		Assertions.assertEquals(orSpecA, norm);
	}

}
