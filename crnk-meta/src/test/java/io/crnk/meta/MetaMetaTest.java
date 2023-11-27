package io.crnk.meta;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaType;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.test.mock.TestModule;
import io.crnk.test.mock.models.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MetaMetaTest {

	private MetaLookup lookup;

	private ResourceMetaProvider resourceProvider;

	@BeforeEach
	public void setup() {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://localhost"));
		boot.addModule(new TestModule());

		resourceProvider = new ResourceMetaProvider();

		MetaModuleConfig moduleConfig = new MetaModuleConfig();
		moduleConfig.addMetaProvider(resourceProvider);
		MetaModule module = MetaModule.createServerModule(moduleConfig);
		boot.addModule(module);
		boot.boot();

		lookup = module.getLookup();
	}

	@Test
	public void testAttributesProperlyDeclaredAndNotInherited() {
		MetaResource elementMeta = resourceProvider.getMeta(MetaElement.class);
		MetaResource dataMeta = resourceProvider.getMeta(MetaDataObject.class);

		Assertions.assertSame(elementMeta.getAttribute("id"), dataMeta.getAttribute("id"));
		Assertions.assertSame(elementMeta.getPrimaryKey(), dataMeta.getPrimaryKey());
	}

	@Test
	public void testMetaElementImmutable() {
		MetaResource dataMeta = resourceProvider.getMeta(MetaDataObject.class);
		Assertions.assertFalse(dataMeta.isUpdatable());
		Assertions.assertFalse(dataMeta.isInsertable());
		Assertions.assertFalse(dataMeta.isDeletable());
		Assertions.assertNotEquals(0, dataMeta.getAttributes().size());
		for (MetaAttribute attr : dataMeta.getAttributes()) {
			Assertions.assertFalse(attr.isUpdatable());
			Assertions.assertFalse(attr.isInsertable());
		}
	}

	@Test
	public void testLinksNaming() {
		MetaResource taskMeta = resourceProvider.getMeta(Task.class);
		MetaAttribute linksInformation = taskMeta.getAttribute("linksInformation");
		MetaType type = linksInformation.getType();
		Assertions.assertEquals(type.getId(), "resources.tasks$links");
		Assertions.assertEquals(type.getName(), "TaskLinks");
	}


	@Test
	public void testMetaNaming() {
		MetaResource taskMeta = resourceProvider.getMeta(Task.class);
		MetaAttribute metaInformation = taskMeta.getAttribute("metaInformation");
		MetaType type = metaInformation.getType();
		Assertions.assertEquals(type.getId(), "resources.tasks$meta");
		Assertions.assertEquals(type.getName(), "TaskMeta");
	}

	@Test
	public void testNonMetaElementMutable() {
		MetaResource dataMeta = resourceProvider.getMeta(Task.class);
		Assertions.assertTrue(dataMeta.isUpdatable());
		Assertions.assertTrue(dataMeta.isInsertable());
		Assertions.assertTrue(dataMeta.isDeletable());
		Assertions.assertNotEquals(0, dataMeta.getAttributes().size());
		for (MetaAttribute attr : dataMeta.getAttributes()) {
			Assertions.assertTrue(attr.isInsertable());
		}
	}

	@Test
	public void testMetaDataObjectMeta() {
		MetaResource meta = resourceProvider.getMeta(MetaDataObject.class);

		MetaAttribute elementTypeAttr = meta.getAttribute("elementType");
		Assertions.assertNotNull(elementTypeAttr);
		Assertions.assertNotNull(elementTypeAttr.getType());
		Assertions.assertEquals("resources.metaType.elementType", elementTypeAttr.getId());

		MetaAttribute attrsAttr = meta.getAttribute("attributes");
		Assertions.assertNotNull(attrsAttr.getType());

		MetaAttribute childrenAttr = meta.getAttribute("children");
		Assertions.assertEquals("resources.metaElement.children", childrenAttr.getId());
		Assertions.assertEquals("resources.metaElement$list", childrenAttr.getType().getId());
	}
}
