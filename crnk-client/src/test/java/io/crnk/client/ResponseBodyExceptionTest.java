package io.crnk.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResponseBodyExceptionTest {

	@Test
	public void test() {
		ResponseBodyException exception = new ResponseBodyException("test");
		Assertions.assertEquals("test", exception.getMessage());
	}
}
