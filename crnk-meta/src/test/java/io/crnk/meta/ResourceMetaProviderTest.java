package io.crnk.meta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.meta.model.MetaArrayType;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaAttributePath;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaEnumType;
import io.crnk.meta.model.MetaKey;
import io.crnk.meta.model.MetaListType;
import io.crnk.meta.model.MetaMapType;
import io.crnk.meta.model.MetaPrimitiveType;
import io.crnk.meta.model.MetaSetType;
import io.crnk.meta.model.MetaType;
import io.crnk.meta.model.resource.MetaJsonObject;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceAction;
import io.crnk.meta.model.resource.MetaResourceAction.MetaRepositoryActionType;
import io.crnk.meta.model.resource.MetaResourceField;
import io.crnk.meta.model.resource.MetaResourceRepository;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.test.mock.models.NoAccessTask;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.models.TaskStatus;
import io.crnk.test.mock.models.TaskSubType;
import io.crnk.test.mock.models.VersionedTask;
import io.crnk.test.mock.models.types.ProjectData;
import io.crnk.test.mock.repository.ScheduleRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ResourceMetaProviderTest extends AbstractMetaTest {

	private MetaLookupImpl lookup;

	private ResourceMetaProvider resourceProvider;

	@BeforeEach
	public void setup() {
		super.setup();

		resourceProvider = new ResourceMetaProvider();

		lookup = new MetaLookupImpl();
		lookup.setModuleContext(container.getModuleRegistry().getContext());
		lookup.addProvider(resourceProvider);
		lookup.initialize();
	}

	@Test
	public void testReadWriteMethods() {
		MetaResource meta = resourceProvider.getMeta(Schedule.class);

		MetaAttribute idAttr = meta.getAttribute("id");
		Assertions.assertEquals("getId", idAttr.getReadMethod().getName());
		Assertions.assertEquals("setId", idAttr.getWriteMethod().getName());

		Schedule schedule = new Schedule();
		schedule.setTasks(new HashSet<>());
		schedule.setId(13L);
		Assertions.assertEquals(13L, idAttr.getValue(schedule));

		idAttr.setValue(schedule, 14L);
		Assertions.assertEquals(14L, schedule.getId().longValue());

		// TODO meta ttribute & renaming
		/*
		MetaAttribute listAttr = meta.getAttribute("taskSet");
		Task task = new Task();
		task.setId(12L);
		listAttr.addValue(schedule, task);
		Assertions.assertEquals(1, schedule.getTasks().size());
		listAttr.removeValue(schedule, task);
		Assertions.assertEquals(0, schedule.getTasks().size());
		 */
	}

	@Test
	public void getVersionAttribute() {
		// TODO setup versioning concept in json api layer
		MetaResource meta = resourceProvider.getMeta(Schedule.class);
		Assertions.assertNull(meta.getVersionAttribute());
	}


	@Test
	public void checkUseOfJsonNaming() {
		MetaResource meta = resourceProvider.getMeta(Schedule.class);
		// this is the json name, not java (desc)!
		MetaAttribute attr = meta.getAttribute("description");
		Assertions.assertNotNull(attr);
	}

	@Test
	public void resolvePath() {
		MetaResource meta = resourceProvider.getMeta(Schedule.class);

		MetaAttributePath metaAttributes = meta.resolvePath(Arrays.asList("project", "name"));
		Assertions.assertEquals(2, metaAttributes.length());
		Assertions.assertEquals("project", metaAttributes.getElement(0).getName());
		Assertions.assertEquals("name", metaAttributes.getElement(1).getName());
	}

	@Test
	public void resolvePathWithSubType() {
		MetaResource meta = resourceProvider.getMeta(Task.class);

		MetaAttributePath metaAttributes = meta.resolvePath(Arrays.asList("subTypeValue"), true);
		Assertions.assertEquals(1, metaAttributes.length());
		Assertions.assertEquals("subTypeValue", metaAttributes.getElement(0).getName());
	}


	@Test
	public void getSubTypes() {
		MetaResource meta = resourceProvider.getMeta(Task.class);

		List<MetaDataObject> subTypes = meta.getSubTypes(true, false);
		Assertions.assertEquals(1, subTypes.size());
		MetaDataObject subType = subTypes.iterator().next();
		Assertions.assertEquals(TaskSubType.class, subType.getImplementationClass());
	}


	@Test
	public void getSubTypesOrSelf() {
		MetaResource meta = resourceProvider.getMeta(Task.class);

		List<MetaDataObject> subTypes = meta.getSubTypes(true, true);
		Assertions.assertEquals(2, subTypes.size());
		Assertions.assertTrue(subTypes.contains(meta));
	}

	@Test
	public void testGetAnnotation() {
		MetaResource meta = resourceProvider.getMeta(Schedule.class);
		MetaAttribute idAttr = meta.getAttribute("id");
		Assertions.assertNotNull(idAttr.getAnnotation(JsonApiId.class));
	}

	@Test
	public void testGetAnnotations() {
		MetaResource meta = resourceProvider.getMeta(Schedule.class);
		MetaAttribute idAttr = meta.getAttribute("id");
		Assertions.assertEquals(1, idAttr.getAnnotations().size());
	}

	@Test
	public void testPreserveAttributeOrder() {
		ResourceInformation resourceInformation = container.getEntry(Schedule.class).getResourceInformation();
		List<ResourceField> fields = resourceInformation.getFields();
		Assertions.assertEquals("id", fields.get(0).getUnderlyingName());
		Assertions.assertEquals("name", fields.get(1).getUnderlyingName());
		Assertions.assertEquals("desc", fields.get(2).getUnderlyingName());
		Assertions.assertEquals("tasks", fields.get(3).getUnderlyingName());
		Assertions.assertEquals("project", fields.get(4).getUnderlyingName());
		Assertions.assertEquals("projects", fields.get(5).getUnderlyingName());
		Assertions.assertEquals("status", fields.get(6).getUnderlyingName());
		Assertions.assertEquals("delayed", fields.get(7).getUnderlyingName());
		Assertions.assertEquals("customData", fields.get(8).getUnderlyingName());

		MetaResource meta = resourceProvider.getMeta(Schedule.class);
		List<? extends MetaAttribute> attributes = meta.getAttributes();
		Assertions.assertEquals("id", attributes.get(0).getName());
		Assertions.assertEquals("name", attributes.get(1).getName());
		Assertions.assertEquals("description", attributes.get(2).getName());
		Assertions.assertEquals("taskSet", attributes.get(3).getName());
		Assertions.assertEquals("project", attributes.get(4).getName());
		Assertions.assertEquals("projects", attributes.get(5).getName());
		Assertions.assertEquals("status", attributes.get(6).getName());
		Assertions.assertEquals("delayed", attributes.get(7).getName());
		Assertions.assertEquals("customData", attributes.get(8).getName());
	}

	@Test
	public void testUnderlyingNameForNestedObject() {
		MetaDataObject meta = resourceProvider.getMeta(ProjectData.class);
		MetaAttribute attribute = meta.getAttribute("due");
		Assertions.assertEquals("due", attribute.getName());
		Assertions.assertEquals("dueDate", attribute.getUnderlyingName());
	}

	@Test
	public void testUnderlyingNameForResource() {
		MetaResource meta = resourceProvider.getMeta(Schedule.class);
		MetaAttribute attribute = meta.getAttribute("taskSet");
		Assertions.assertEquals("taskSet", attribute.getName());
		Assertions.assertEquals("tasks", attribute.getUnderlyingName());
	}

	@Test
	public void testPrimaryKeyNotNullable() {
		MetaResource meta = resourceProvider.getMeta(Schedule.class);
		MetaKey primaryKey = meta.getPrimaryKey();
		MetaAttribute idField = primaryKey.getElements().get(0);
		Assertions.assertFalse(idField.isNullable());

		Assertions.assertNotNull(idField.getAnnotation(JsonApiId.class));
		Assertions.assertEquals(1, idField.getAnnotations().size());
	}

	@Test
	public void testRegularFieldNullable() {
		MetaResource meta = resourceProvider.getMeta(Schedule.class);
		MetaAttribute taskField = meta.getAttribute("project");
		Assertions.assertTrue(taskField.isNullable());
	}

	@Test
	public void testPrimitiveFieldNotNullable() {
		MetaResource meta = resourceProvider.getMeta(Schedule.class);
		MetaAttribute taskField = meta.getAttribute("delayed");
		Assertions.assertFalse(taskField.isNullable());
	}

	@Test
	public void testInheritanceOnResource() {
		MetaResource meta = resourceProvider.getMeta(TaskSubType.class);

		Assertions.assertNotNull(meta.getAttribute("subTypeValue"));
		Assertions.assertNotNull(meta.getAttribute("name"));
		Assertions.assertNotNull(meta.getAttribute("id"));

		MetaDataObject superType = meta.getSuperType();
		Assertions.assertEquals(MetaResource.class, superType.getClass());
		Assertions.assertFalse(superType.hasAttribute("subTypeValue"));
		Assertions.assertNotNull(superType.getAttribute("name"));
		Assertions.assertNotNull(superType.getAttribute("id"));

		MetaKey primaryKey = meta.getPrimaryKey();
		Assertions.assertNotNull("id", primaryKey.getName());
		Assertions.assertEquals(1, primaryKey.getElements().size());
		Assertions.assertEquals("id", primaryKey.getElements().get(0).getName());
		Assertions.assertSame(primaryKey.getElements().get(0), meta.getAttribute("id"));
		Assertions.assertTrue(meta.getPrimaryKey().isUnique());

		Assertions.assertEquals(primaryKey.getElements().get(0), primaryKey.getUniqueElement());

		Assertions.assertEquals("12", primaryKey.toKeyString(12));
	}

	@Test
	public void testAccessInheritanceOnResource() {
		MetaResource meta = resourceProvider.getMeta(NoAccessTask.class);

		Assertions.assertFalse(meta.isReadable());
		Assertions.assertFalse(meta.isInsertable());
		Assertions.assertFalse(meta.isUpdatable());
		Assertions.assertFalse(meta.isDeletable());
	}

	@Test
	public void testResourceProperties() {
		MetaResource meta = resourceProvider.getMeta(Schedule.class);

		Assertions.assertEquals("schedule", meta.getResourceType());
		Assertions.assertEquals("Schedule", meta.getName());
		Assertions.assertEquals("resources.schedule", meta.getId());

		Assertions.assertEquals(Schedule.class, meta.getImplementationClass());
		Assertions.assertEquals(Schedule.class, meta.getImplementationType());
		Assertions.assertNull(meta.getParent());
		Assertions.assertTrue(meta.getSubTypes().isEmpty());
	}

	@Test
	public void testRenamedOppositeRelationship() {
		MetaResource meta = resourceProvider.getMeta(Schedule.class);
		MetaAttribute taskSetAttr = meta.getAttribute("taskSet");
		Assertions.assertEquals("taskSet", taskSetAttr.getName());
		MetaAttribute oppositeAttribute = taskSetAttr.getOppositeAttribute();
		Assertions.assertNotNull(oppositeAttribute);
		Assertions.assertEquals("schedule", oppositeAttribute.getName());
		Assertions.assertSame(taskSetAttr, oppositeAttribute.getOppositeAttribute());
	}

	@Test
	public void testLinksAttribute() {
		MetaResource meta = resourceProvider.getMeta(Task.class);

		MetaResourceField attr = (MetaResourceField) meta.getAttribute("linksInformation");
		Assertions.assertEquals("linksInformation", attr.getName());
		Assertions.assertEquals("resources.tasks.linksInformation", attr.getId());
		Assertions.assertFalse(attr.isLazy());
		Assertions.assertTrue(attr.getFieldType() == ResourceFieldType.LINKS_INFORMATION);
		Assertions.assertNull(attr.getOppositeAttribute());
		Assertions.assertEquals(Task.TaskLinks.class, attr.getType().getImplementationClass());
	}

	@Test
	public void testMetaAttribute() {
		MetaResource meta = resourceProvider.getMeta(Task.class);

		MetaResourceField attr = (MetaResourceField) meta.getAttribute("metaInformation");
		Assertions.assertEquals("metaInformation", attr.getName());
		Assertions.assertEquals("resources.tasks.metaInformation", attr.getId());
		Assertions.assertFalse(attr.isLazy());
		Assertions.assertTrue(attr.getFieldType() == ResourceFieldType.META_INFORMATION);
		Assertions.assertNull(attr.getOppositeAttribute());
		Assertions.assertEquals(Task.TaskMeta.class, attr.getType().getImplementationClass());
	}

	@Test
	public void testNestedObject() {
		MetaResource meta = resourceProvider.getMeta(Project.class);
		MetaAttribute data = meta.getAttribute("data");
		MetaType type = data.getType();
		Assertions.assertEquals("resources.types.projectdata", type.getId());
	}


	@Test
	public void testNestedAttributeRenaming() {
		MetaResource meta = resourceProvider.getMeta(Project.class);
		MetaAttribute data = meta.getAttribute("data");
		MetaJsonObject type = (MetaJsonObject) data.getType();
		Assertions.assertEquals("resources.types.projectdata", type.getId());

		Assertions.assertFalse(type.hasAttribute("dueDate"));
		Assertions.assertNotNull(type.getAttribute("due"));

	}


	@Test
	public void testSingleValuedAttribute() {
		MetaResource meta = resourceProvider.getMeta(Task.class);

		MetaResourceField attr = (MetaResourceField) meta.getAttribute("name");
		Assertions.assertEquals("name", attr.getName());
		Assertions.assertFalse(attr.isLazy());
		Assertions.assertFalse(attr.getFieldType() == ResourceFieldType.META_INFORMATION);
		Assertions.assertFalse(attr.getFieldType() == ResourceFieldType.LINKS_INFORMATION);
		Assertions.assertFalse(attr.isAssociation());

		Assertions.assertTrue(attr.isSortable());
		Assertions.assertTrue(attr.isFilterable());
		Assertions.assertTrue(attr.isInsertable());
		Assertions.assertTrue(attr.isUpdatable());
	}

	@Test
	public void testArrayAttribute() {
		MetaResource meta = resourceProvider.getMeta(Project.class);

		MetaResourceField dataField = (MetaResourceField) meta.getAttribute("data");
		Assertions.assertEquals("data", dataField.getName());

		MetaJsonObject projectData = (MetaJsonObject) dataField.getType();

		MetaAttribute keywordField = projectData.getAttribute("keywords");

		Assertions.assertFalse(keywordField.isLazy());
		Assertions.assertFalse(keywordField.isAssociation());
		Assertions.assertTrue(keywordField.isInsertable());
		Assertions.assertTrue(keywordField.isUpdatable());

		Assertions.assertEquals(MetaArrayType.class, keywordField.getType().getClass());
		Assertions.assertTrue(keywordField.getType().getElementType() instanceof MetaPrimitiveType);

		Assertions.assertEquals("string$array", keywordField.getType().getName());
		Assertions.assertEquals("base.string$array", keywordField.getType().getId());

		// FIXME support crnk annotations
		// Assertions.assertTrue(dataField.isSortable());
		// Assertions.assertTrue(dataField.isFilterable());
	}

	@Test
	public void testMapAttribute() {
		MetaResource meta = resourceProvider.getMeta(Project.class);

		MetaResourceField dataField = (MetaResourceField) meta.getAttribute("data");
		Assertions.assertEquals("data", dataField.getName());

		MetaJsonObject projectData = (MetaJsonObject) dataField.getType();

		MetaAttribute keywordField = projectData.getAttribute("customData");

		Assertions.assertFalse(keywordField.isLazy());
		Assertions.assertFalse(keywordField.isAssociation());
		Assertions.assertTrue(keywordField.isInsertable());
		Assertions.assertTrue(keywordField.isUpdatable());

		Assertions.assertTrue(keywordField.getType() instanceof MetaMapType);
		MetaMapType mapType = (MetaMapType) keywordField.getType();
		Assertions.assertTrue(keywordField.getType().getElementType() instanceof MetaPrimitiveType);
		Assertions.assertTrue(mapType.getKeyType() instanceof MetaPrimitiveType);

		Assertions.assertNotNull(mapType.asMap());
		Assertions.assertTrue(mapType.isMap());
		Assertions.assertFalse(mapType.isCollection());
	}


	@Test
	public void testEnum() {
		MetaResource meta = resourceProvider.getMeta(Task.class);
		MetaResourceField statusField = (MetaResourceField) meta.getAttribute("status");
		Assertions.assertEquals(TaskStatus.class, statusField.getType().getImplementationClass());

		MetaEnumType statusType = (MetaEnumType) statusField.getType();
		Assertions.assertEquals(3, statusType.getChildren().size());
		Assertions.assertEquals("OPEN", statusType.getChildren().get(0).getName());
		Assertions.assertEquals("INPROGRESS", statusType.getChildren().get(1).getName());
		Assertions.assertEquals("CLOSED", statusType.getChildren().get(2).getName());
	}

	@Test
	public void testSingleValuedRelation() {
		MetaResource meta = resourceProvider.getMeta(Task.class);

		MetaResourceField attr = (MetaResourceField) meta.getAttribute("schedule");
		Assertions.assertEquals("schedule", attr.getName());
		Assertions.assertEquals("resources.tasks.schedule", attr.getId());
		Assertions.assertFalse(attr.isLazy());
		Assertions.assertFalse(attr.getFieldType() == ResourceFieldType.META_INFORMATION);
		Assertions.assertFalse(attr.getFieldType() == ResourceFieldType.LINKS_INFORMATION);
		Assertions.assertTrue(attr.isAssociation());
		Assertions.assertNotNull(attr.getOppositeAttribute());
		Assertions.assertNotNull("tasks", attr.getOppositeAttribute().getName());
		Assertions.assertEquals(Schedule.class, attr.getType().getImplementationClass());

		Assertions.assertTrue(attr.isSortable());
		Assertions.assertTrue(attr.isFilterable());
		Assertions.assertTrue(attr.isInsertable());
		Assertions.assertTrue(attr.isUpdatable());
	}

	@Test
	public void testMultiValuedSetRelation() {
		MetaResource meta = resourceProvider.getMeta(Schedule.class);

		MetaResourceField attr = (MetaResourceField) meta.getAttribute("taskSet");
		Assertions.assertEquals("taskSet", attr.getName());
		Assertions.assertEquals("resources.schedule.tasks", attr.getId());
		Assertions.assertTrue(attr.isLazy());
		Assertions.assertFalse(attr.getFieldType() == ResourceFieldType.META_INFORMATION);
		Assertions.assertFalse(attr.getFieldType() == ResourceFieldType.LINKS_INFORMATION);
		Assertions.assertTrue(attr.isAssociation());
		Assertions.assertFalse(attr.isOwner());
		Assertions.assertNotNull(attr.getOppositeAttribute());
		Assertions.assertTrue(attr.getOppositeAttribute().isOwner());
		Assertions.assertNotNull("taskSet", attr.getOppositeAttribute().getName());
		Assertions.assertEquals(Set.class, attr.getType().getImplementationClass());
		Assertions.assertEquals(Task.class, attr.getType().getElementType().getImplementationClass());
		Assertions.assertTrue(attr.getType() instanceof MetaSetType, attr.getType().getClass().getName());

		MetaSetType listType = (MetaSetType) attr.getType();
		Assertions.assertTrue(listType.newInstance() instanceof Set);
		Assertions.assertTrue(listType.isCollection());
		Assertions.assertFalse(listType.isMap());
		Assertions.assertNotNull(attr.getType().asCollection());
	}

	@Test
	@Disabled
	public void testMultiValuedListRelation() {
		MetaResource meta = resourceProvider.getMeta(Task.class);

		MetaResourceField attr = (MetaResourceField) meta.getAttribute("projects");
		Assertions.assertFalse(attr.getFieldType() == ResourceFieldType.META_INFORMATION);
		Assertions.assertFalse(attr.getFieldType() == ResourceFieldType.LINKS_INFORMATION);
		Assertions.assertTrue(attr.isAssociation());
		Assertions.assertTrue(attr.isOwner());
		Assertions.assertEquals(ResourceList.class, attr.getType().getImplementationClass());
		Assertions.assertEquals(Project.class, attr.getType().getElementType().getImplementationClass());
		Assertions.assertTrue(attr.getType() instanceof MetaListType);

		MetaListType listType = (MetaListType) attr.getType();
		Assertions.assertTrue(listType.newInstance() instanceof List);
		Assertions.assertTrue(listType.isCollection());
		Assertions.assertFalse(listType.isMap());
		Assertions.assertNotNull(attr.getType().asCollection());
	}


	@Test
	public void testRepository() {
		MetaResource resourceMeta = resourceProvider.getMeta(Schedule.class);
		MetaResourceRepository meta = (MetaResourceRepository) lookup.getMetaById().get(resourceMeta.getId() + "$repository");
		Assertions.assertEquals(resourceMeta, meta.getResourceType());
		Assertions.assertTrue(meta.isExposed());
		Assertions.assertNotNull(meta.getListLinksType());
		Assertions.assertNotNull(meta.getListMetaType());
		Assertions.assertEquals(ScheduleRepository.ScheduleListLinks.class, meta.getListLinksType().getImplementationClass());
		Assertions.assertEquals(ScheduleRepository.ScheduleListMeta.class, meta.getListMetaType().getImplementationClass());

		List<MetaElement> children = new ArrayList<>(meta.getChildren());
		Collections.sort(children, Comparator.comparing(MetaElement::getName));
		Assertions.assertEquals(9, children.size());

		MetaResourceAction repositoryActionMeta = (MetaResourceAction) children.get(1);
		Assertions.assertEquals("repositoryAction", repositoryActionMeta.getName());
		Assertions.assertEquals(MetaRepositoryActionType.REPOSITORY, repositoryActionMeta.getActionType());
		MetaResourceAction resourceActionMeta = (MetaResourceAction) children.get(8);
		Assertions.assertEquals("resourceAction", resourceActionMeta.getName());
		Assertions.assertEquals(MetaRepositoryActionType.RESOURCE, resourceActionMeta.getActionType());

	}

	@Test
	public void testDynamicResources() {
		for (int i = 0; i < 2; i++) {
			MetaResource meta = (MetaResource) lookup.getMetaById().get("resources.dynamic" + i);
			Assertions.assertNotNull(meta);

			MetaAttribute parentAttr = meta.getAttribute("parent");
			Assertions.assertNotNull(meta.getAttribute("id"));
			Assertions.assertNotNull(meta.getAttribute("value"));
			Assertions.assertNotNull(parentAttr);
			Assertions.assertNotNull(meta.getAttribute("children"));

			Assertions.assertEquals("children", parentAttr.getOppositeAttribute().getName());
			Assertions.assertEquals("dynamic" + i, meta.getResourceType());
		}
	}

	@Test
	public void testVersioning() {
		MetaResource resourceMeta = resourceProvider.getMeta(VersionedTask.class);
		Assertions.assertEquals(0, resourceMeta.getVersionRange().getMin());
		Assertions.assertEquals(5, resourceMeta.getVersionRange().getMax());

		List<MetaResourceField> fields = (List<MetaResourceField>) resourceMeta.getAttributes();
		Assertions.assertEquals(4, fields.size());

		MetaResourceField idField = fields.get(0);
		MetaResourceField nameField = fields.get(1);
		MetaResourceField completedField = fields.get(2);
		MetaResourceField newCompletedField = fields.get(3);
		Assertions.assertEquals("id", idField.getName());
		Assertions.assertEquals("name", nameField.getName());
		Assertions.assertEquals("completed", completedField.getName());
		Assertions.assertEquals("completed", newCompletedField.getName());

		Assertions.assertEquals(1, completedField.getVersionRange().getMin());
		Assertions.assertEquals(3, completedField.getVersionRange().getMax());
		Assertions.assertEquals(5, newCompletedField.getVersionRange().getMin());
		Assertions.assertEquals(Integer.MAX_VALUE, newCompletedField.getVersionRange().getMax());
	}
}
