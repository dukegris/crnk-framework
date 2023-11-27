package io.crnk.reactive;

import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.reactive.internal.adapter.ReactiveOneRelationshipRepositoryAdapter;
import io.crnk.reactive.model.ReactiveProject;
import io.crnk.reactive.model.ReactiveTask;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

public class ReactiveOneRelationshipRepositoryAdapterTest extends ReactiveTestBase {

	private QuerySpec querySpec;

	private QuerySpecAdapter queryAdapter;

	private ReactiveOneRelationshipRepositoryAdapter adapter;

	@BeforeEach
	public void setup() {
		super.setup();

		querySpec = new QuerySpec(ReactiveTask.class);
		queryAdapter = new QuerySpecAdapter(querySpec, boot.getResourceRegistry(), queryContext);

		ResourceRegistry resourceRegistry = boot.getResourceRegistry();
		RegistryEntry entry = resourceRegistry.getEntry(ReactiveTask.class);
		adapter = (ReactiveOneRelationshipRepositoryAdapter) entry.getRelationshipRepository("project");
	}

	@Test
	public void setRelation() {
		ReactiveTask task = createTask(1);
		ReactiveProject project = createProject(2);
		adapter.setRelation(task, project.getId(), adapter.getResourceField(), queryAdapter).get();

		Map<Long, Long> relationMap = taskToProject.getRelationMap();
		Assertions.assertEquals(1, relationMap.size());
		Assertions.assertEquals(Long.valueOf(2L), relationMap.get(Long.valueOf(1L)));
	}


	@Test
	public void findOneTarget() {
		ReactiveProject project = createProject(2);
		projectRepository.getMap().put(2L, project);
		taskToProject.getRelationMap().put(1L, 2L);

		JsonApiResponse response = adapter.findOneRelations(1L, adapter.getResourceField(), queryAdapter).get();
		Assertions.assertEquals(project, response.getEntity());
	}


	@Test
	public void findBulkOneTargets() {
		ReactiveProject project = createProject(2);
		projectRepository.getMap().put(2L, project);
		taskToProject.getRelationMap().put(1L, 2L);

		Map<Object, JsonApiResponse> responses =
				adapter.findBulkOneTargets(Arrays.asList(1L), adapter.getResourceField(), queryAdapter).get();
		Assertions.assertEquals(1, responses.size());
		Assertions.assertEquals(project, responses.get(1L).getEntity());
	}

	@Test
	public void setRelations() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
			adapter.setRelations(null, null, null, null);
		});
	}

	@Test
	public void addRelations() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
			adapter.addRelations(null, null, null, null);
		});
	}

	@Test
	public void removeRelations() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
			adapter.removeRelations(null, null, null, null);
		});
	}

	@Test
	public void findManyTargets() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
			adapter.findManyRelations(null, null, null);
		});
	}

	@Test
	public void findBulkManyTargets() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
			adapter.findBulkManyTargets(null, null, null);
		});
	}
}
