package io.crnk.data.jpa.meta;

import io.crnk.data.jpa.model.TestEntity;
import io.crnk.meta.MetaLookupImpl;
import io.crnk.meta.model.MetaKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public class MetaKeyImplTest {

	@Test
	public void test() {
		JpaMetaProvider metaProvider = new JpaMetaProvider(Collections.emptySet());
		MetaLookupImpl lookup = new MetaLookupImpl();
		lookup.addProvider(metaProvider);
		MetaEntity meta = metaProvider.discoverMeta(TestEntity.class);
		MetaKey primaryKey = meta.getPrimaryKey();
		Assertions.assertTrue(primaryKey.isUnique());
		Assertions.assertEquals("TestEntity$primaryKey", primaryKey.getName());
		Assertions.assertEquals(1, primaryKey.getElements().size());
	}
}
