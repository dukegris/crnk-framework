package io.crnk.rs;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.rs.internal.WebApplicationExceptionMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.ws.rs.WebApplicationException;
import java.util.Iterator;

public class WebApplicationExceptionMapperTest {

	@Test
	public void test() {
		WebApplicationExceptionMapper mapper = new WebApplicationExceptionMapper();
		WebApplicationException exception = new WebApplicationException("hi");
		ErrorResponse response = mapper.toErrorResponse(exception);
		Iterable<ErrorData> errors = response.getErrors();
		Iterator<ErrorData> iterator = errors.iterator();
		ErrorData data = iterator.next();
		Assertions.assertFalse(iterator.hasNext());
		Assertions.assertEquals("500", data.getStatus());
		Assertions.assertEquals("hi", data.getCode());
		Assertions.assertTrue(mapper.accepts(response));
		WebApplicationException fromErrorResponse = mapper.fromErrorResponse(response);
		Assertions.assertEquals("hi", fromErrorResponse.getMessage());
	}
}
