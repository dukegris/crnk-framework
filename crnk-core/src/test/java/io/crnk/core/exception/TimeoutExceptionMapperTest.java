package io.crnk.core.exception;

import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.internal.exception.TimeoutExceptionMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

public class TimeoutExceptionMapperTest {

	@Test
	public void test() {
		TimeoutExceptionMapper mapper = new TimeoutExceptionMapper();
		ErrorResponse response = mapper.toErrorResponse(new TimeoutException());
		assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT_504);

		Assertions.assertTrue(mapper.accepts(response));
		Assertions.assertTrue(mapper.fromErrorResponse(response) instanceof TimeoutException);
	}
}