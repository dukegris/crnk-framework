package io.crnk.servlet.reactive;

import io.crnk.servlet.AsyncCrnkServlet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;

public class AsyncServletInitTest {

	@Test
	public void checkEnforceAsyncResultFactory() throws ServletException {
		try {
			AsyncCrnkServlet servlet = new AsyncCrnkServlet();
			servlet.init(Mockito.mock(ServletConfig.class));
			Assertions.fail();
		} catch (IllegalStateException e) {
			Assertions.assertTrue(e.getMessage().contains("make use of an async ResultFactory"));
		}
	}
}
