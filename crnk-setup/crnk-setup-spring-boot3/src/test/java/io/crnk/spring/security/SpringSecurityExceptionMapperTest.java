package io.crnk.spring.security;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.spring.internal.AccessDeniedExceptionMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.Iterator;

public class SpringSecurityExceptionMapperTest {

	@Test
	public void testAccessDenied() {
		AccessDeniedExceptionMapper mapper = new AccessDeniedExceptionMapper();
		AccessDeniedException exception = new AccessDeniedException("hi");
		ErrorResponse response = mapper.toErrorResponse(exception);
		Iterable<ErrorData> errors = response.getErrors();
		Iterator<ErrorData> iterator = errors.iterator();
		ErrorData data = iterator.next();
		Assertions.assertFalse(iterator.hasNext());
		Assertions.assertEquals("403", data.getStatus());
		Assertions.assertEquals("hi", data.getCode());
		Assertions.assertTrue(mapper.accepts(response));
		AccessDeniedException fromErrorResponse = mapper.fromErrorResponse(response);
		Assertions.assertEquals("hi", fromErrorResponse.getMessage());
	}
}
