package io.crnk.rs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

public class CrnkFilterTest {


	@Test
	public void checkExceptionsGetWrappedWithWebApplicationException() throws IOException {
		CrnkFeature feature = Mockito.mock(CrnkFeature.class);
		CrnkFilter filter = new CrnkFilter(feature);

		ContainerRequestContext requestContext = Mockito.mock(ContainerRequestContext.class);
		Mockito.when(requestContext.getUriInfo()).thenThrow(new RuntimeException("test"));
		try {
			filter.filter(requestContext);
			Assertions.fail();
		} catch (WebApplicationException e) {
			Assertions.assertEquals("test", e.getCause().getMessage());
		}
	}

	@Test
	public void checkWebApplicationExceptionDoNotGetWrappedWithWebApplicationException() throws IOException {
		CrnkFeature feature = Mockito.mock(CrnkFeature.class);
		CrnkFilter filter = new CrnkFilter(feature);

		ContainerRequestContext requestContext = Mockito.mock(ContainerRequestContext.class);
		Mockito.when(requestContext.getUriInfo()).thenThrow(new WebApplicationException("test"));
		try {
			filter.filter(requestContext);
			Assertions.fail();
		} catch (WebApplicationException e) {
			Assertions.assertEquals("test", e.getMessage());
			Assertions.assertNull(e.getCause());
		}
	}

	@Test
	public void checkWebPathPrefixNullFilter() throws IOException {
		CrnkFeature feature = Mockito.mock(CrnkFeature.class);
		Mockito.when(feature.getWebPathPrefix()).thenReturn(null);
		Mockito.when(feature.getBoot()).thenThrow(new WebApplicationException("test"));

		CrnkFilter filter = new CrnkFilter(feature);
		UriInfo uriInfo = Mockito.mock(UriInfo.class);
		Mockito.when(uriInfo.getPath()).thenReturn("/tasks");
		Mockito.when(uriInfo.getQueryParameters()).thenReturn(Mockito.mock(MultivaluedMap.class));

		ContainerRequestContext requestContext = Mockito.mock(ContainerRequestContext.class);
		Mockito.when(requestContext.getUriInfo()).thenReturn(uriInfo);
		try {
			filter.filter(requestContext);
			Assertions.fail();
		} catch (WebApplicationException e) {
			Assertions.assertEquals("test", e.getMessage());
		}
	}

	@Test
	public void checkWebPathPrefixCorrectFilter() throws IOException {
		CrnkFeature feature = Mockito.mock(CrnkFeature.class);
		Mockito.when(feature.getWebPathPrefix()).thenReturn("api");
		Mockito.when(feature.getBoot()).thenThrow(new WebApplicationException("test"));

		CrnkFilter filter = new CrnkFilter(feature);
		UriInfo uriInfo = Mockito.mock(UriInfo.class);
		Mockito.when(uriInfo.getPath()).thenReturn("/api/tasks");
		Mockito.when(uriInfo.getQueryParameters()).thenReturn(Mockito.mock(MultivaluedMap.class));

		ContainerRequestContext requestContext = Mockito.mock(ContainerRequestContext.class);
		Mockito.when(requestContext.getUriInfo()).thenReturn(uriInfo);
		try {
			filter.filter(requestContext);
			Assertions.fail();
		} catch (WebApplicationException e) {
			Assertions.assertEquals("test", e.getMessage());
		}
	}

	@Test
	public void checkWebPathPrefixWrongNoFilter() throws IOException {
		CrnkFeature feature = Mockito.mock(CrnkFeature.class);
		Mockito.when(feature.getWebPathPrefix()).thenReturn("api");
		Mockito.when(feature.getBoot()).thenThrow(new WebApplicationException("test"));

		CrnkFilter filter = new CrnkFilter(feature);
		UriInfo uriInfo = Mockito.mock(UriInfo.class);
		Mockito.when(uriInfo.getPath()).thenReturn("/api2/tasks");
		Mockito.when(uriInfo.getQueryParameters()).thenReturn(Mockito.mock(MultivaluedMap.class));

		ContainerRequestContext requestContext = Mockito.mock(ContainerRequestContext.class);
		Mockito.when(requestContext.getUriInfo()).thenReturn(uriInfo);
		try {
			filter.filter(requestContext);
		} catch (WebApplicationException e) {
			Assertions.fail();
		}
	}
}
