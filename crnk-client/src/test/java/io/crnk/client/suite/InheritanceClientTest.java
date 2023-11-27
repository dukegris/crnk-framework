package io.crnk.client.suite;

import io.crnk.client.internal.proxy.ObjectProxy;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Task;
import io.crnk.test.suite.InheritanceAccessTestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class InheritanceClientTest extends InheritanceAccessTestBase {

	public InheritanceClientTest() {
		ClientTestContainer testContainer = new ClientTestContainer();
		this.testContainer = testContainer;
	}

	@Test
	public void testIncludePoloymorphCollectionWithoutInclude() {
		doTestIncludePoloymorphCollection(false);
	}

	@Test
	public void testIncludePoloymorphCollectionWithInclude() {
		doTestIncludePoloymorphCollection(true);
	}

	private void doTestIncludePoloymorphCollection(boolean include) {
		QuerySpec querySpec = new QuerySpec(Project.class);
		if (include) {
			querySpec.includeRelation(Arrays.asList("tasks"));
		}
		List<Project> projects = projectRepo.findAll(querySpec);
		Assertions.assertEquals(1, projects.size());
		Project project = projects.get(0);

		List<Task> tasks = project.getTasks();
		if (include) {
			Assertions.assertFalse(tasks instanceof ObjectProxy);
		} else {
			ObjectProxy proxy = (ObjectProxy) tasks;
			Assertions.assertFalse(proxy.isLoaded());
		}

		if (tasks.get(0).getName().equals("baseTask")) {
			Assertions.assertEquals("baseTask", tasks.get(0).getName());
			Assertions.assertEquals("taskSubType", tasks.get(1).getName());
		} else {
			Assertions.assertEquals("baseTask", tasks.get(1).getName());
			Assertions.assertEquals("taskSubType", tasks.get(0).getName());
		}
	}

}
