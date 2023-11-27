package io.crnk.data;

import java.util.ArrayList;
import java.util.List;

import io.crnk.test.mock.models.BulkTask;
import io.crnk.test.mock.repository.BulkTaskRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PlainJsonBulkText {

	protected BulkTaskRepository taskRepo;

	private PlainJsonTestContainer container;

	@BeforeEach
	public void setup() {
		container = new PlainJsonTestContainer();
		container.start();
		taskRepo = container.getClient().getRepositoryForInterface(BulkTaskRepository.class);
	}

	@AfterEach
	public void tearDown() {
		container.stop();
	}

	@Test
	public void test() {
		List<BulkTask> tasks = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			BulkTask task = new BulkTask();
			task.setId((long) i);
			task.setName("bulkTask" + i);
			tasks.add(task);
		}

		List<BulkTask> createdTasks = taskRepo.create(tasks);
		Assertions.assertEquals(10, createdTasks.size());
		Assertions.assertEquals("bulkTask0", createdTasks.get(0).getName());
	}
}
