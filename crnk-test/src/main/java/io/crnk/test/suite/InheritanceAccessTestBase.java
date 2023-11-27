package io.crnk.test.suite;

import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.SortSpec;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.models.TaskSubType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public abstract class InheritanceAccessTestBase {

	protected TestContainer testContainer;

	protected ResourceRepository<Task, Long> taskRepo;

	protected ResourceRepository<Project, Long> projectRepo;

	protected RelationshipRepository<Project, Long, Task, Long> relRepo;

	@BeforeEach
	public void setup() {
		testContainer.start();
		taskRepo = testContainer.getRepositoryForType(Task.class);
		projectRepo = testContainer.getRepositoryForType(Project.class);
		relRepo = testContainer.getRepositoryForType(Project.class, Task.class);

		Task baseTask = new Task();
		baseTask.setId(Long.valueOf(1));
		baseTask.setName("baseTask");
		taskRepo.create(baseTask);

		TaskSubType taskSubType = new TaskSubType();
		taskSubType.setId(Long.valueOf(2));
		taskSubType.setName("taskSubType");
		taskSubType.setSubTypeValue(13);
		taskRepo.create(taskSubType);

		Project project = new Project();
		project.setId(1L);
		project.setName("project0");
		project.setTasks(Arrays.asList(baseTask, taskSubType));
		projectRepo.create(project);

		relRepo.addRelations(project, Arrays.asList(baseTask.getId(), taskSubType.getId()), "tasks");
	}

	@AfterEach
	public void tearDown() {
		testContainer.stop();
	}


	@Test
	public void testFindAll() {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.addSort(new SortSpec(Arrays.asList("name"), Direction.ASC));
		List<Task> tasks = taskRepo.findAll(querySpec);
		Assertions.assertEquals(2, tasks.size());

		Assertions.assertEquals("baseTask", tasks.get(0).getName());
		Assertions.assertEquals("taskSubType", tasks.get(1).getName());
	}
}
