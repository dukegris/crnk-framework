package io.crnk.data.jpa.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.links.DefaultPagedLinksInformation;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.list.ResourceListBase;
import io.crnk.core.resource.meta.DefaultPagedMetaInformation;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.data.jpa.JpaRepositoryConfig;
import io.crnk.data.jpa.JpaRepositoryConfig.Builder;
import io.crnk.data.jpa.model.TestEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JpaRepositoryConfigTest {

	@Test
	public void testTypedList() {
		JpaRepositoryConfig<TestEntity> config = JpaRepositoryConfig.builder(TestEntity.class).setInterfaceClass(TestRepository.class)
				.build();
		Assertions.assertEquals(TestList.class, config.getListClass());
		TestList list = (TestList) config.newResultList();
		Assertions.assertTrue(list.getMeta() instanceof TestListMeta);
		Assertions.assertTrue(list.getLinks() instanceof TestListLinks);
	}

	@Test
	public void testDefaultList() {
		JpaRepositoryConfig<TestEntity> config = JpaRepositoryConfig.builder(TestEntity.class).build();
		Assertions.assertEquals(DefaultResourceList.class, config.getListClass());
		DefaultResourceList<TestEntity> list = config.newResultList();
		Assertions.assertTrue(list.getMeta() instanceof DefaultPagedMetaInformation);
		Assertions.assertTrue(list.getLinks() instanceof DefaultPagedLinksInformation);
	}

	@Test
	public void testFindAllNotOverriden() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
		Builder<TestEntity> builder = JpaRepositoryConfig.builder(TestEntity.class);
		builder.setInterfaceClass(IncompleteTestRepository.class);
		});
	}

	@Test
	public void testFindAllInvalidReturnType() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
		Builder<TestEntity> builder = JpaRepositoryConfig.builder(TestEntity.class);
		builder.setInterfaceClass(InvalidReturnTypeTestRepository.class);
		});
	}

	public interface TestRepository extends ResourceRepository<TestEntity, Long> {

		@Override
		TestList findAll(QuerySpec querySpec);

	}

	public interface IncompleteTestRepository extends ResourceRepository<TestEntity, Long> {

	}

	public interface InvalidReturnTypeTestRepository extends ResourceRepository<TestEntity, Long> {

		@Override
		ResourceList<TestEntity> findAll(QuerySpec querySpec);

	}

	public static class TestList extends ResourceListBase<TestEntity, TestListMeta, TestListLinks> {

	}

	public static class TestListLinks extends DefaultPagedLinksInformation implements LinksInformation {

		public String name = "value";
	}

	public static class TestListMeta implements MetaInformation {

		public String name = "value";

	}
}
