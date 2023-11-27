package io.crnk.core.engine.internal.dispatcher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.exception.ForbiddenException;
import io.crnk.core.exception.RequestBodyNotFoundException;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.ProjectPolymorphic;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.models.User;
import io.crnk.core.mock.repository.TaskToProjectRepository;
import io.crnk.core.mock.repository.UserToProjectRepository;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.utils.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RelationshipsPostControllerTest extends ControllerTestBase {

    private static final String REQUEST_TYPE = HttpMethod.POST.name();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private UserToProjectRepository localUserToProjectRepository;

    @BeforeEach
    public void beforeTest() {
        localUserToProjectRepository = (UserToProjectRepository) container.getRepository(User.class, "assignedProjects");
        localUserToProjectRepository.removeRelations("project");
        localUserToProjectRepository.removeRelations("assignedProjects");
    }

    @Test
    public void onValidRequestShouldAcceptIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.build("tasks/1/relationships/project", queryContext);
        RelationshipsPostController sut = new RelationshipsPostController();
        sut.init(controllerContext);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        assertThat(result).isTrue();
    }

    @Test
    public void onNonRelationRequestShouldDenyIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.build("tasks", queryContext);
        RelationshipsPostController sut = new RelationshipsPostController();
        sut.init(controllerContext);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    public void onMissingBodyThrowException() {
		Assertions.assertThrows(RequestBodyNotFoundException.class, () -> {
        JsonPath savedTaskPath = pathBuilder.build("/tasks/1/relationships/project", queryContext);
        RelationshipsPostController sut = new RelationshipsPostController();
        sut.init(controllerContext);

        // do not sent along a body
        Document newTaskToProjectBody = null;

        sut.handle(savedTaskPath, emptyProjectQuery, newTaskToProjectBody);
		});
    }

    @Test
    public void onExistingResourcesShouldAddToOneRelationship() {
        // GIVEN
        Document newTaskBody = new Document();
        newTaskBody.setData(Nullable.of(createTask()));

        JsonPath taskPath = pathBuilder.build("/tasks", queryContext);
        ResourcePostController resourcePost = new ResourcePostController();
        resourcePost.init(controllerContext);

        // WHEN -- adding a task
        Response taskResponse = resourcePost.handle(taskPath, emptyTaskQuery, newTaskBody);

        // THEN
        assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
        Long taskId = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());
        assertThat(taskId).isNotNull();

        /* ------- */

        // GIVEN
        Document newProjectBody = new Document();
        newProjectBody.setData(Nullable.of(createProject()));

        JsonPath projectPath = pathBuilder.build("/projects", queryContext);

        // WHEN -- adding a project
        Response projectResponse = resourcePost.handle(projectPath, emptyProjectQuery,
                newProjectBody);

        // THEN
        assertThat(projectResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
        assertThat(projectResponse.getDocument().getSingleData().get().getId()).isNotNull();
        assertThat(projectResponse.getDocument().getSingleData().get().getAttributes().get("name").asText())
                .isEqualTo("sample project");
        Long projectId = Long.parseLong(projectResponse.getDocument().getSingleData().get().getId());
        assertThat(projectId).isNotNull();

        /* ------- */

        // GIVEN
        Document newTaskToProjectBody = new Document();
        newTaskToProjectBody.setData(Nullable.of(createProject(Long.toString(projectId))));

        JsonPath savedTaskPath = pathBuilder.build("/tasks/" + taskId + "/relationships/project", queryContext);
        RelationshipsPostController sut = new RelationshipsPostController();
        sut.init(controllerContext);

        // WHEN -- adding a relation between task and project
        Response projectRelationshipResponse =
                sut.handle(savedTaskPath, emptyProjectQuery, newTaskToProjectBody);
        assertThat(projectRelationshipResponse).isNotNull();

        // THEN
        TaskToProjectRepository taskToProjectRepository = (TaskToProjectRepository) container.getRepository(Task.class, "project");
        Map<Long, Project> map = taskToProjectRepository.findOneRelations(Arrays.asList(taskId), "project", new QuerySpec(Project.class));
        Assertions.assertEquals(1, map.size());
        Project project = map.get(taskId);
        assertThat(project.getId()).isEqualTo(projectId);

        ResourceIdentifier projectResourceId = new ResourceIdentifier(projectId.toString(), "projects");
        // TODO properly implement ResourceIdentifier vs Resource in relationship repositories
        // Mockito.validate(modificationFilter, Mockito.times(1)).modifyOneRelationship(Mockito.any(), Mockito.any(ResourceField
        // .class), Mockito.eq(projectResourceId));
    }

    @Test
    public void onExistingResourcesShouldAddToManyRelationship() {
        // GIVEN
        Document newUserBody = new Document();
        Resource data = createUser();
        newUserBody.setData(Nullable.of(data));

        JsonPath taskPath = pathBuilder.build("/users", queryContext);
        ResourcePostController resourcePost = new ResourcePostController();
        resourcePost.init(controllerContext);

        // WHEN -- adding a user
        Response taskResponse = resourcePost.handle(taskPath, emptyUserQuery, newUserBody);

        // THEN
        assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("users");
        Long userId = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());
        assertThat(userId).isNotNull();

        /* ------- */

        // GIVEN
        Document newProjectBody = new Document();
        data = createProject();
        newProjectBody.setData(Nullable.of(data));
        data.setType("projects");

        JsonPath projectPath = pathBuilder.build("/projects", queryContext);

        // WHEN -- adding a project
        Response projectResponse = resourcePost.handle(projectPath, emptyProjectQuery, newProjectBody);

        // THEN
        assertThat(projectResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
        assertThat(projectResponse.getDocument().getSingleData().get().getId()).isNotNull();
        assertThat(projectResponse.getDocument().getSingleData().get().getAttributes().get("name").asText())
                .isEqualTo("sample project");
        Long projectId = Long.parseLong(projectResponse.getDocument().getSingleData().get().getId());
        assertThat(projectId).isNotNull();

        /* ------- */

        // GIVEN
        Document newTaskToProjectBody = new Document();
        data = new Resource();
        newTaskToProjectBody.setData(Nullable.of(Collections.singletonList(data)));
        data.setType("projects");
        data.setId(projectId.toString());

        JsonPath savedTaskPath = pathBuilder.build("/users/" + userId + "/relationships/assignedProjects", queryContext);
        RelationshipsPostController sut = new RelationshipsPostController();
        sut.init(controllerContext);

        // WHEN -- adding a relation between user and project
        Response projectRelationshipResponse =
                sut.handle(savedTaskPath, emptyProjectQuery, newTaskToProjectBody);
        assertThat(projectRelationshipResponse).isNotNull();

        // THEN
        UserToProjectRepository userToProjectRepository = (UserToProjectRepository) container.getRepository(User.class, "assignedProjects");
        Project project = userToProjectRepository.findOneTarget(userId, "assignedProjects", new QuerySpec(Project.class));
        assertThat(project.getId()).isEqualTo(projectId);
    }

    @Test
    public void onDeletingToOneRelationshipShouldSetTheValue() {
        // GIVEN
        Document newTaskBody = new Document();
        Resource data = createTask();
        newTaskBody.setData(Nullable.of(data));

        JsonPath taskPath = pathBuilder.build("/tasks", queryContext);
        ResourcePostController resourcePost = new ResourcePostController();
        resourcePost.init(controllerContext);

        // WHEN -- adding a task
        Response taskResponse = resourcePost.handle(taskPath, emptyTaskQuery, newTaskBody);

        // THEN
        assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
        Long taskId = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());
        assertThat(taskId).isNotNull();

        /* ------- */

        // GIVEN
        Document newTaskToProjectBody = new Document();
        newTaskToProjectBody.setData(Nullable.nullValue());

        JsonPath savedTaskPath = pathBuilder.build("/tasks/" + taskId + "/relationships/project", queryContext);
        RelationshipsPostController sut = new RelationshipsPostController();
        sut.init(controllerContext);

        // WHEN -- adding a relation between user and project
        Response projectRelationshipResponse =
                sut.handle(savedTaskPath, emptyProjectQuery, newTaskToProjectBody);
        assertThat(projectRelationshipResponse).isNotNull();

        // THEN
        assertThat(projectRelationshipResponse.getHttpStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
        Project project = localUserToProjectRepository.findOneTarget(1L, "project", new QuerySpec(Project.class));
        assertThat(project).isNull();
    }

    @Test
    public void supportPolymorphicRelationshipTypes() {

        // GIVEN
        Document newTaskBody = new Document();
        Resource data = createTask();
        newTaskBody.setData(Nullable.of(data));

        JsonPath taskPath = pathBuilder.build("/tasks", queryContext);

        ResourcePostController resourcePost = new ResourcePostController();
        resourcePost.init(controllerContext);
        Response taskResponse = resourcePost.handle(taskPath, emptyTaskQuery, newTaskBody);
        assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
        Long taskIdOne = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());
        assertThat(taskIdOne).isNotNull();
        taskResponse = resourcePost.handle(taskPath, emptyTaskQuery, newTaskBody);
        Long taskIdTwo = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());
        assertThat(taskIdOne).isNotNull();
        taskResponse = resourcePost.handle(taskPath, emptyTaskQuery, newTaskBody);
        Long taskIdThree = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());
        assertThat(taskIdOne).isNotNull();

        // Create ProjectPolymorphic object
        Document newProjectBody = new Document();
        data = new Resource();
        String type = ClassUtils.getAnnotation(ProjectPolymorphic.class, JsonApiResource.class).get().type();
        data.setType(type);
        data.setId("1");
        data.getRelationships().put("task", new Relationship(new ResourceIdentifier(taskIdOne.toString(), "tasks")));
        data.getRelationships().put("tasks", new Relationship(Arrays.asList(new ResourceIdentifier(taskIdTwo.toString(),
                        "tasks"),
                new ResourceIdentifier(taskIdThree.toString(), "tasks"))));
        newProjectBody.setData(Nullable.of(data));
        JsonPath projectPolymorphicTypePath = pathBuilder.build("/" + type, queryContext);

        // WHEN
        Response projectResponse =
                resourcePost.handle(projectPolymorphicTypePath, emptyProjectQuery, newProjectBody);

        // THEN
        assertThat(projectResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects-polymorphic");
        Long projectId = Long.parseLong(projectResponse.getDocument().getSingleData().get().getId());
        assertThat(projectId).isNotNull();
        Resource projectPolymorphic = projectResponse.getDocument().getSingleData().get();
        assertNotNull(projectPolymorphic.getRelationships().get("task").getSingleData().get());
        assertNotNull(projectPolymorphic.getRelationships().get("tasks"));

        ResourceRepository repository = resourceRegistry.getEntry(ProjectPolymorphic.class).getResourceRepositoryFacade();
        ProjectPolymorphic projectPolymorphicObj =
                (ProjectPolymorphic) repository.findOne(projectId, new QuerySpec(ProjectPolymorphic.class));
        assertNotNull(projectPolymorphicObj.getTasks());
        assertEquals(2, projectPolymorphicObj.getTasks().size());
    }

    @Test
    public void onNonPostableRelationshipShouldThrowException() {
        Task task = new Task();
        task.setName("some task");
        ResourceRepository<Task, Object> taskRepository = container.getRepository(Task.class);
        taskRepository.save(task);
        Long taskId = task.getId();

        // attempt to update non-postable relationship
        Document body = new Document();
        ResourceIdentifier id = new ResourceIdentifier("13", "things");
        body.setData(Nullable.of(id));
        JsonPath savedTaskPath = pathBuilder.build("/tasks/" + taskId + "/relationships/statusThing", queryContext);
        RelationshipsPostController sut = new RelationshipsPostController();
        sut.init(controllerContext);

        // WHEN -- adding a relation between user and project
        assertThatThrownBy(() -> sut.handle(savedTaskPath, emptyTaskQuery, body))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("field 'tasks.statusThing' cannot be accessed for POST");
    }
}
