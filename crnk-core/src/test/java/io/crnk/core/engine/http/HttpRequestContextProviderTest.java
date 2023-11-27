package io.crnk.core.engine.http;


import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.result.ImmediateResultFactory;
import io.crnk.core.module.ModuleRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class HttpRequestContextProviderTest {

	@Test
	public void test() {
		ModuleRegistry moduleRegistry = Mockito.mock(ModuleRegistry.class);
		ResourceRegistry resourceRegistry = Mockito.mock(ResourceRegistry.class);
		Mockito.when(resourceRegistry.getLatestVersion()).thenReturn(2);
		Mockito.when(moduleRegistry.getResourceRegistry()).thenReturn(resourceRegistry);

		ImmediateResultFactory resultFactory = new ImmediateResultFactory();
		HttpRequestContextProvider provider = new HttpRequestContextProvider(() -> resultFactory, moduleRegistry);

		HttpRequestContext context = Mockito.mock(HttpRequestContext.class);
		Mockito.when(context.getBaseUrl()).thenReturn("http://test");
		QueryContext queryContext = new QueryContext();
		Mockito.when(context.getQueryContext()).thenReturn(queryContext);

		Assertions.assertFalse(provider.hasThreadRequestContext());

		provider.onRequestStarted(context);
		Assertions.assertTrue(provider.hasThreadRequestContext());
		Assertions.assertSame(context, provider.getRequestContext());
		Assertions.assertEquals("http://test", provider.getServiceUrlProvider().getUrl());
		provider.onRequestFinished();
		Assertions.assertFalse(provider.hasThreadRequestContext());
		Assertions.assertEquals(2, queryContext.getRequestVersion()); // set to latest from registry
	}
}
