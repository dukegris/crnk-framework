package io.crnk.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClientExceptionTest {

	@Test
	public void test() {
		ClientException exception = new ClientException(400, "test");
		Assertions.assertEquals("400", exception.getErrorData().getStatus());
		Assertions.assertEquals("test", exception.getMessage());
	}
}
