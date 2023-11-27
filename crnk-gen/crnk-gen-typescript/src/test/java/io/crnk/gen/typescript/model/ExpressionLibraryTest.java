package io.crnk.gen.typescript.model;

import io.crnk.gen.typescript.model.libraries.CrnkLibrary;
import io.crnk.test.mock.ClassTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExpressionLibraryTest {

	@Test
	public void checkHasPrivateConstructor() {
		ClassTestUtils.assertPrivateConstructor(CrnkLibrary.class);
	}

	@Test
	public void checkGetStringExpression() {
		Assertions.assertSame(CrnkLibrary.STRING_PATH, CrnkLibrary.getPrimitiveExpression("string"));
	}

	@Test
	public void checkGetNumberExpression() {
		Assertions.assertSame(CrnkLibrary.NUMBER_PATH, CrnkLibrary.getPrimitiveExpression("number"));
	}

	@Test
	public void checkGetBooleanExpression() {
		Assertions.assertSame(CrnkLibrary.BOOLEAN_PATH, CrnkLibrary.getPrimitiveExpression("boolean"));
	}

	@Test
	public void throwExceptionOnUnknownPrimitiveException() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			CrnkLibrary.getPrimitiveExpression("doesNotExist");
		});
	}
}
