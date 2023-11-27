package io.crnk.meta;


import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.utils.Supplier;
import io.crnk.meta.internal.MetaRelationshipRepositoryImpl;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaKey;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.test.mock.models.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MetaRelationshipRepositoryImplTest extends AbstractMetaTest {

	private MetaRelationshipRepositoryImpl repo;

	private MetaLookupImpl lookup;

	@BeforeEach
	public void setup() {
		super.setup();

		resourceProvider = new ResourceMetaProvider();

		lookup = new MetaLookupImpl();
		lookup.setModuleContext(container.getModuleRegistry().getContext());
		lookup.addProvider(resourceProvider);
		lookup.initialize();

		repo = new MetaRelationshipRepositoryImpl(new Supplier<MetaLookup>() {
			@Override
			public MetaLookup get() {
				return lookup;
			}
		}, MetaElement.class, MetaElement.class);
		repo.setHttpRequestContextProvider(container.getModuleRegistry().getHttpRequestContextProvider());
	}

	@Test
	public void checkReadOnly1() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {		
		repo.setRelation(null, null, null);
		});
	}

	@Test
	public void checkReadOnly2() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {		
		repo.setRelations(null, null, null);
		});
	}


	@Test
	public void checkReadOnly3() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {		
		repo.addRelations(null, null, null);
		});
	}


	@Test
	public void checkReadOnly4() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {		
		repo.removeRelations(null, null, null);
		});
	}


	@Test
	public void findOneTargetReturnsResult() {
		MetaResource resource = resourceProvider.getMeta(Task.class);

		MetaKey key = (MetaKey) repo.findOneTarget(resource.getId(), "primaryKey", new QuerySpec(MetaElement.class));
		Assertions.assertNotNull(key);
		Assertions.assertEquals("id", key.getUniqueElement().getName());
	}

	@Test
	public void findOneTargetReturnsNull() {
		MetaResource resource = resourceProvider.getMeta(Task.class);
		resource.setPrimaryKey(null);

		MetaKey key = (MetaKey) repo.findOneTarget(resource.getId(), "primaryKey", new QuerySpec(MetaElement.class));
		Assertions.assertNull(key);
	}

	@Test
	public void findOneTargetReturnsExceptionWhenSourceNotFound() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {		
		repo.findOneTarget("does not exist", "primaryKey", new QuerySpec(MetaElement.class));
		});
	}

	@Test
	public void findManyTargetReturnsResult() {
		MetaResource resource = resourceProvider.getMeta(Task.class);

		ResourceList<MetaElement> children = repo.findManyTargets(resource.getId(), "children", new QuerySpec(MetaElement
				.class));
		Assertions.assertNotEquals(0, children.size());
	}


	@Test
	public void findManyTargetCannotBeUsedForSingeValuesRelations() {
		Assertions.assertThrows(ClassCastException.class, () -> {		
		MetaResource resource = resourceProvider.getMeta(Task.class);
			repo.findManyTargets(resource.getId(), "primaryKey", new QuerySpec(MetaElement.class));
		});
	}


	@Test
	public void findManyTargetReturnsExceptionWhenSourceNotFound() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {		
		repo.findManyTargets("does not exist", "children", new QuerySpec(MetaElement.class));
		});
	}

}
