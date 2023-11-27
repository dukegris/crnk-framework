package io.crnk.core.engine.internal.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.Callable;


public class ExceptionUtilsTest {

	@Test
	public void testPrivateConstructor() {
		CoreClassTestUtils.assertPrivateConstructor(ExceptionUtil.class);
	}

	@Test
	public void testNoError() {
		Assertions.assertEquals(13, ExceptionUtil.wrapCatchedExceptions(new Callable<Object>() {
			@Override
			public Object call() {
				return 13;
			}
		}));
		Assertions.assertEquals(13, ExceptionUtil.wrapCatchedExceptions(new Callable<Object>() {
			@Override
			public Object call() {
				return 13;
			}
		}, "test"));
	}


	@Test
	public void testRuntimeException() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			ExceptionUtil.wrapCatchedExceptions(new Callable<Object>() {
				@Override
				public Object call() {
					throw new IllegalArgumentException();
				}
			});
		});
	}

	@Test
	public void testCheckedException() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			ExceptionUtil.wrapCatchedExceptions(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					throw new IOException();
				}
			});
		});
	}


	@Test
	public void testExceptionWithMessage() {
		try {
			ExceptionUtil.wrapCatchedExceptions(new Callable<Object>() {
				@Override
				public Object call() {
					throw new IllegalArgumentException();
				}
			}, "test %s", 13);
			Assertions.fail();
		} catch (IllegalStateException e) {
			Assertions.assertEquals("test 13", e.getMessage());
			Assertions.assertTrue(e.getCause() instanceof IllegalArgumentException);
		}
	}

}
