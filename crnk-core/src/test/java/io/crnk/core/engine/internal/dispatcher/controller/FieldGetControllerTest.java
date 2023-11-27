package io.crnk.core.engine.internal.dispatcher.controller;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.dispatcher.controller.ControllerTestBase;
import io.crnk.core.engine.internal.dispatcher.controller.FieldResourceGetController;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.models.User;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FieldGetControllerTest extends ControllerTestBase {

	private static final String REQUEST_TYPE = "GET";

	@Test
	public void onValidRequestShouldAcceptIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("tasks/1/project", queryContext);
		FieldResourceGetController sut = new FieldResourceGetController();
		sut.init(controllerContext);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		assertThat(result).isTrue();
	}

	@Test
	public void onRelationshipRequestShouldDenyIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("tasks/1/relationships/project", queryContext);
		FieldResourceGetController sut = new FieldResourceGetController();
		sut.init(controllerContext);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		assertThat(result).isFalse();
	}

	@Test
	public void onNonRelationRequestShouldDenyIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("tasks", queryContext);
		FieldResourceGetController sut = new FieldResourceGetController();
		sut.init(controllerContext);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		assertThat(result).isFalse();
	}

	@Test
	public void onGivenRequestFieldResourceGetShouldHandleIt() {
		// GIVEN

		JsonPath jsonPath = pathBuilder.build("/tasks/1/project", queryContext);
		FieldResourceGetController sut = new FieldResourceGetController();
		sut.init(controllerContext);

		// WHEN
		Response response = sut.handle(jsonPath, emptyProjectQuery, null);

		// THEN
		Assertions.assertNotNull(response);
	}

	@Test
	public void onGivenRequestFieldResourcesGetShouldHandleIt() {
		// GIVEN

		JsonPath jsonPath = pathBuilder.build("/users/1/assignedProjects", queryContext);
		FieldResourceGetController sut = new FieldResourceGetController();
		sut.init(controllerContext);

		// WHEN
		Response response = sut.handle(jsonPath, emptyProjectQuery, null);

		// THEN
		Assertions.assertNotNull(response);
	}

	@Test
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void onGivenIncludeRequestFieldResourcesGetShouldHandleIt() {

		// get repositories
		ResourceRepositoryAdapter userRepo = resourceRegistry.getEntry(User.class).getResourceRepository();
		ResourceRepositoryAdapter projectRepo = resourceRegistry.getEntry(Project.class).getResourceRepository();
		ResourceRepositoryAdapter taskRepo = resourceRegistry.getEntry(Task.class).getResourceRepository();

		RelationshipRepositoryAdapter relRepositoryUserToProject =
				resourceRegistry.getEntry(User.class).getRelationshipRepository("assignedProjects");
		RelationshipRepositoryAdapter relRepositoryProjectToTask =
				resourceRegistry.getEntry(Project.class).getRelationshipRepository("tasks");

		ResourceInformation userInfo = resourceRegistry.getEntry(User.class).getResourceInformation();
		ResourceInformation projectInfo = resourceRegistry.getEntry(Project.class).getResourceInformation();
		ResourceField includedTaskField = projectInfo.findRelationshipFieldByName("includedTask");
		ResourceField assignedProjectsField = userInfo.findRelationshipFieldByName("assignedProjects");

		// setup test data
		User user = new User();
		user.setLoginId(1L);
		userRepo.create(user, emptyUserQuery);
		Project project = new Project();
		project.setId(2L);
		projectRepo.create(project, emptyProjectQuery);
		Task task = new Task();
		task.setId(3L);
		taskRepo.create(task, emptyTaskQuery);
		relRepositoryUserToProject
				.setRelations(user, Collections.singletonList(project.getId()), assignedProjectsField, emptyProjectQuery);
		relRepositoryProjectToTask.setRelation(project, task.getId(), includedTaskField, emptyTaskQuery);

		QuerySpec queryParams = new QuerySpec(Project.class);
		queryParams.includeRelation(PathSpec.of("includedTask"));

		QueryAdapter queryAdapter = container.toQueryAdapter(queryParams);
		JsonPath jsonPath = pathBuilder.build("/users/1/assignedProjects", queryContext);
		FieldResourceGetController sut = new FieldResourceGetController();
		sut.init(controllerContext);

		Response response = sut.handle(jsonPath, queryAdapter, null);

		// THEN
		Assertions.assertNotNull(response);
		Assertions.assertNotNull(response.getDocument().getData());
		List<Resource> entityList = ((List<Resource>) response.getDocument().getData().get());
		Assertions.assertEquals(true, entityList.size() > 0);
		Assertions.assertEquals("projects", entityList.get(0).getType());
		Resource returnedProject = entityList.get(0);
		Assertions.assertEquals(project.getId().toString(), returnedProject.getId());
		Relationship relationship = returnedProject.getRelationships().get("includedTask");
		Assertions.assertNotNull(relationship);
		Assertions.assertEquals(task.getId().toString(), relationship.getSingleData().get().getId());
	}

}
