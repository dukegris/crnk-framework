package io.crnk.data.jpa.query;

import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.data.jpa.meta.MetaEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
public abstract class AbstractInheritanceTest<B, C> extends AbstractJpaTest {

	private Class<B> baseClass;

	private Class<C> childClass;

	protected AbstractInheritanceTest(Class<B> baseClass, Class<C> childClass) {
		this.baseClass = baseClass;
		this.childClass = childClass;
	}

	private JpaQuery<B> baseBuilder() {
		return queryFactory.query(baseClass);
	}

	@Test
	public void testMeta() {
		MetaEntity baseMeta = module.getJpaMetaProvider().getMeta(baseClass);
		MetaEntity childMeta = module.getJpaMetaProvider().getMeta(childClass);
		Assertions.assertSame(baseMeta, childMeta.getSuperType());

		Assertions.assertEquals(1, childMeta.getDeclaredAttributes().size());
		Assertions.assertEquals(2, baseMeta.getAttributes().size());
		Assertions.assertEquals(3, childMeta.getAttributes().size());

		Assertions.assertNotNull(baseMeta.getAttribute("id"));
		Assertions.assertNotNull(baseMeta.getAttribute("stringValue"));
		try {
			Assertions.assertNull(baseMeta.getAttribute("intValue"));
			Assertions.fail();
		} catch (Exception e) {
			// ok
		}
		Assertions.assertNotNull(childMeta.getAttribute("id"));
		Assertions.assertNotNull(childMeta.getAttribute("stringValue"));
		Assertions.assertNotNull(childMeta.getAttribute("intValue"));
	}

	@Test
	public void testAll() {
		assertEquals(10, baseBuilder().buildExecutor().getResultList().size());
	}

	@Test
	public void testFilterBySubtypeAttribute() {
		// FIXME subtype lookup
		Assertions.assertTrue(module.getJpaMetaProvider().getMeta(childClass) instanceof MetaEntity);

		assertEquals(1, baseBuilder().addFilter("intValue", FilterOperator.EQ, 2).buildExecutor().getResultList().size());
		assertEquals(3, baseBuilder().addFilter("intValue", FilterOperator.GT, 1).buildExecutor().getResultList().size());
	}

	@Test
	public void testOrderBySubtypeAttribute() {
		// FIXME subtype lookup
		Assertions.assertTrue(module.getJpaMetaProvider().getMeta(childClass) instanceof MetaEntity);

		List<B> list = baseBuilder().addSortBy(Arrays.asList("intValue"), Direction.DESC).buildExecutor().getResultList();
		Assertions.assertEquals(10, list.size());
		for (int i = 0; i < 10; i++) {
			B entity = list.get(i);
			MetaEntity meta = module.getJpaMetaProvider().getMeta(entity.getClass());

			if (i < 5) {
				Assertions.assertTrue(childClass.isInstance(entity));
				Assertions.assertEquals(4 - i, meta.getAttribute("intValue").getValue(entity));
			} else {
				Assertions.assertFalse(childClass.isInstance(entity));

				// order by primary key by default second order criteria
				Assertions.assertEquals(Long.valueOf(i - 5), meta.getAttribute("id").getValue(entity));
			}
		}
	}

}
