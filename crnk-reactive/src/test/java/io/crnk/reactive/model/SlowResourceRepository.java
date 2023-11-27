package io.crnk.reactive.model;

import io.crnk.core.engine.http.HttpRequestContextAware;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import org.junit.jupiter.api.Assertions;

import java.util.HashMap;
import java.util.Map;

public class SlowResourceRepository extends ResourceRepositoryBase<SlowTask, Long> implements HttpRequestContextAware {

	public static final int DELAY = 100;

	private Map<Long, SlowTask> resources = new HashMap<>();

	private HttpRequestContextProvider requestContextProvider;

	public SlowResourceRepository() {
		super(SlowTask.class);

		SlowTask task = new SlowTask();
		task.setId(1L);
		task.setName("task1");
		resources.put(task.getId(), task);
	}

	@Override
	public ResourceList<SlowTask> findAll(QuerySpec querySpec) {
		try {
			Assertions.assertNotNull(requestContextProvider);
			Assertions.assertTrue(requestContextProvider.hasThreadRequestContext());
			Assertions.assertNotNull(requestContextProvider.getRequestContext());
			Assertions.assertNotNull(requestContextProvider.getRequestContext().getQueryContext());

			Thread.sleep(DELAY);
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
		return querySpec.apply(resources.values());
	}

	public Map<Long, SlowTask> getMap() {
		return resources;
	}

	public void clear() {
		resources.clear();
	}

	@Override
	public void setHttpRequestContextProvider(HttpRequestContextProvider requestContextProvider) {
		this.requestContextProvider = requestContextProvider;
	}
}
