package io.crnk.home;

import com.fasterxml.jackson.databind.JsonNode;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.http.HttpRequestContextBase;
import io.crnk.core.engine.http.HttpResponse;
import io.crnk.core.engine.internal.http.HttpRequestDispatcherImpl;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.meta.MetaModule;
import io.crnk.meta.MetaModuleConfig;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.test.mock.TestModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;

public class JsonApiFormatTest {

	private CrnkBoot boot;

	private HomeModule module;

	@BeforeEach
	public void setup() {
		MetaModuleConfig config = new MetaModuleConfig();
		config.addMetaProvider(new ResourceMetaProvider());
		MetaModule metaModule = MetaModule.createServerModule(config);

		this.module = Mockito.spy(HomeModule.create(HomeFormat.JSON_API));
		boot = new CrnkBoot();
		boot.addModule(module);
		boot.addModule(metaModule);
		boot.addModule(new TestModule());
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://localhost"));
		boot.boot();
	}


	@Test
	public void testWithAnyRequest() throws IOException {
		testJsonApiReturned(true);
	}


	@Test
	public void testWithJsonApiRequest() throws IOException {
		testJsonApiReturned(false);
	}

	private void testJsonApiReturned(boolean anyRequest) throws IOException {
		ArgumentCaptor<HttpResponse> responseCaptor = ArgumentCaptor.forClass(HttpResponse.class);

		HttpRequestContextBase requestContextBase = Mockito.mock(HttpRequestContextBase.class);

		Mockito.when(requestContextBase.getMethod()).thenReturn("GET");
		Mockito.when(requestContextBase.getPath()).thenReturn("/");
		Mockito.when(requestContextBase.getRequestHeader("Accept"))
				.thenReturn(anyRequest ? "*" : HttpHeaders.JSONAPI_CONTENT_TYPE);

		HttpRequestDispatcherImpl requestDispatcher = boot.getRequestDispatcher();
		requestDispatcher.process(requestContextBase);

		Mockito.verify(requestContextBase, Mockito.times(1)).setResponse(responseCaptor.capture());
		String expectedContentType = anyRequest ? HomeModule.JSON_CONTENT_TYPE : HttpHeaders.JSONAPI_CONTENT_TYPE;
		Assertions.assertEquals(expectedContentType, responseCaptor.getValue().getHeader(("Content-Type")));
		Assertions.assertEquals(200, responseCaptor.getValue().getStatusCode());

		String json = new String(responseCaptor.getValue().getBody());
		JsonNode response = boot.getObjectMapper().reader().readTree(json);

		JsonNode resourcesNode = response.get("links");
		JsonNode tasksNode = resourcesNode.get("tasks");
		Assertions.assertEquals("http://localhost/tasks", tasksNode.asText());

		Assertions.assertEquals("http://localhost/meta/", resourcesNode.get("meta").asText());
		Assertions.assertNull(resourcesNode.get("meta/resource"));
		Assertions.assertNull(resourcesNode.get("meta/attribute"));
	}

	@Test
	public void testRequestSubDirectory() throws IOException {
		ArgumentCaptor<HttpResponse> responseCaptor = ArgumentCaptor.forClass(HttpResponse.class);

		HttpRequestContextBase requestContextBase = Mockito.mock(HttpRequestContextBase.class);

		Mockito.when(requestContextBase.getMethod()).thenReturn("GET");
		Mockito.when(requestContextBase.getPath()).thenReturn("/meta/");

		HttpRequestDispatcherImpl requestDispatcher = boot.getRequestDispatcher();
		requestDispatcher.process(requestContextBase);

		Mockito.verify(requestContextBase, Mockito.times(1)).setResponse(responseCaptor.capture());
		String expectedContentType = HomeModule.JSON_CONTENT_TYPE;
		Assertions.assertEquals(expectedContentType, responseCaptor.getValue().getHeader("Content-Type"));
		Assertions.assertEquals(200, responseCaptor.getValue().getStatusCode());
		String json = new String(responseCaptor.getValue().getBody());
		JsonNode response = boot.getObjectMapper().reader().readTree(json);

		JsonNode linksNode = response.get("links");
		JsonNode resourceNode = linksNode.get("resource");
		Assertions.assertEquals("http://localhost/meta/resource", resourceNode.asText());

		Assertions.assertNull(linksNode.get("meta"));
		Assertions.assertNotNull(linksNode.get("element"));
		Assertions.assertNotNull(linksNode.get("attribute"));
	}
}
