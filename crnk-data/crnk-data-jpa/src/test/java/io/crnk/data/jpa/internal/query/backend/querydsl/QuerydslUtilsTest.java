package io.crnk.data.jpa.internal.query.backend.querydsl;

import com.querydsl.core.types.Expression;
import io.crnk.test.mock.ClassTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class QuerydslUtilsTest {

	@Test
	public void checkHasPrivateConstructor() {
		ClassTestUtils.assertPrivateConstructor(QuerydslUtils.class);
	}

	@Test
	public void throwExceptionWhenAccessingInvalidEntityPath() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
		QuerydslUtils.getEntityPath(InvalidEntity.class);
		});
	}

	@Test
	public void throwExceptionWhenGettingInvalidQueryClass() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
		QuerydslUtils.getQueryClass(InvalidEntity.class);
		});
	}

	@Test
	public void throwExceptionWhenFollowingInvalidPath() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
		Expression<?> expression = Mockito.mock(Expression.class);
		Mockito.when(expression.getType()).thenReturn((Class) InvalidEntity.class);
		QuerydslUtils.get(expression, "doesNotExist");
		});
	}

	class InvalidEntity {

	}
}