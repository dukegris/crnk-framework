package io.crnk.core.engine.internal.document.mapper.lookup.relationid;

import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.internal.document.mapper.AbstractDocumentMapperTest;
import io.crnk.core.mock.models.RelationIdTestResource;
import io.crnk.core.mock.models.Schedule;
import io.crnk.core.mock.repository.ScheduleRepositoryImpl;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.utils.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SerializeOnlyIdRelationIdLookupTest extends AbstractDocumentMapperTest {


    private ScheduleRepositoryImpl scheduleRepository;

    private Schedule schedule;

    @SuppressWarnings({"rawtypes", "unchecked"})
    @BeforeEach
    public void setup() {
        super.setup();

        scheduleRepository = (ScheduleRepositoryImpl) (ResourceRepository) container.getRepository(Schedule.class);
        schedule = new Schedule();
        schedule.setId(3L);
        schedule.setName("test");
        scheduleRepository.save(schedule);
    }

    @Test
    public void checkOnlyIdSet() {
        check(false, true);
    }

    @Test
    public void checkNull() {
        check(false, false);
    }

    @Test
    public void checkEntitySet() {
        check(true, true);
    }

    private void check(boolean setRelatedEntity, boolean setRelatedId) {
        RelationIdTestResource entity = new RelationIdTestResource();
        entity.setId(2L);
        entity.setName("test");
        if (setRelatedId) {
            entity.setTestSerializeOnlyIdId(3L);
        }
        if (setRelatedEntity) {
            entity.setTestSerializeOnlyId(schedule);
        }

        QuerySpec querySpec = new QuerySpec(RelationIdTestResource.class);

        Document document = mapper.toDocument(toResponse(entity), toAdapter(querySpec), mappingConfig).get();
        Resource resource = document.getSingleData().get();
        Assertions.assertEquals("2", resource.getId());
        Assertions.assertEquals("relationIdTest", resource.getType());
        Assertions.assertEquals("test", resource.getAttributes().get("name").asText());

        Nullable<ResourceIdentifier> data = resource.getRelationships().get("testSerializeOnlyId").getSingleData();
        Assertions.assertTrue(data.isPresent());
        Assertions.assertEquals(0, scheduleRepository.getNumFindAll());
        Assertions.assertEquals(0, document.getIncluded().size());
        if (setRelatedEntity || setRelatedId) {
            Assertions.assertNotNull(data.get());
        } else {
            Assertions.assertNull(data.get());
        }
    }

}
