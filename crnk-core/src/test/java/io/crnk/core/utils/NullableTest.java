package io.crnk.core.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

public class NullableTest {


	@Test
	public void throwExceptionWhenNotPresent() {
		Assertions.assertThrows(NoSuchElementException.class, () -> {
			Nullable.empty().get();
		});
	}

	@Test
	public void ofNullable() {
		Assertions.assertEquals(13, Nullable.ofNullable(13).get().intValue());
		Assertions.assertFalse(Nullable.ofNullable(null).isPresent());
	}
}
