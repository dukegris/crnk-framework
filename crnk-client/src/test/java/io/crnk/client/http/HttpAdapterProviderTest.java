package io.crnk.client.http;

import io.crnk.client.AbstractClientTest;
import io.crnk.client.CrnkClient;
import io.crnk.client.http.apache.HttpClientAdapter;
import io.crnk.client.http.apache.HttpClientAdapterProvider;
import io.crnk.client.http.okhttp.OkHttpAdapter;
import io.crnk.client.http.okhttp.OkHttpAdapterProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HttpAdapterProviderTest extends AbstractClientTest {

	@BeforeEach
	public void setup() {
		client = new CrnkClient(getBaseUri().toString());
	}

	@Test
	public void shouldThrowExceptionIfNoProviderInstalled() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			client.getHttpAdapterProviders().clear();
			client.getHttpAdapter();
		});
	}

	@Test
	public void httpClientHasPriority() {
		Assertions.assertTrue(client.getHttpAdapter() instanceof OkHttpAdapter);
	}

	@Test
	public void testOkHttpProvider() {
		OkHttpAdapterProvider provider = new OkHttpAdapterProvider();
		Assertions.assertTrue(provider.isAvailable());
		Assertions.assertTrue(provider.newInstance() instanceof OkHttpAdapter);
	}

	@Test
	public void testHttpClientProvider() {
		HttpClientAdapterProvider provider = new HttpClientAdapterProvider();
		Assertions.assertTrue(provider.isAvailable());
		Assertions.assertTrue(provider.newInstance() instanceof HttpClientAdapter);
	}

}