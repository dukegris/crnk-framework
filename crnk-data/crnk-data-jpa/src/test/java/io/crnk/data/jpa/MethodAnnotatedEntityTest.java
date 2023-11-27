package io.crnk.data.jpa;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.data.jpa.meta.MetaEntity;
import io.crnk.data.jpa.model.MethodAnnotatedEntity;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class MethodAnnotatedEntityTest extends AbstractJpaJerseyTest {

	@Test
	public void testMeta() {
		MethodAnnotatedEntity entity = new MethodAnnotatedEntity();
		entity.setId(13L);
		entity.setStringValue("test");

		MetaEntity meta = jpaMetaProvider.getMeta(MethodAnnotatedEntity.class);
		MetaKey primaryKey = meta.getPrimaryKey();
		Assertions.assertNotNull(primaryKey);
		Assertions.assertEquals(1, primaryKey.getElements().size());

		MetaAttribute stringValueAttr = meta.getAttribute("stringValue");
		Assertions.assertNotNull(stringValueAttr);
		Assertions.assertEquals("stringValue", stringValueAttr.getName());
		Assertions.assertEquals("test", stringValueAttr.getValue(entity));

		MetaAttribute idAttr = meta.getAttribute("id");
		Assertions.assertNotNull(idAttr);
		Assertions.assertEquals("id", idAttr.getName());
		Assertions.assertEquals(13L, idAttr.getValue(entity));

	}

	@Test
	public void testMethodAnnotatedFields() {
		// tests whether JPA annotations on methods are supported as well
		ResourceRepository<MethodAnnotatedEntity, Long> methodRepo = client.getRepositoryForType(MethodAnnotatedEntity.class);

		MethodAnnotatedEntity task = new MethodAnnotatedEntity();
		task.setId(1L);
		task.setStringValue("test");
		methodRepo.create(task);

		// check retrievable with findAll
		List<MethodAnnotatedEntity> list = methodRepo.findAll(new QuerySpec(MethodAnnotatedEntity.class));
		Assertions.assertEquals(1, list.size());
		MethodAnnotatedEntity savedTask = list.get(0);
		Assertions.assertEquals(task.getId(), savedTask.getId());
		Assertions.assertEquals(task.getStringValue(), savedTask.getStringValue());

		// check retrievable with findAll(ids)
		list = methodRepo.findAll(Arrays.asList(1L), new QuerySpec(MethodAnnotatedEntity.class));
		Assertions.assertEquals(1, list.size());
		savedTask = list.get(0);
		Assertions.assertEquals(task.getId(), savedTask.getId());
		Assertions.assertEquals(task.getStringValue(), savedTask.getStringValue());

		// check retrievable with findOne
		savedTask = methodRepo.findOne(1L, new QuerySpec(MethodAnnotatedEntity.class));
		Assertions.assertEquals(task.getId(), savedTask.getId());
		Assertions.assertEquals(task.getStringValue(), savedTask.getStringValue());
	}
}
