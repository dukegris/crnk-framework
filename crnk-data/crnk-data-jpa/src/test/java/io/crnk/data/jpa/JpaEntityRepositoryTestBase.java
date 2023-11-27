package io.crnk.data.jpa;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.SortSpec;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.PagedMetaInformation;
import io.crnk.data.jpa.internal.JpaRepositoryUtils;
import io.crnk.data.jpa.model.FieldOnlyEntity;
import io.crnk.data.jpa.model.JpaTransientTestEntity;
import io.crnk.data.jpa.model.RelatedEntity;
import io.crnk.data.jpa.model.RelationIdEntity;
import io.crnk.data.jpa.model.SequenceEntity;
import io.crnk.data.jpa.model.TestEntity;
import io.crnk.data.jpa.query.AbstractJpaTest;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract class JpaEntityRepositoryTestBase extends AbstractJpaTest {

	protected JpaEntityRepository<TestEntity, Long> repo;

	@Override
	@BeforeEach
	public void setup() {
		super.setup();

		JpaRepositoryConfig<TestEntity> config = JpaRepositoryConfig.create(TestEntity.class);
		JpaRepositoryUtils.setDefaultConfig(module.getConfig(), config);

		repo = new JpaEntityRepository<>(config);
		repo.setResourceRegistry(resourceRegistry);
	}

	@Test
	public void testGetResourceType() {
		Assertions.assertEquals(TestEntity.class, repo.getResourceClass());
	}

	@Test
	public void testGetEntityType() {
		Assertions.assertEquals(TestEntity.class, repo.getEntityClass());
	}

	@Test
	public void testFindAll() {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);

		List<TestEntity> list = repo.findAll(querySpec);
		Assertions.assertEquals(numTestEntities, list.size());
	}

	@Test
	public void testFindOne() {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);

		TestEntity entity = repo.findOne(1L, querySpec);
		Assertions.assertEquals("test1", entity.getStringValue());
	}

	@Test
	public void testFindAllById() {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);

		ResourceList<TestEntity> entities = repo.findAll(Arrays.asList(1L, 2L), querySpec);
		Assertions.assertEquals(2, entities.size());
		Assertions.assertEquals("test1", entities.get(0).getStringValue());
		Assertions.assertEquals("test2", entities.get(1).getStringValue());
	}

	@Test
	public void testInvalidLimit() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			QuerySpec querySpec = new QuerySpec(TestEntity.class);
			querySpec.setLimit(Long.MAX_VALUE);
			repo.findAll(querySpec);
		});
	}

	@Test
	public void testInvalidOffset() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			QuerySpec querySpec = new QuerySpec(TestEntity.class);
			querySpec.setOffset(Long.MAX_VALUE);
			repo.findAll(querySpec);
		});
	}

	@Test
	public void testFindAllOrderByAsc() {
		testFindAllOrder(true);
	}

	@Test
	public void testFindAllOrderByDesc() {
		testFindAllOrder(false);
	}

	public void testFindAllOrder(boolean asc) {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addSort(new SortSpec(Arrays.asList("longValue"), asc ? Direction.ASC : Direction.DESC));
		List<TestEntity> list = repo.findAll(querySpec);
		Assertions.assertEquals(numTestEntities, list.size());
		for (int i = 0; i < numTestEntities; i++) {
			if (asc) {
				Assertions.assertEquals(i, list.get(i).getLongValue());
			} else {
				Assertions.assertEquals(numTestEntities - 1 - i, list.get(i).getLongValue());
			}
		}
	}

	@Test
	public void testFilterString() {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("stringValue"), FilterOperator.EQ, "test1"));
		List<TestEntity> list = repo.findAll(querySpec);

		Assertions.assertEquals(1, list.size());
		TestEntity entity = list.get(0);
		Assertions.assertEquals("test1", entity.getStringValue());
	}

	@Test
	public void testFilterLong() {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("longValue"), FilterOperator.EQ, 2L));
		List<TestEntity> list = repo.findAll(querySpec);

		Assertions.assertEquals(1, list.size());
		TestEntity entity = list.get(0);
		Assertions.assertEquals(2, entity.getId().longValue());
		Assertions.assertEquals(2L, entity.getLongValue());
	}

	@Test
	public void testFilterInt() {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("embValue", "embIntValue"), FilterOperator.EQ, 2));
		List<TestEntity> list = repo.findAll(querySpec);

		Assertions.assertEquals(1, list.size());
		TestEntity entity = list.get(0);
		Assertions.assertEquals(2L, entity.getId().longValue());
		Assertions.assertEquals(2, entity.getEmbValue().getEmbIntValue().intValue());
	}

	@Test
	public void testFilterBooleanTrue() {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addFilter(
				new FilterSpec(Arrays.asList("embValue", "nestedValue", "embBoolValue"), FilterOperator.EQ, true));
		List<TestEntity> list = repo.findAll(querySpec);

		Assertions.assertEquals(1, list.size());
		TestEntity entity = list.get(0);
		Assertions.assertTrue(entity.getEmbValue().getNestedValue().getEmbBoolValue());
	}

	@Test
	public void testOptimization() {
		QuerySpec querySpec = new QuerySpec(RelationIdEntity.class);
		querySpec.addFilter(PathSpec.of("oneRelatedValue", "id").filter(FilterOperator.EQ, 1L));
		QuerySpec optimized = repo.optimizeQuerySpec(querySpec);

		Assertions.assertEquals(1, optimized.getFilters().size());
		FilterSpec filterSpec = optimized.getFilters().get(0);
		Assertions.assertEquals(PathSpec.of("oneRelatedValueId"), filterSpec.getPath());
	}

	@Test
	public void testJpaTransientFieldIgnored() {
		QuerySpec querySpec = new QuerySpec(JpaTransientTestEntity.class);

		JpaRepositoryConfig<JpaTransientTestEntity> config = JpaRepositoryConfig.create(JpaTransientTestEntity.class);
		JpaRepositoryUtils.setDefaultConfig(module.getConfig(), config);

		JpaEntityRepository<JpaTransientTestEntity, Serializable> transientRepo = new JpaEntityRepository<>(config);
		transientRepo.setResourceRegistry(resourceRegistry);

		JpaTransientTestEntity entity = new JpaTransientTestEntity();
		entity.setId(12L);
		transientRepo.create(entity);

		List<JpaTransientTestEntity> list = transientRepo.findAll(querySpec);
		Assertions.assertEquals(1, list.size());
		entity = list.get(0);
		Assertions.assertNotNull(entity);

		transientRepo.delete(entity.getId());
		list = transientRepo.findAll(querySpec);
		Assertions.assertEquals(0, list.size());
	}

	@Test
	public void testFilterBooleanFalse() {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addFilter(
				new FilterSpec(Arrays.asList("embValue", "nestedValue", "embBoolValue"), FilterOperator.EQ, false));
		List<TestEntity> list = repo.findAll(querySpec);

		Assertions.assertEquals(numTestEntities - 1, list.size());
		for (TestEntity entity : list) {
			Assertions.assertFalse(entity.getEmbValue().getNestedValue().getEmbBoolValue());
		}
	}

	@Test
	public void testFilterEquals() {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("longValue"), FilterOperator.EQ, 2L));
		List<TestEntity> list = repo.findAll(querySpec);

		Assertions.assertEquals(1, list.size());
	}

	@Test
	public void testFilterNotEquals() {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("longValue"), FilterOperator.NEQ, 2L));
		List<TestEntity> list = repo.findAll(querySpec);

		Assertions.assertEquals(4, list.size());
	}

	@Test
	public void testFilterLess() {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("longValue"), FilterOperator.LT, 2));
		List<TestEntity> list = repo.findAll(querySpec);

		Assertions.assertEquals(2, list.size());
	}

	@Test
	public void testFilterLessEqual() {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("longValue"), FilterOperator.LE, 2));
		List<TestEntity> list = repo.findAll(querySpec);

		Assertions.assertEquals(3, list.size());
	}

	@Test
	public void testFilterGreater() {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("longValue"), FilterOperator.GT, 1));
		List<TestEntity> list = repo.findAll(querySpec);

		Assertions.assertEquals(3, list.size());
	}

	@Test
	public void testFilterGreaterEqual() {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("longValue"), FilterOperator.GE, 1));
		List<TestEntity> list = repo.findAll(querySpec);

		Assertions.assertEquals(4, list.size());
	}

	@Test
	public void testFilterLike() {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("stringValue"), FilterOperator.LIKE, "test2"));
		List<TestEntity> list = repo.findAll(querySpec);

		Assertions.assertEquals(1, list.size());
	}

	@Test
	public void testFilterLikeWildcards() {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("stringValue"), FilterOperator.LIKE, "test%"));
		List<TestEntity> list = repo.findAll(querySpec);

		Assertions.assertEquals(5, list.size());
	}

	@Test
	public void testPaging() {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.setOffset(2L);
		querySpec.setLimit(2L);

		ResourceList<TestEntity> list = repo.findAll(querySpec);
		Assertions.assertEquals(2, list.size());
		Assertions.assertEquals(2, list.get(0).getId().intValue());
		Assertions.assertEquals(3, list.get(1).getId().intValue());

		PagedMetaInformation metaInformation = list.getMeta(PagedMetaInformation.class);
		Assertions.assertEquals(5, metaInformation.getTotalResourceCount().longValue());
	}

	@Test
	public void testPagingFirst() {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.setOffset(0L);
		querySpec.setLimit(3L);

		ResourceList<TestEntity> list = repo.findAll(querySpec);
		Assertions.assertEquals(3, list.size());
		Assertions.assertEquals(0, list.get(0).getId().intValue());
		Assertions.assertEquals(1, list.get(1).getId().intValue());
		Assertions.assertEquals(2, list.get(2).getId().intValue());

		PagedMetaInformation metaInformation = list.getMeta(PagedMetaInformation.class);
		Assertions.assertEquals(5, metaInformation.getTotalResourceCount().longValue());
	}

	@Test
	public void testPagingLast() {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.setOffset(4L);
		querySpec.setLimit(4L);

		ResourceList<TestEntity> list = repo.findAll(querySpec);
		Assertions.assertEquals(1, list.size());
		Assertions.assertEquals(4, list.get(0).getId().intValue());

		PagedMetaInformation metaInformation = list.getMeta(PagedMetaInformation.class);
		Assertions.assertEquals(5, metaInformation.getTotalResourceCount().longValue());
	}


	@Test
	public void testPagingLargeLimit() {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.setOffset(1L);
		querySpec.setLimit(10L);

		ResourceList<TestEntity> list = repo.findAll(querySpec);
		Assertions.assertEquals(4, list.size());

		PagedMetaInformation metaInformation = list.getMeta(PagedMetaInformation.class);
		Assertions.assertEquals(5, metaInformation.getTotalResourceCount().longValue());
	}


	@Test
	public void testOffserAndNoLimit() {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.setOffset(1L);
		querySpec.setLimit(null);

		ResourceList<TestEntity> list = repo.findAll(querySpec);
		Assertions.assertEquals(4, list.size());

		PagedMetaInformation metaInformation = list.getMeta(PagedMetaInformation.class);
		Assertions.assertEquals(5, metaInformation.getTotalResourceCount().longValue());
	}

	@Test
	public void testIncludeNoRelations() {
		em.clear();
		List<TestEntity> list = repo.findAll(new QuerySpec(TestEntity.class));
		Assertions.assertEquals(numTestEntities, list.size());
		for (TestEntity entity : list) {
			RelatedEntity relatedValue = entity.getOneRelatedValue();
			if (relatedValue != null) {
				Assertions.assertFalse(Hibernate.isInitialized(relatedValue));
			}
		}
	}

	@Test
	public void testFilterUnknownAttr() {
		Assertions.assertThrows(Exception.class, () -> {
			QuerySpec querySpec = new QuerySpec(TestEntity.class);
			querySpec.addFilter(new FilterSpec(Arrays.asList("test"), FilterOperator.EQ, "test"));
			repo.findAll(querySpec);
		});
	}

	@Test
	public void testSortUnknownAttr() {
		Assertions.assertThrows(Exception.class, () -> {
			QuerySpec querySpec = new QuerySpec(TestEntity.class);
			querySpec.addSort(new SortSpec(Arrays.asList("test"), Direction.DESC));
			repo.findAll(querySpec);
		});
	}

	@Test
	public void testSequencePrimaryKey() {
		JpaRepositoryConfig<SequenceEntity> config = JpaRepositoryConfig.create(SequenceEntity.class);
		JpaRepositoryUtils.setDefaultConfig(module.getConfig(), config);

		JpaEntityRepository<SequenceEntity, Long> sequenceRepo = new JpaEntityRepository<>(config);
		sequenceRepo.setResourceRegistry(resourceRegistry);
		QuerySpec querySpec = new QuerySpec(SequenceEntity.class);
		List<SequenceEntity> list = sequenceRepo.findAll(querySpec);
		Assertions.assertEquals(0, list.size());

		SequenceEntity entity = new SequenceEntity();
		entity.setStringValue("someValue");
		entity = sequenceRepo.create(entity);

		Assertions.assertNotNull(entity.getId());
		Assertions.assertNotEquals(0L, entity.getId().longValue());

		entity.setStringValue("someUpdatedValue");
		entity = sequenceRepo.save(entity);
		Assertions.assertEquals("someUpdatedValue", entity.getStringValue());
	}

	@Test
	@Disabled // currently not supported
	public void testFieldOnlyEntity() {
		QuerySpec querySpec = new QuerySpec(FieldOnlyEntity.class);
		JpaRepositoryConfig<FieldOnlyEntity> config = JpaRepositoryConfig.create(FieldOnlyEntity.class);
		JpaRepositoryUtils.setDefaultConfig(module.getConfig(), config);

		JpaEntityRepository<FieldOnlyEntity, Long> fieldRepo = new JpaEntityRepository<>(config);
		List<FieldOnlyEntity> list = fieldRepo.findAll(querySpec);
		Assertions.assertEquals(0, list.size());

		FieldOnlyEntity entity = new FieldOnlyEntity();
		entity.id = 13L;
		entity.longValue = 14L;
		fieldRepo.create(entity);

		FieldOnlyEntity savedEntity = fieldRepo.findOne(13L, querySpec);
		Assertions.assertNotNull(savedEntity);
		Assertions.assertEquals(14L, savedEntity.longValue);

		fieldRepo.delete(13L);
		list = fieldRepo.findAll(querySpec);
		Assertions.assertEquals(0, list.size());
	}
}
