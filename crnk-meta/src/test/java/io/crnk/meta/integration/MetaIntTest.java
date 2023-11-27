package io.crnk.meta.integration;

import io.crnk.client.internal.proxy.ObjectProxy;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaPrimitiveType;
import io.crnk.meta.model.MetaType;
import io.crnk.meta.model.resource.MetaResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class MetaIntTest extends AbstractMetaJerseyTest {

	private ResourceRepository<MetaElement, Serializable> repository;

	@BeforeEach
	@Override
	public void setUp() throws Exception {
		super.setUp();
		repository = client.getRepositoryForType(MetaElement.class);
	}

	@Test
	public void testFindAll() {
		ResourceList<MetaElement> list = repository.findAll(new QuerySpec(MetaElement.class));
		Assertions.assertFalse(list.isEmpty());
	}

	@Test
	public void testIdPrefix() {
		ResourceList<MetaElement> list = repository.findAll(new QuerySpec(MetaElement.class));
		Assertions.assertFalse(list.isEmpty());
		for (MetaElement elem : list) {
			if (elem instanceof MetaPrimitiveType) {
				Assertions.assertTrue(elem.getId().startsWith("base."), elem.getId());
			} else {
				Assertions.assertTrue(
						elem.getId().startsWith("base.") || elem.getId().startsWith("resources.") || elem.getId().startsWith("app.resources.") || elem.getId().startsWith("io.crnk"),
						elem.getId());
			}
		}
	}

	@Test
	public void testAttributesHaveParent() {
		QuerySpec querySpec = new QuerySpec(MetaAttribute.class);
		querySpec.includeRelation(Arrays.asList("parent"));
		ResourceList<MetaAttribute> list = client.getRepositoryForType(MetaAttribute.class).findAll(querySpec);
		Assertions.assertFalse(list.isEmpty());
		for (MetaAttribute elem : list) {
			Assertions.assertNotNull(elem.getParent());
		}
	}

	@Test
	public void testAttributesHaveType() {
		QuerySpec querySpec = new QuerySpec(MetaAttribute.class);
		querySpec.includeRelation(Arrays.asList("type", "elementType"));
		ResourceList<MetaAttribute> list = client.getRepositoryForType(MetaAttribute.class).findAll(querySpec);
		Assertions.assertFalse(list.isEmpty());
		for (MetaAttribute elem : list) {
			Assertions.assertNotNull(elem.getType());
			Assertions.assertNotNull(elem.getType().getElementType());
		}
	}

	@Test
	public void testFetchResourcesWithAttributesAndType() {
		QuerySpec querySpec = new QuerySpec(MetaResource.class);
		querySpec.includeRelation(Arrays.asList("attributes", "type"));
		ResourceList<MetaResource> list = client.getRepositoryForType(MetaResource.class).findAll(querySpec);
		Assertions.assertFalse(list.isEmpty());
		for (MetaResource elem : list) {
			List<? extends MetaAttribute> attributes = elem.getAttributes();
			Assertions.assertTrue(isLoaded(attributes));
			for (MetaAttribute attr : attributes) {
				Assertions.assertTrue(isLoaded(attr));
				MetaType attrType = attr.getType();
				Assertions.assertTrue(isLoaded(attrType));
			}
		}
	}

	@Test
	public void testFetchResourcesWithNestedAttributesAndTypes() {
		QuerySpec querySpec = new QuerySpec(MetaResource.class);

		QuerySpec dataObjectSpec = querySpec.getOrCreateQuerySpec(MetaDataObject.class);
		dataObjectSpec.includeRelation(Arrays.asList("attributes"));
		QuerySpec attrSpec = querySpec.getOrCreateQuerySpec(MetaAttribute.class);
		attrSpec.includeRelation(Arrays.asList("type"));
		QuerySpec typeSpec = querySpec.getOrCreateQuerySpec(MetaType.class);
		typeSpec.includeRelation(Arrays.asList("attributes", "type", "elementType", "attributes"));
		typeSpec.includeRelation(Arrays.asList("elementType"));
		typeSpec.includeRelation(Arrays.asList("superType"));

		ResourceList<MetaResource> list = client.getRepositoryForType(MetaResource.class).findAll(querySpec);
		Assertions.assertFalse(list.isEmpty());
		for (MetaResource elem : list) {
			checkDataObjectLoaded(elem, new HashSet<String>());
		}
	}

	private void checkDataObjectLoaded(MetaDataObject elem, HashSet<String> checked) {
		if (checked.contains(elem.getId())) {
			return;
		}
		checked.add(elem.getId());

		List<? extends MetaAttribute> attributes = elem.getAttributes();
		Assertions.assertTrue(isLoaded(attributes));
		for (MetaAttribute attr : attributes) {
			Assertions.assertTrue(isLoaded(attr));
			MetaType attrType = attr.getType();
			Assertions.assertTrue(isLoaded(attrType));
			MetaType attrElementType = attrType.getElementType();
			Assertions.assertTrue(isLoaded(attrElementType));
			if (attrElementType instanceof MetaDataObject) {
				checkDataObjectLoaded(attrElementType.asDataObject(), checked);
			}
		}

	}

	private boolean isLoaded(Object object) {
		if (object == null) {
			return false;
		}
		if (object instanceof MetaElement && ((MetaElement) object).getName() == null) {
			return false;
		}
		if (!(object instanceof ObjectProxy)) {
			return true;
		}
		ObjectProxy proxy = (ObjectProxy) object;
		return proxy.isLoaded();
	}

	@Test
	public void testGetResource() {
		testResource(false);
	}

	@Test
	public void testGetResourceAsMetaElement() {
		testResource(true);
	}

	public void testResource(boolean accessAsMetaElement) {
		Class<? extends MetaElement> elementClass = accessAsMetaElement ? MetaElement.class : MetaResource.class;
		QuerySpec querySpec = new QuerySpec(elementClass);
		querySpec.includeRelation(Arrays.asList("attributes", "type"));
		querySpec.includeRelation(Arrays.asList("declaredAttributes"));
		querySpec.includeRelation(Arrays.asList("primaryKey", "elements"));
		querySpec.includeRelation(Arrays.asList("superType"));
		String id = "resources.schedule";
		MetaResource resource = (MetaResource) client.getRepositoryForType(elementClass).findOne(id, querySpec);
		Assertions.assertNotNull(resource);
		Assertions.assertNotNull(resource.getAttributes());
		Assertions.assertNotNull(resource.getDeclaredAttributes());
		Assertions.assertNotNull(resource.getPrimaryKey());
		Assertions.assertNull(resource.getSuperType());
		Assertions.assertEquals(1, resource.getPrimaryKey().getElements().size());

		MetaAttribute idAttr = resource.getAttribute("id");
		Assertions.assertEquals("id", idAttr.getName());
		Assertions.assertNotNull(idAttr.getType());
		Assertions.assertTrue(idAttr.getType() instanceof MetaPrimitiveType);
		Assertions.assertFalse(idAttr.isAssociation());
	}

	public void testIdNaming() {
		QuerySpec querySpec = new QuerySpec(MetaElement.class);
		Assertions.assertNotNull(repository.findOne("io.crnk.meta.metaElement", querySpec));
		Assertions.assertNotNull(repository.findOne("io.crnk.meta.metaElement$List", querySpec));
		Assertions.assertNotNull(repository.findOne("io.crnk.meta.metaAttribute", querySpec));
		Assertions.assertNotNull(repository.findOne("io.crnk.meta.metaType", querySpec));
		Assertions.assertNotNull(repository.findOne("io.crnk.data.jpa.metaEmbeddableAttribute.laz", querySpec));
		Assertions.assertNotNull(repository.findOne("io.crnk.meta.metaType$primaryKey", querySpec));
	}
}
