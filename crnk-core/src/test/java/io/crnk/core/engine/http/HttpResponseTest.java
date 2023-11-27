package io.crnk.core.engine.http;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpResponseTest {

	private HttpResponse response;

	@BeforeEach
	public void setup() {
		response = new HttpResponse();
	}

	@Test
	public void checkHeaderAccess() {
		Assertions.assertNotNull(response.getHeaders());

		Map<String, String> headers = new HashMap<>();
		response.setHeaders(headers);
		Assertions.assertSame(headers, response.getHeaders());

		response.setContentType("test");
		Assertions.assertEquals("test", headers.get(HttpHeaders.HTTP_CONTENT_TYPE));
	}

	@Test
	public void checkBodyUtf8Encoding() throws UnsupportedEncodingException {
		response.setBody("aäöü");
		Assertions.assertNotEquals(4, response.getBody().length);
		Assertions.assertEquals("aäöü", new String(response.getBody(), StandardCharsets.UTF_8));
	}
}
