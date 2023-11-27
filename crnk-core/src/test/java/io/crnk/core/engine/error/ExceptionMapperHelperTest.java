package io.crnk.core.engine.error;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.internal.utils.CoreClassTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExceptionMapperHelperTest {

	@Test
	public void hasPrivateConstructor() {
		CoreClassTestUtils.assertPrivateConstructor(ExceptionMapperHelper.class);
	}

	@Test
	public void test() {
		IllegalStateException exception = new IllegalStateException("test");
		ErrorResponse response = ExceptionMapperHelper.toErrorResponse(exception, 499, "illegal");

		Assertions.assertEquals(1, response.getErrors().size());
		ErrorData errorData = response.getErrors().iterator().next();

		Assertions.assertEquals("test", errorData.getCode());
		Assertions.assertEquals("test", errorData.getTitle());
		Assertions.assertEquals("499", errorData.getStatus());
		Assertions.assertEquals("illegal", errorData.getMeta().get("type"));

		Assertions.assertEquals(499, response.getHttpStatus());

		Assertions.assertEquals("test", ExceptionMapperHelper.createErrorMessage(response));

		Assertions.assertTrue(ExceptionMapperHelper.accepts(response, 499, "illegal"));
		Assertions.assertFalse(ExceptionMapperHelper.accepts(response, 1, "illegal"));
		Assertions.assertFalse(ExceptionMapperHelper.accepts(response, 499, "test"));
		Assertions.assertFalse(ExceptionMapperHelper.accepts(new ErrorResponseBuilder().setStatus(499).build(), 499, "illegal"));
	}
}
