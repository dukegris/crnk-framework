package io.crnk.data.jpa;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.properties.NullPropertiesProvider;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.data.jpa.internal.JpaResourceInformationProvider;
import io.crnk.data.jpa.model.RelatedEntity;
import io.crnk.data.jpa.model.TestEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;

@Disabled
public class JpaPartialEntityExposureTest extends AbstractJpaJerseyTest {

    protected ResourceRepository<TestEntity, Serializable> testRepo;

    @Override
    @BeforeEach
    public void setup() {
        super.setup();
        testRepo = client.getRepositoryForType(TestEntity.class);
    }

    @Override
    protected void setupModule(JpaModuleConfig config, boolean server, EntityManager em) {
        super.setupModule(config, server, em);
        config.removeRepository(RelatedEntity.class);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testCrud() {
        TestEntity test = new TestEntity();
        test.setId(2L);
        test.setStringValue("test");
        testRepo.save(test);

        List<TestEntity> tests = testRepo.findAll(new QuerySpec(TestEntity.class));
        Assertions.assertEquals(1, tests.size());
        test = tests.get(0);
        Assertions.assertEquals(2L, test.getId().longValue());
        Assertions.assertNull(test.getOneRelatedValue());
        Assertions.assertNull(test.getEagerRelatedValue());
        Assertions.assertTrue(test.getManyRelatedValues().isEmpty());

        testRepo.delete(test.getId());
        tests = testRepo.findAll(new QuerySpec(TestEntity.class));
        Assertions.assertEquals(0, tests.size());
    }

    @Test
    public void testInformationBuilder() {
        EntityManager em = null;
        JpaResourceInformationProvider builder = new JpaResourceInformationProvider(new NullPropertiesProvider());
        ResourceInformation info = builder.build(TestEntity.class);
        List<ResourceField> relationshipFields = info.getRelationshipFields();
        Assertions.assertEquals(0, relationshipFields.size());
    }

}
