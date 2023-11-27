package io.crnk.reactive;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.reactive.model.SlowTask;
import io.crnk.reactive.repository.ReactiveResourceRepositoryBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

public class ReactiveRepositoryBaseTest {

	private Map<Long, SlowTask> tasks = new HashMap<>();

	private ReactiveResourceRepositoryBase base = new ReactiveResourceRepositoryBase(SlowTask.class) {
		@Override
		public Mono<ResourceList> findAll(QuerySpec querySpec) {
			return Mono.just(querySpec.apply(tasks.values()));
		}
	};
	private SlowTask task;

	@BeforeEach
	public void setup() {
		tasks.clear();

		SimpleModule baseModule = new SimpleModule("test");
		baseModule.addRepository(base);

		CrnkBoot boot = new CrnkBoot();
		boot.addModule(baseModule);
		boot.addModule(new ReactiveModule());
		boot.boot();
		base.setResourceRegistry(boot.getResourceRegistry());

		task = new SlowTask();
		task.setId(1L);
		task.setName("test");
		tasks.put(task.getId(), task);
	}

	@Test
	public void checkFindAll() {
		QuerySpec querySpec = new QuerySpec(SlowTask.class);
		Mono<ResourceList> mono = base.findAll(querySpec);
		Assertions.assertEquals(1, mono.block().size());
	}

	@Test
	public void checkFindExistingOne() {
		QuerySpec querySpec = new QuerySpec(SlowTask.class);
		Assertions.assertSame(task, base.findOne(1L, querySpec).block());
	}

	@Test
	public void checkFindNonExistingOne() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			QuerySpec querySpec = new QuerySpec(SlowTask.class);
			base.findOne(2L, querySpec).block();
		});
	}

	@Test
	public void cannotCreateByDefault() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
			base.create(null).block();
		});
	}

	@Test
	public void cannotUpdateByDefault() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
			base.save(null).block();
		});
	}

	@Test
	public void cannotDeleteByDefault() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
			base.delete(null).block();
		});
	}
}
