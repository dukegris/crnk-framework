package io.crnk.gen.typescript.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TSFunctionTest {

	@Test
	public void notAField() {
		TSFunction function = new TSFunction();
		Assertions.assertFalse(function.isField());
	}

	@Test
	public void cannotCastToField() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
		TSFunction function = new TSFunction();
		function.asField();
		});
	}
}
