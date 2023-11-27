package io.crnk.core.engine.internal.utils;

import io.crnk.core.CoreTestModule;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.mock.models.Task;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JsonApiUrlBuilderTest {

	private JsonApiUrlBuilder urlBuilder;
	private Task task;
	private QueryContext context;

	@BeforeEach
	public void setup() {
		CrnkBoot boot = new CrnkBoot();
		boot.addModule(new CoreTestModule());
		boot.boot();
		urlBuilder = new JsonApiUrlBuilder(boot.getModuleRegistry());

		task = new Task();
		task.setId(42L);

		context = new QueryContext();
		context.setBaseUrl("http://foo.com/api");
	}

	@Test
	public void test() {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.addFilter(PathSpec.of("name").filter(FilterOperator.EQ, "bar"));
		Assertions.assertEquals("http://foo.com/api/tasks/42", urlBuilder.buildUrl(context, task));
		Assertions.assertEquals("http://foo.com/api/tasks/42?filter[name]=bar", urlBuilder.buildUrl(context, task, querySpec));
		Assertions.assertEquals("http://foo.com/api/tasks/42/relationships/project", urlBuilder.buildUrl(context, task, null, "project", true));
		Assertions.assertEquals("http://foo.com/api/tasks/42/project", urlBuilder.buildUrl(context, task, null, "project", false));
	}
}
