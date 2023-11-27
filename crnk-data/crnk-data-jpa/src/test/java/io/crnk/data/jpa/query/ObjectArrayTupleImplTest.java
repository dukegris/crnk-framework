package io.crnk.data.jpa.query;

import com.querydsl.core.types.Expression;
import io.crnk.data.jpa.internal.query.backend.querydsl.QuerydslObjectArrayTupleImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.persistence.TupleElement;

public class ObjectArrayTupleImplTest {

	private QuerydslObjectArrayTupleImpl impl = new QuerydslObjectArrayTupleImpl(new Object[]{"0", "1"}, null);

	@Test
	public void testGetByExpressionNotSupported() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
			impl.get((Expression<?>) null);
		});
	}

	@Test
	public void testGetByTupleNotSupported() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
			impl.get((TupleElement<?>) null);
		});
	}

	@Test
	public void testGetByNameNotSupported() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
			impl.get((String) null);
		});
	}

	@Test
	public void testGetElementsNotSupported() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
			impl.getElements();
		});
	}

	@Test
	public void testReduce() {
		Assertions.assertEquals(2, impl.size());
		Assertions.assertEquals(2, impl.size());
		Assertions.assertArrayEquals(new Object[]{"0", "1"}, impl.toArray());
		impl.reduce(1);
		Assertions.assertEquals("1", impl.get(0, String.class));
		Assertions.assertEquals(1, impl.size());
		Assertions.assertArrayEquals(new Object[]{"1"}, impl.toArray());
	}
}
