package io.crnk.core.engine.repository;

import io.crnk.core.CoreTestContainer;
import io.crnk.core.CoreTestModule;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.exception.MethodNotAllowedException;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.mock.models.Task;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ResourceRepositoryBaseTest {

	private TestRepository repository;

	class TestRepository extends ResourceRepositoryBase<Task, Integer> {

		TestRepository() {
			super(Task.class);
		}

		@Override
		public ResourceList<Task> findAll(QuerySpec querySpec) {
			return new DefaultResourceList<>();
		}
	}

	@BeforeEach
	public void setup() {
		CoreTestContainer container = new CoreTestContainer();
		container.addModule(new CoreTestModule());
		container.getBoot().setPropertiesProvider(new PropertiesProvider() {
			@Override
			public String getProperty(String key) {
				if (key.equals(CrnkProperties.RETURN_404_ON_NULL)) {
					return "true";
				}
				return null;
			}
		});
		container.boot();


		repository = new TestRepository();
		repository.setResourceRegistry(container.getResourceRegistry());
	}

	@Test
	public void checkThrowExceptionWhenResourceNotFound() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			repository.findOne(-1, new QuerySpec(Task.class));
		});
	}

	@Test
	public void saveNotSupported() {
		Assertions.assertThrows(MethodNotAllowedException.class, () -> {
			repository.save(null);
		});
	}

	@Test
	public void createNotSupported() {
		Assertions.assertThrows(MethodNotAllowedException.class, () -> {
			repository.create(null);
		});
	}

	@Test
	public void deleteNotSupported() {
		Assertions.assertThrows(MethodNotAllowedException.class, () -> {
			repository.delete(null);
		});
	}
}
