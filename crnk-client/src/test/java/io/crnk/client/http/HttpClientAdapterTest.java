package io.crnk.client.http;

import io.crnk.client.http.apache.HttpClientAdapter;
import io.crnk.client.http.apache.HttpClientAdapterListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.TimeUnit;

public class HttpClientAdapterTest {

	@Test
	public void testCannotAddListenersAfterInitialization() {
		HttpClientAdapter adapter = new HttpClientAdapter();
		adapter.getImplementation();

		try {
			adapter.addListener(Mockito.mock(HttpClientAdapterListener.class));
			Assertions.fail();
		} catch (IllegalStateException e) {
			// ok
		}
	}

	@Test
	public void testCannotSetTimeoutAfterInitialization() {
		HttpClientAdapter adapter = new HttpClientAdapter();
		adapter.getImplementation();

		try {
			adapter.setReceiveTimeout(0, TimeUnit.DAYS);
			Assertions.fail();
		} catch (IllegalStateException e) {
			// ok
		}
	}
}
