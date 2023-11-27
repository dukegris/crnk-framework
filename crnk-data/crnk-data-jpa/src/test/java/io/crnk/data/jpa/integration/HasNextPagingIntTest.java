package io.crnk.data.jpa.integration;

import io.crnk.core.resource.meta.JsonLinksInformation;
import io.crnk.core.resource.meta.JsonMetaInformation;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.data.jpa.AbstractJpaJerseyTest;
import io.crnk.data.jpa.JpaModuleConfig;
import io.crnk.data.jpa.model.RelatedEntity;
import io.crnk.data.jpa.model.TestEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.Arrays;

public class HasNextPagingIntTest extends AbstractJpaJerseyTest {

	private ResourceRepository<TestEntity, Long> testRepo;

	@Override
	@BeforeEach
	public void setup() {
		super.setup();
		testRepo = client.getRepositoryForType(TestEntity.class);
	}

	@Override
	protected void setupModule(JpaModuleConfig config, boolean server, EntityManager em) {
		super.setupModule(config, server, em);
		if (server) {
			config.setTotalResourceCountUsed(false);
		}
	}


	@Test
	public void testRootPaging() {
		for (long i = 0; i < 5; i++) {
			TestEntity task = new TestEntity();
			task.setId(i);
			task.setStringValue("test");
			testRepo.create(task);
		}

		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.setOffset(2L);
		querySpec.setLimit(2L);

		ResourceList<TestEntity> list = testRepo.findAll(querySpec);
		Assertions.assertEquals(2, list.size());
		Assertions.assertEquals(2, list.get(0).getId().intValue());
		Assertions.assertEquals(3, list.get(1).getId().intValue());

		JsonLinksInformation links = list.getLinks(JsonLinksInformation.class);

		String baseUri = getBaseUri().toString();
		Assertions.assertEquals(baseUri + "test?page[limit]=2", links.asJsonNode().get("first").asText());
		Assertions.assertNull(links.asJsonNode().get("last")); // not available for hasNext
		Assertions.assertEquals(baseUri + "test?page[limit]=2", links.asJsonNode().get("prev").asText());
		Assertions.assertEquals(baseUri + "test?page[limit]=2&page[offset]=4", links.asJsonNode().get("next").asText());

		JsonMetaInformation meta = list.getMeta(JsonMetaInformation.class);
		Assertions.assertNull(meta);
	}

	@Test
	public void testRelationPaging() {
		TestEntity test = new TestEntity();
		test.setId(1L);
		test.setStringValue("test");
		testRepo.create(test);

		ResourceRepository<RelatedEntity, Serializable> relatedRepo = client.getRepositoryForType(RelatedEntity.class);
		RelationshipRepository<TestEntity, Long, RelatedEntity, Long> relRepo = client
				.getRepositoryForType(TestEntity.class, RelatedEntity.class);

		for (long i = 0; i < 5; i++) {
			RelatedEntity related1 = new RelatedEntity();
			related1.setId(i);
			related1.setStringValue("related" + i);
			relatedRepo.create(related1);

			relRepo.addRelations(test, Arrays.asList(i), TestEntity.ATTR_manyRelatedValues);
		}

		QuerySpec querySpec = new QuerySpec(RelatedEntity.class);
		querySpec.setOffset(2L);
		querySpec.setLimit(2L);

		ResourceList<RelatedEntity> list = relRepo.findManyTargets(test.getId(), TestEntity.ATTR_manyRelatedValues, querySpec);
		Assertions.assertEquals(2, list.size());
		Assertions.assertEquals(2, list.get(0).getId().intValue());
		Assertions.assertEquals(3, list.get(1).getId().intValue());

		JsonMetaInformation meta = list.getMeta(JsonMetaInformation.class);
		JsonLinksInformation links = list.getLinks(JsonLinksInformation.class);
		Assertions.assertNull(meta);
		Assertions.assertNotNull(links);

		String baseUri = getBaseUri().toString();
		Assertions.assertEquals(baseUri + "test/1/manyRelatedValues?page[limit]=2",
				links.asJsonNode().get("first").asText());
		Assertions.assertNull(links.asJsonNode().get("last"));
		Assertions.assertEquals(baseUri + "test/1/manyRelatedValues?page[limit]=2",
				links.asJsonNode().get("prev").asText());
		Assertions.assertEquals(baseUri + "test/1/manyRelatedValues?page[limit]=2&page[offset]=4",
				links.asJsonNode().get("next").asText());
	}
}
