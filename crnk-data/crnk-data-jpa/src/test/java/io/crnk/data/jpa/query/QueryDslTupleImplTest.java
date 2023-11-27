package io.crnk.data.jpa.query;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import io.crnk.data.jpa.internal.query.backend.querydsl.QuerydslTupleImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

public class QueryDslTupleImplTest {

	private QuerydslTupleImpl impl;
	private Expression expression;

	@BeforeEach
	public void setup() {

		expression = Mockito.mock(Expression.class);
		Tuple tuple = Mockito.mock(Tuple.class);
		Mockito.when(tuple.size()).thenReturn(2);
		Mockito.when(tuple.get(expression)).thenReturn("test");
		Mockito.when(tuple.toArray()).thenReturn(new Object[]{"0", "1"});
		Mockito.when(tuple.get(0, String.class)).thenReturn("0");
		Mockito.when(tuple.get(1, String.class)).thenReturn("1");

		Mockito.when(tuple.size()).thenReturn(2);
		Map<String, Integer> selectionBindings = new HashMap<>();
		impl = new QuerydslTupleImpl(tuple, selectionBindings);
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

		Assertions.assertEquals("test", impl.get(expression));

	}
}
