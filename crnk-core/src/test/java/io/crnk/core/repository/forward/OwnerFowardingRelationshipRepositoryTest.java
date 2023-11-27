package io.crnk.core.repository.forward;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.crnk.core.CoreTestContainer;
import io.crnk.core.CoreTestModule;
import io.crnk.core.engine.internal.utils.CoreClassTestUtils;
import io.crnk.core.engine.internal.utils.MultivaluedMap;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.RelationIdTestResource;
import io.crnk.core.mock.models.Schedule;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.repository.RelationIdTestRepository;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipMatcher;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.repository.foward.ForwardingDirection;
import io.crnk.core.repository.foward.ForwardingRelationshipRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OwnerFowardingRelationshipRepositoryTest {


	private ForwardingRelationshipRepository relRepository;

	private ResourceRepository<Schedule, Long> scheduleRepository;

	private Schedule schedule3;

	private RelationIdTestRepository testRepository;

	private RelationIdTestResource resource;

	private Schedule schedule;

	private ResourceRepository<Project, Long> projectRepository;

	private Project project;

	private ResourceRepository<Task, Long> taskRepository;

	private Task task;

	private ForwardingRelationshipRepository taskProjectRepository;

	private ResourceRegistry resourceRegistry;

	private ForwardingRelationshipRepository projectTaskRepository;

	@BeforeEach
	public void setup() {
		CoreTestContainer container = new CoreTestContainer();
		container.addModule(new CoreTestModule());
		container.boot();

		resourceRegistry = container.getResourceRegistry();

		RegistryEntry entry = resourceRegistry.getEntry(RelationIdTestResource.class);
		relRepository =
				(ForwardingRelationshipRepository) entry.getRelationshipRepository("testSerializeEager")
						.getImplementation();

		RelationshipMatcher taskProjectMatcher = new RelationshipMatcher().rule().source(Task.class).target(Project.class).add();
		taskProjectRepository = new ForwardingRelationshipRepository(Task.class, taskProjectMatcher, ForwardingDirection.OWNER,
				ForwardingDirection.OWNER);
		taskProjectRepository.setResourceRegistry(resourceRegistry);
		taskProjectRepository.setHttpRequestContextProvider(container.getModuleRegistry().getHttpRequestContextProvider());

		projectTaskRepository = new ForwardingRelationshipRepository(Project.class, taskProjectMatcher, ForwardingDirection
				.OWNER,
				ForwardingDirection.OWNER);
		projectTaskRepository.setResourceRegistry(resourceRegistry);
		projectTaskRepository.setHttpRequestContextProvider(container.getModuleRegistry().getHttpRequestContextProvider());

		testRepository = (RelationIdTestRepository) entry.getResourceRepository().getImplementation();
		testRepository.setResourceRegistry(resourceRegistry);
		resource = new RelationIdTestResource();
		resource.setId(2L);
		resource.setName("relationId");
		testRepository.create(resource);


		scheduleRepository = container.getRepository(Schedule.class);
		schedule3 = new Schedule();
		schedule3.setId(3L);
		schedule3.setName("schedule");
		scheduleRepository.create(schedule3);

		for (int i = 0; i < 10; i++) {
			schedule = new Schedule();
			schedule.setId(4L + i);
			schedule.setName("schedule");
			scheduleRepository.create(schedule);

			projectRepository = container.getRepository(Project.class);
			project = new Project();
			project.setId(42L + i);
			project.setName("project");
			projectRepository.save(project);

			taskRepository = container.getRepository(Task.class);
			task = new Task();
			task.setId(13L + i);
			task.setName("task");
			taskRepository.save(task);
		}
	}

	@Test
	public void hasProtectedDefaultConstructor() {
		CoreClassTestUtils.assertProtectedConstructor(ForwardingRelationshipRepository.class);
	}

	@Test
	public void checkSetRelationId() {
		relRepository.setRelation(resource, 3L, "testSerializeEager");
		Assertions.assertEquals(3L, resource.getTestSerializeEagerId().longValue());
		Assertions.assertNull(resource.getTestSerializeEager());

		Assertions.assertSame(schedule3,
				relRepository.findOneTarget(resource.getId(), "testSerializeEager", new QuerySpec(Schedule.class)));

		MultivaluedMap targets =
				relRepository.findTargets(Arrays.asList(resource.getId()), "testSerializeEager", new QuerySpec(Schedule.class));
		Assertions.assertEquals(1, targets.keySet().size());
		Object target = targets.getUnique(resource.getId());
		Assertions.assertEquals(schedule3, target);

		relRepository.setRelation(resource, null, "testSerializeEager");
		Assertions.assertNull(resource.getTestSerializeEagerId());
		Assertions.assertNull(resource.getTestSerializeEager());
	}

	@Test
	public void checkSetRelationIdNotFound() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
		relRepository.setRelation(resource, 123123L, "testSerializeEager");
			Assertions.assertEquals(123123L, resource.getTestSerializeEagerId().longValue());
			Assertions.assertNull(resource.getTestSerializeEager());
		relRepository.findOneTarget(resource.getId(), "testSerializeEager", new QuerySpec(Schedule.class));
		});
	}

	@Test
	public void checkSetRelationIdToNull() {
		relRepository.setRelation(resource, null, "testSerializeEager");
		Assertions.assertEquals(null, resource.getTestSerializeEagerId());
		Assertions.assertNull(resource.getTestSerializeEager());
		Assertions.assertNull(relRepository.findOneTarget(resource.getId(), "testSerializeEager", new QuerySpec(Schedule.class)));
	}

	@Test
	public void checkSetRelationIds() {
		relRepository.setRelations(resource, Arrays.asList(3L, 4L), "testMultipleValues");
		Assertions.assertEquals(Arrays.asList(3L, 4L), resource.getTestMultipleValueIds());

		List<Schedule> targets =
				relRepository.findManyTargets(resource.getId(), "testMultipleValues", new QuerySpec(Schedule.class));
		Assertions.assertEquals(2, targets.size());
		Assertions.assertSame(schedule3, targets.get(0));
		Assertions.assertSame(4L, targets.get(1).getId().longValue());

		MultivaluedMap targetsMap =
				relRepository.findTargets(Arrays.asList(resource.getId()), "testMultipleValues", new QuerySpec(Schedule.class));
		Assertions.assertEquals(1, targetsMap.keySet().size());
		targets = targetsMap.getList(resource.getId());
		Assertions.assertEquals(2, targets.size());
		Assertions.assertSame(3L, targets.get(0).getId().longValue());
		Assertions.assertSame(4L, targets.get(1).getId().longValue());
	}

	@Test
	public void checkAddRemoveRelationIds() {
		relRepository.addRelations(resource, Arrays.asList(3L, 4L), "testMultipleValues");
		Assertions.assertEquals(Arrays.asList(3L, 4L), resource.getTestMultipleValueIds());

		relRepository.addRelations(resource, Arrays.asList(5L), "testMultipleValues");
		Assertions.assertEquals(Arrays.asList(3L, 4L, 5L), resource.getTestMultipleValueIds());

		relRepository.removeRelations(resource, Arrays.asList(3L), "testMultipleValues");
		Assertions.assertEquals(Arrays.asList(4L, 5L), resource.getTestMultipleValueIds());
	}

	@Test
	public void checkSetRelation() {
		taskProjectRepository.setRelation(task, 42L, "project");
		Assertions.assertEquals(42L, task.getProject().getId().longValue());

		Project target = (Project) taskProjectRepository.findOneTarget(task.getId(), "project", new QuerySpec(Task.class));
		Assertions.assertSame(42L, target.getId().longValue());
	}

	@Test
	public void checkSetRelations() {
		taskProjectRepository.setRelations(task, Arrays.asList(42L), "projects");
		Assertions.assertEquals(1, task.getProjects().size());
		Assertions.assertEquals(42L, task.getProjects().iterator().next().getId().longValue());

		MultivaluedMap targets =
				taskProjectRepository.findTargets(Arrays.asList(task.getId()), "projects", new QuerySpec(Task.class));
		Assertions.assertEquals(1, targets.keySet().size());
		Assertions.assertEquals(task.getId(), targets.keySet().iterator().next());
		Project target = (Project) targets.getUnique(task.getId());
		Assertions.assertEquals(42L, target.getId().longValue());
	}


	@Test
	public void checkAddRemoveRelations() {
		projectTaskRepository.addRelations(project, Arrays.asList(13L, 14L), "tasks");
		Set<Long> taskIds = project.getTasks().stream().map(it -> it.getId()).collect(Collectors.toSet());
		Assertions.assertTrue(taskIds.contains(13L));
		Assertions.assertTrue(taskIds.contains(14L));

		projectTaskRepository.addRelations(project, Arrays.asList(15L), "tasks");
		Assertions.assertEquals(3, project.getTasks().size());
		taskIds = project.getTasks().stream().map(it -> it.getId()).collect(Collectors.toSet());
		Assertions.assertTrue(taskIds.contains(13L));
		Assertions.assertTrue(taskIds.contains(14L));
		Assertions.assertTrue(taskIds.contains(15L));

		projectTaskRepository.removeRelations(project, Arrays.asList(13L), "tasks");
		Assertions.assertEquals(2, project.getTasks().size());
		taskIds = project.getTasks().stream().map(it -> it.getId()).collect(Collectors.toSet());
		Assertions.assertTrue(taskIds.contains(14L));
		Assertions.assertTrue(taskIds.contains(15L));
	}

	@Test
	public void checkFilterRelations() {
		FilterSpec filterTasks = new FilterSpec(Arrays.asList("id"), FilterOperator.GT, 13L);
		QuerySpec querySpecTask = new QuerySpec(Task.class);

		FilterSpec filterSchedules = new FilterSpec(Arrays.asList("id"), FilterOperator.GT, 3L);
		QuerySpec querySpecSchedules = new QuerySpec(Schedule.class);

		projectTaskRepository.setRelations(project, Arrays.asList(13L, 14L, 15L), "tasks");
		relRepository.setRelations(resource, new ArrayList<Long>(Arrays.asList(3L, 4L)), "testMultipleValues");

		MultivaluedMap targetsTasks =
				projectTaskRepository.findTargets(Arrays.asList(project.getId()), "tasks", querySpecTask);
		MultivaluedMap targetsMap =
				relRepository.findTargets(Arrays.asList(resource.getId()), "testMultipleValues", querySpecSchedules);
		Assertions.assertEquals(3, targetsTasks.getList(project.getId()).size());
		Assertions.assertEquals(2, targetsMap.getList(resource.getId()).size());

		querySpecTask.addFilter(filterTasks);
		querySpecSchedules.addFilter(filterSchedules);

		targetsTasks =
				projectTaskRepository.findTargets(Arrays.asList(project.getId()), "tasks", querySpecTask);
		targetsMap =
				relRepository.findTargets(Arrays.asList(resource.getId()), "testMultipleValues", querySpecSchedules);

		Assertions.assertEquals(2, targetsTasks.getList(project.getId()).size());
		Assertions.assertEquals(1, targetsMap.getList(resource.getId()).size());
	}
}
