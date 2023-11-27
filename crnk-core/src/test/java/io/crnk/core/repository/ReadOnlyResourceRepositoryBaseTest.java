package io.crnk.core.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.crnk.core.exception.MethodNotAllowedException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;

public class ReadOnlyResourceRepositoryBaseTest {

	private ReadOnlyResourceRepositoryBase repo = new ReadOnlyResourceRepositoryBase(null) {
		@Override
		public ResourceList findAll(QuerySpec querySpec) {
			return null;
		}
	};

	@Test
	public void save() {
		Assertions.assertThrows(MethodNotAllowedException.class, () -> {
			repo.save(null);
		});
	}

	@Test
	public void create() {
		Assertions.assertThrows(MethodNotAllowedException.class, () -> {
			repo.create(null);
		});
	}

	@Test
	public void delete() {
		Assertions.assertThrows(MethodNotAllowedException.class, () -> {
			repo.delete(null);
		});
	}
}
