package io.crnk.core.engine.http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DefaultHttpRequestProcessorTest {

	private HttpRequestProcessor processor = new HttpRequestProcessor() {
		@Override
		public boolean supportsAsync() {
			return false;
		}
	};

	@Test
	public void test() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
			processor.accepts(null);
		});
	}

	@Test
	public void processAsync() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
			processor.processAsync(null);
		});
	}
}
