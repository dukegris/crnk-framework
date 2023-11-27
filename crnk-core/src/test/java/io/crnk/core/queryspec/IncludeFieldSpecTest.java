package io.crnk.core.queryspec;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IncludeFieldSpecTest {

	@Test
	public void testBasic() {
		IncludeFieldSpec spec = new IncludeFieldSpec(Arrays.asList("name"));
		Assertions.assertEquals(Arrays.asList("name"), spec.getAttributePath());
	}

	@Test
	public void testThrowExceptionOnNullArgument() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new IncludeFieldSpec((PathSpec) null);
		});
	}

	@Test
	public void testToString() {
		Assertions.assertEquals("name", new IncludeFieldSpec(Arrays.asList("name")).toString());
		Assertions.assertEquals("name1.name2", new IncludeFieldSpec(Arrays.asList("name1", "name2")).toString());
		Assertions.assertEquals("name", new IncludeFieldSpec(Arrays.asList("name")).toString());
	}

	@Test
	public void testEquals() {
		IncludeFieldSpec spec1 = new IncludeFieldSpec(Arrays.asList("name1"));
		IncludeFieldSpec spec2 = new IncludeFieldSpec(Arrays.asList("name1"));
		IncludeFieldSpec spec3 = new IncludeFieldSpec(Arrays.asList("name2"));
		IncludeRelationSpec rel = new IncludeRelationSpec(Arrays.asList("name2"));

		Assertions.assertEquals(spec1, spec1);
		Assertions.assertEquals(spec3, spec3);
		Assertions.assertEquals(spec1, spec2);
		Assertions.assertEquals(spec2, spec1);
		Assertions.assertEquals(spec1.hashCode(), spec1.hashCode());
		Assertions.assertEquals(spec3.hashCode(), spec3.hashCode());
		Assertions.assertEquals(spec1.hashCode(), spec2.hashCode());
		Assertions.assertNotEquals(spec2, spec3);
		Assertions.assertNotEquals(spec3, spec2);
		Assertions.assertNotEquals(spec1, rel);
	}

	@Test
	public void testClone() {
		IncludeFieldSpec spec = new IncludeFieldSpec(Arrays.asList("sortAttr"));
		IncludeFieldSpec duplicate = spec.clone();
		Assertions.assertNotSame(spec, duplicate);
		Assertions.assertNotSame(spec.getAttributePath(), duplicate.getAttributePath());
	}
}
