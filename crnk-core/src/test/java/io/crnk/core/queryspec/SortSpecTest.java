package io.crnk.core.queryspec;

import java.util.Arrays;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SortSpecTest {

	@Test
	public void testBasic() {
		SortSpec spec = new SortSpec(Arrays.asList("name"), Direction.ASC);
		Assertions.assertEquals(Direction.ASC, spec.getDirection());
		Assertions.assertEquals(Arrays.asList("name"), spec.getAttributePath());
	}

	@Test
	public void testThrowExceptionOnNullPathArgument() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
		new SortSpec((PathSpec) null, Direction.ASC);
		});
	}

	@Test
	public void testThrowExceptionOnNullDirArgument() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
		new SortSpec(Arrays.asList("test"), null);
		});
	}


	@Test
	public void fromPathSpec() {
		SortSpec sort = PathSpec.of("a", "b").sort(Direction.ASC);
		Assertions.assertEquals(Direction.ASC, sort.getDirection());
		Assertions.assertEquals("a.b", sort.getPath().toString());
	}

	@Test
	public void testToString() {
		Assertions.assertEquals("name ASC", new SortSpec(Arrays.asList("name"), Direction.ASC).toString());
		Assertions.assertEquals("name1.name2 ASC", new SortSpec(Arrays.asList("name1", "name2"), Direction.ASC).toString());
		Assertions.assertEquals("name DESC", new SortSpec(Arrays.asList("name"), Direction.DESC).toString());
	}

	@Test
	public void testReverse() {
		SortSpec specAsc = new SortSpec(Arrays.asList("name1"), Direction.ASC);
		SortSpec specDesc = new SortSpec(Arrays.asList("name1"), Direction.DESC);
		Assertions.assertEquals(specDesc, specAsc.reverse());
		Assertions.assertEquals(specAsc, specDesc.reverse());
	}

	@Test
	public void testEquals() {
		EqualsVerifier.forClass(SortSpec.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();

		SortSpec spec1 = new SortSpec(Arrays.asList("name1"), Direction.ASC);
		SortSpec spec2 = new SortSpec(Arrays.asList("name1"), Direction.ASC);
		SortSpec spec3 = new SortSpec(Arrays.asList("name2"), Direction.ASC);
		SortSpec spec4 = new SortSpec(Arrays.asList("name1"), Direction.DESC);

		Assertions.assertEquals(spec1, spec1);
		Assertions.assertEquals(spec3, spec3);
		Assertions.assertEquals(spec1, spec2);
		Assertions.assertEquals(spec2, spec1);
		Assertions.assertEquals(spec1.hashCode(), spec1.hashCode());
		Assertions.assertEquals(spec3.hashCode(), spec3.hashCode());
		Assertions.assertEquals(spec1.hashCode(), spec2.hashCode());
		Assertions.assertNotEquals(spec2, spec3);
		Assertions.assertNotEquals(spec3, spec2);
		Assertions.assertNotEquals(spec1, spec4);
		Assertions.assertNotEquals(spec3, spec4);

		Assertions.assertEquals(spec1, SortSpec.asc(Arrays.asList("name1")));
		Assertions.assertEquals(spec4, SortSpec.desc(Arrays.asList("name1")));
		Assertions.assertNotEquals(spec1, null);
		Assertions.assertNotEquals(spec1, "test");
	}

	@Test
	public void testClone() {
		SortSpec sortSpec = new SortSpec(Arrays.asList("sortAttr"), Direction.ASC);
		SortSpec duplicate = sortSpec.clone();
		Assertions.assertNotSame(sortSpec, duplicate);
		Assertions.assertNotSame(sortSpec.getAttributePath(), duplicate.getAttributePath());
		Assertions.assertSame(sortSpec.getDirection(), duplicate.getDirection());
	}
}
