package io.crnk.meta;


import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.utils.Supplier;
import io.crnk.meta.internal.MetaResourceRepositoryImpl;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MetaResourceRepositoryTest extends AbstractMetaTest {

	private MetaResourceRepositoryImpl<MetaElement> repo;

	private MetaLookupImpl lookup;

	@BeforeEach
	public void setup() {
		super.setup();

		ResourceMetaProvider provider = new ResourceMetaProvider();

		lookup = new MetaLookupImpl();
		lookup.setModuleContext(container.getModuleRegistry().getContext());
		lookup.addProvider(provider);
		lookup.initialize();

		repo = new MetaResourceRepositoryImpl(new Supplier<MetaLookup>() {
			@Override
			public MetaLookup get() {
				return lookup;
			}
		}, MetaElement.class);
		repo.setHttpRequestContextProvider(container.getModuleRegistry().getHttpRequestContextProvider());
	}

	@Test
	public void checkThrowsNotFoundException() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {		
			repo.findOne("does not exist", new QuerySpec(MetaElement.class));
		});
	}
}
