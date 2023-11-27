package io.crnk.data.jpa.meta;

import io.crnk.data.jpa.model.TestEmbeddableBase;
import io.crnk.data.jpa.model.TestEmbeddable;
import io.crnk.meta.MetaLookupImpl;
import io.crnk.meta.model.MetaElement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public class MetaEmbeddableTest {

	private JpaMetaProvider metaProvider;

	@BeforeEach
	public void setup() {
		metaProvider = new JpaMetaProvider(Collections.emptySet());
		MetaLookupImpl lookup = new MetaLookupImpl();
		lookup.addProvider(metaProvider);
	}

	@Test
	public void testMetaEmbeddableBaseDiscovery() {
		MetaElement meta = metaProvider.discoverMeta(TestEmbeddableBase.class);
		Assertions.assertTrue(meta instanceof MetaEmbeddable);
	}

	@Test
	public void testMetaEmbeddableDiscovery() {
		MetaElement meta = metaProvider.discoverMeta(TestEmbeddable.class);
		Assertions.assertTrue(meta instanceof MetaEmbeddable);
	}

}
