package io.crnk.client.http;

import io.crnk.client.http.okhttp.OkHttpAdapter;
import io.crnk.client.http.okhttp.OkHttpAdapterListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.TimeUnit;

public class OkHttpAdapterTest {


	@Test
	public void testCannotAddListenersAfterInitialization() {
		OkHttpAdapter adapter = new OkHttpAdapter();
		adapter.getImplementation();

		try {
			adapter.addListener(Mockito.mock(OkHttpAdapterListener.class));
			Assertions.fail();
		} catch (IllegalStateException e) {
			// ok
		}
	}

	@Test
	public void testCannotSetTimeoutAfterInitialization() {
		OkHttpAdapter adapter = new OkHttpAdapter();
		adapter.getImplementation();

		try {
			adapter.setReceiveTimeout(0, TimeUnit.DAYS);
			Assertions.fail();
		} catch (IllegalStateException e) {
			// ok
		}
	}
}
