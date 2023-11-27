package io.crnk.core.engine.internal.information;

import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.repository.RelationshipRepositoryInformation;
import io.crnk.core.engine.information.repository.RepositoryMethodAccess;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldAccess;
import io.crnk.core.engine.information.resource.ResourceFieldAccessor;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.resource.ResourceTypeHolder;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;
import io.crnk.core.resource.annotations.SerializeType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class DefaultInformationBuilderTest {

	private DefaultInformationBuilder builder;

	@BeforeEach
	public void setup() {
		TypeParser parser = new TypeParser();
		builder = new DefaultInformationBuilder(parser);
	}

	@Test
	public void resourceWithNoResourcePath() {
		InformationBuilder.ResourceInformationBuilder resource = builder.createResource(Task.class, "tasks");
		ResourceInformation info = resource.build();
		resource.superResourceType("superTask");
		resource.implementationType(Project.class);
		Assertions.assertEquals("tasks", info.getResourceType());
		Assertions.assertEquals("tasks", info.getResourcePath());
	}

	@Test
	public void resource() {
		InformationBuilder.ResourceInformationBuilder resource = builder.createResource(Task.class, "tasks", null);
		resource.superResourceType("superTask");
		resource.resourceType("changedTasks");
		resource.implementationType(Project.class);

		InformationBuilder.FieldInformationBuilder idField = resource.addField("id", ResourceFieldType.ID, String.class);
		idField.serializeType(SerializeType.EAGER);
		idField.access(new ResourceFieldAccess(true, true, true, false, false));

		ResourceFieldAccessor accessor = Mockito.mock(ResourceFieldAccessor.class);
		InformationBuilder.FieldInformationBuilder projectField = resource.addField("project", ResourceFieldType.RELATIONSHIP, Project.class);
		projectField.serializeType(SerializeType.EAGER);
		projectField.access(new ResourceFieldAccess(true, false, true, false, false));
		projectField.oppositeName("tasks");
		projectField.relationshipRepositoryBehavior(RelationshipRepositoryBehavior.FORWARD_OWNER);
		projectField.lookupIncludeBehavior(LookupIncludeBehavior.AUTOMATICALLY_ALWAYS);
		projectField.accessor(accessor);

		ResourceInformation info = resource.build();
		Assertions.assertEquals("changedTasks", info.getResourceType());
		Assertions.assertEquals(Project.class, info.getResourceClass());
		Assertions.assertEquals("superTask", info.getSuperResourceType());

		ResourceField idInfo = info.findFieldByName("id");
		Assertions.assertEquals("id", idInfo.getUnderlyingName());
		Assertions.assertEquals(String.class, idInfo.getType());
		Assertions.assertFalse(idInfo.getAccess().isFilterable());
		Assertions.assertFalse(idInfo.getAccess().isSortable());
		Assertions.assertTrue(idInfo.getAccess().isPostable());
		Assertions.assertTrue(idInfo.getAccess().isPatchable());
		Assertions.assertEquals(SerializeType.EAGER, idInfo.getSerializeType());
		Assertions.assertFalse(idInfo.isCollection());

		ResourceField projectInfo = info.findFieldByName("project");
		Assertions.assertEquals("project", projectInfo.getUnderlyingName());
		Assertions.assertEquals("tasks", projectInfo.getOppositeName());
		Assertions.assertEquals(LookupIncludeBehavior.AUTOMATICALLY_ALWAYS, projectInfo.getLookupIncludeBehavior());
		Assertions.assertEquals(Project.class, projectInfo.getType());
		Assertions.assertSame(accessor, projectInfo.getAccessor());
		Assertions.assertFalse(projectInfo.getAccess().isFilterable());
		Assertions.assertFalse(projectInfo.getAccess().isSortable());
		Assertions.assertFalse(projectInfo.getAccess().isPostable());
		Assertions.assertTrue(projectInfo.getAccess().isPatchable());
		Assertions.assertEquals(SerializeType.EAGER, projectInfo.getSerializeType());
		Assertions.assertEquals(RelationshipRepositoryBehavior.FORWARD_OWNER, projectInfo.getRelationshipRepositoryBehavior());
		Assertions.assertFalse(projectInfo.isCollection());
	}


	@Test
	public void checkRelationIdFieldCreation() {
		InformationBuilder.ResourceInformationBuilder resource = builder.createResource(Task.class, "tasks", null);
		resource.superResourceType("superTask");
		resource.resourceType("changedTasks");
		resource.implementationType(Project.class);

		InformationBuilder.FieldInformationBuilder idField = resource.addField("id", ResourceFieldType.ID, String.class);
		idField.serializeType(SerializeType.EAGER);
		idField.access(new ResourceFieldAccess(true, true, true, false, false));

		ResourceFieldAccessor idAccessor = Mockito.mock(ResourceFieldAccessor.class);
		ResourceFieldAccessor accessor = Mockito.mock(ResourceFieldAccessor.class);
		InformationBuilder.FieldInformationBuilder projectField = resource.addField("project", ResourceFieldType.RELATIONSHIP, Project.class);
		projectField.idName("taskId");
		projectField.idAccessor(idAccessor);
		projectField.idType(Long.class);
		projectField.serializeType(SerializeType.EAGER);
		projectField.access(new ResourceFieldAccess(true, false, true, false, false));
		projectField.oppositeName("tasks");
		projectField.lookupIncludeBehavior(LookupIncludeBehavior.AUTOMATICALLY_ALWAYS);
		projectField.accessor(accessor);

		ResourceInformation info = resource.build();
		Assertions.assertEquals("changedTasks", info.getResourceType());
		Assertions.assertEquals(Project.class, info.getResourceClass());
		Assertions.assertEquals("superTask", info.getSuperResourceType());

		ResourceField projectInfo = info.findFieldByName("project");
		Assertions.assertEquals("project", projectInfo.getUnderlyingName());
		Assertions.assertEquals("tasks", projectInfo.getOppositeName());
		Assertions.assertEquals(LookupIncludeBehavior.AUTOMATICALLY_ALWAYS, projectInfo.getLookupIncludeBehavior());
		Assertions.assertEquals(Project.class, projectInfo.getType());
		Assertions.assertSame(accessor, projectInfo.getAccessor());
		Assertions.assertFalse(projectInfo.getAccess().isFilterable());
		Assertions.assertFalse(projectInfo.getAccess().isSortable());
		Assertions.assertFalse(projectInfo.getAccess().isPostable());
		Assertions.assertTrue(projectInfo.getAccess().isPatchable());
		Assertions.assertEquals(SerializeType.EAGER, projectInfo.getSerializeType());
		Assertions.assertTrue(projectInfo.hasIdField());
		Assertions.assertEquals("taskId", projectInfo.getIdName());
		Assertions.assertEquals(Long.class, projectInfo.getIdType());
		Assertions.assertSame(idAccessor, projectInfo.getIdAccessor());
		Assertions.assertFalse(projectInfo.isCollection());
	}

	@Test
	public void checkResourceRepository() {
		ResourceInformation resourceInformation = builder.createResource(Task.class, "tasks", null).build();

		InformationBuilder.ResourceRepositoryInformationBuilder repositoryBuilder = builder.createResourceRepository();
		repositoryBuilder.setResourceInformation(resourceInformation);
		RepositoryMethodAccess expectedAccess = new RepositoryMethodAccess(true, false, true, false);
		repositoryBuilder.setAccess(expectedAccess);
		ResourceRepositoryInformation repositoryInformation = repositoryBuilder.build();
		RepositoryMethodAccess actualAccess = repositoryInformation.getAccess();
		Assertions.assertEquals(expectedAccess, actualAccess);
		Assertions.assertSame(resourceInformation, repositoryInformation.getResourceInformation().get());
	}

	@Test
	public void checkResourceTypeHolderIngored() {
		ResourceInformation resourceInformation = builder.createResource(Task.class, "tasks", null).build();
		Assertions.assertTrue(ResourceTypeHolder.class.isAssignableFrom(ResourceTypeHolder.class));
		Assertions.assertNull(resourceInformation.findFieldByName("type"));
	}

	@Test
	public void checkRelationshipRepository() {
		InformationBuilder.RelationshipRepositoryInformationBuilder repositoryBuilder = builder.createRelationshipRepository("projects", "tasks");
		RepositoryMethodAccess expectedAccess = new RepositoryMethodAccess(true, false, true, false);
		repositoryBuilder.setAccess(expectedAccess);
		RelationshipRepositoryInformation repositoryInformation = repositoryBuilder.build();
		RepositoryMethodAccess actualAccess = repositoryInformation.getAccess();
		Assertions.assertEquals(expectedAccess, actualAccess);
	}
}
