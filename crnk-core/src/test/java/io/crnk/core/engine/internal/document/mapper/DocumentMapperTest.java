package io.crnk.core.engine.internal.document.mapper;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.mock.models.LazyTask;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Schedule;
import io.crnk.core.mock.models.Task;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.links.DefaultLink;
import io.crnk.core.resource.links.DefaultSelfLinksInformation;
import io.crnk.core.resource.links.Link;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.core.utils.Nullable;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class DocumentMapperTest extends AbstractDocumentMapperTest {

	@Test
	public void testAttributesBasic() {
		Task task = createTask(2, "sample task");

		Document document = mapper.toDocument(toResponse(task), createAdapter(Task.class), mappingConfig).get();
		Resource resource = document.getSingleData().get();
		Assertions.assertEquals("2", resource.getId());
		Assertions.assertEquals("tasks", resource.getType());
		Assertions.assertEquals("sample task", resource.getAttributes().get("name").asText());
		MatcherAssert.assertThat(resource.getAttributes().get("writeOnlyValue"), CoreMatchers.nullValue());
	}

	@Test
	public void testSerializeWithoutLinks() {
		Task task = createTask(2, "sample task");

		mappingConfig.getResourceMapping().setSerializeLinks(false);
		Document document = mapper.toDocument(toResponse(task), createAdapter(Task.class), mappingConfig).get();

		Resource resource = document.getSingleData().get();
		Assertions.assertEquals("2", resource.getId());
		Assertions.assertEquals("tasks", resource.getType());
		Assertions.assertNull(resource.getLinks());

		Relationship relationship = resource.getRelationships().get("project");
		Assertions.assertNull(relationship.getLinks());
	}

	@Test
	public void testSerializeRootSelfLink() {
		Task task = createTask(2, "sample task");
		QueryAdapter adapter = createAdapter(Task.class);
//		Mockito.when(container.getRequestContextBase().getRequestUri()).thenReturn(URI.create("http://localhost/api/foo"));
		Document document = mapper.toDocument(toResponse(task), adapter, mappingConfig).get();
		Assertions.assertEquals("http://127.0.0.1", getLinkText(document.getLinks().get("self")));
	}

	public static class TaskLinks implements LinksInformation {

		public Link self = new DefaultLink("something");

		public Link someLink = new DefaultLink("link");

	}

	@Test
	public void testCompactMode() {
		LinksInformation links = new TaskLinks();
		Task task = createTask(2, "sample task");
		task.setLinksInformation(links);

		QuerySpecAdapter queryAdapter = (QuerySpecAdapter) toAdapter(new QuerySpec(Task.class));
		queryAdapter.setCompactMode(true);

		Document document = mapper.toDocument(toResponse(task), queryAdapter, mappingConfig).get();
		Resource resource = document.getSingleData().get();
		Assertions.assertEquals("2", resource.getId());
		Assertions.assertEquals("tasks", resource.getType());
		Assertions.assertNull(resource.getLinks().get("self"));
		Assertions.assertNotNull(resource.getLinks().get("someLink"));

		Relationship project = resource.getRelationships().get("project");
		Assertions.assertNull(project.getLinks());
	}

	@Test
	public void testCompactModeWithInclusion() {
		Project project = new Project();
		project.setName("someProject");
		project.setId(3L);
		Task task = createTask(2, "sample task");
		task.setProject(project);

		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.includeRelation(Arrays.asList("project"));
		QueryAdapter queryAdapter = toAdapter(querySpec);

		DocumentMappingConfig config = mappingConfig.clone();
		config.getResourceMapping().setSerializeSelfRelationshipLinks(false);

		Document document = mapper.toDocument(toResponse(task), queryAdapter, config).get();
		Resource resource = document.getSingleData().get();
		Assertions.assertEquals("2", resource.getId());

		Relationship projectRel = resource.getRelationships().get("project");
		ObjectNode links = projectRel.getLinks();
		Assertions.assertFalse(links.has("self"));
		Assertions.assertTrue(links.has("related"));
	}

	@Test
	public void testOmitSelfRelatedLinks() {
		Project project = new Project();
		project.setName("someProject");
		project.setId(3L);
		LinksInformation links = new TaskLinks();
		Task task = createTask(2, "sample task");
		task.setLinksInformation(links);
		task.setProject(project);

		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.includeRelation(Arrays.asList("project"));
		QuerySpecAdapter queryAdapter = (QuerySpecAdapter) toAdapter(querySpec);
		queryAdapter.setCompactMode(true);

		Document document = mapper.toDocument(toResponse(task), queryAdapter, mappingConfig.clone()).get();
		Resource resource = document.getSingleData().get();
		Assertions.assertEquals("2", resource.getId());
		Assertions.assertEquals("tasks", resource.getType());
		Assertions.assertNull(resource.getLinks().get("self"));
		Assertions.assertNotNull(resource.getLinks().get("someLink"));

		Relationship projectRel = resource.getRelationships().get("project");
		Assertions.assertNull(projectRel.getLinks());

		Assertions.assertEquals(1, document.getIncluded().size());
		Resource projectResource = document.getIncluded().get(0);
		Assertions.assertNull(projectResource.getRelationships().get("tasks"));
	}


	@Test
	public void testCustomSelfLinks() {
		TaskLinks links = new TaskLinks();
		links.self = new DefaultLink("http://something.else.net/api/tasks/3");
		Task task = createTask(2, "sample task");
		task.setLinksInformation(links);

		QuerySpec querySpec = new QuerySpec(Task.class);
		QuerySpecAdapter queryAdapter = (QuerySpecAdapter) toAdapter(querySpec);

		Document document = mapper.toDocument(toResponse(task), queryAdapter, mappingConfig.clone()).get();
		Resource resource = document.getSingleData().get();
		Assertions.assertEquals("2", resource.getId());
		Assertions.assertEquals("tasks", resource.getType());
		Assertions.assertEquals("http://something.else.net/api/tasks/3", getLinkText(resource.getLinks().get("self")));

		Relationship projectRel = resource.getRelationships().get("project");
		Assertions.assertEquals("http://something.else.net/api/tasks/3/relationships/project", getLinkText(projectRel.getLinks().get("self")));
	}


	@Test
	public void testJsonIncludeNonEmptyOnId() throws JsonProcessingException {
		Project project = new Project();
		project.setName("someProject");

		// not that id makes use of @JsonInclude.Include.NON_EMPTY
		project.setId(0L);

		QuerySpec querySpec = new QuerySpec(Project.class);
		QuerySpecAdapter queryAdapter = (QuerySpecAdapter) toAdapter(querySpec);

		mapper.setClient(true);

		Document document = mapper.toDocument(toResponse(project), queryAdapter, mappingConfig).get();
		Resource resource = document.getSingleData().get();
		Assertions.assertNull(resource.getId());

		ObjectWriter writer = container.getObjectMapper().writerFor(Resource.class);
		String json = writer.writeValueAsString(resource);
		Assertions.assertFalse(json.contains("\"id\""));
	}

	@Test
	public void testJsonIncludeNonEmptyIgnoresNull() throws JsonProcessingException {
		// note that desc and followup project make use of @JsonInclude.Include.NON_EMPTY
		Schedule schedule = new Schedule();
		schedule.setDesc(null);
		schedule.setFollowupProject(null);

		QuerySpec querySpec = new QuerySpec(Project.class);
		querySpec.includeRelation(PathSpec.of("followupProject"));
		QuerySpecAdapter queryAdapter = (QuerySpecAdapter) toAdapter(querySpec);

		Document document = mapper.toDocument(toResponse(schedule), queryAdapter, mappingConfig).get();
		Resource resource = document.getSingleData().get();

		Assertions.assertFalse(resource.getAttributes().containsKey("description"));
		Assertions.assertFalse(resource.getRelationships().containsKey("followup"));
	}


	@Test
	public void testJsonIncludeNonEmptyIgnoresEmptyList() throws JsonProcessingException {
		// makes use of @JsonInclude.Include.NON_EMPTY
		Schedule schedule = new Schedule();
		schedule.setKeywords(Collections.emptyList());
		schedule.setTasks(new ArrayList<>());

		QuerySpec querySpec = new QuerySpec(Project.class);
		querySpec.includeRelation(PathSpec.of("tasks"));
		QuerySpecAdapter queryAdapter = (QuerySpecAdapter) toAdapter(querySpec);

		Document document = mapper.toDocument(toResponse(schedule), queryAdapter, mappingConfig).get();
		Resource resource = document.getSingleData().get();

		Assertions.assertFalse(resource.getAttributes().containsKey("keywords"));
		Assertions.assertFalse(resource.getRelationships().containsKey("tasks"));
	}

	@Test
	public void testJsonIncludeNonEmptyWritesNonEmpty() throws JsonProcessingException {
		Project project = new Project();
		project.setId(12L);

		// note that desc and followup project make use of @JsonInclude.Include.NON_EMPTY
		Schedule schedule = new Schedule();
		schedule.setDesc("Hello");
		schedule.setFollowupProject(project);

		QuerySpec querySpec = new QuerySpec(Project.class);
		querySpec.includeRelation(PathSpec.of("followupProject"));
		QuerySpecAdapter queryAdapter = (QuerySpecAdapter) toAdapter(querySpec);

		Document document = mapper.toDocument(toResponse(schedule), queryAdapter, mappingConfig).get();
		Resource resource = document.getSingleData().get();

		Assertions.assertTrue(resource.getAttributes().containsKey("description"));
		Assertions.assertTrue(resource.getRelationships().containsKey("followup"));
	}

	@Test
	public void testOptionalNotSerializedIfEmpty() {
		Schedule schedule = new Schedule();
		schedule.setDueDate(Optional.empty());

		QuerySpecAdapter queryAdapter = (QuerySpecAdapter) toAdapter(new QuerySpec(Project.class));
		Document document = mapper.toDocument(toResponse(schedule), queryAdapter, mappingConfig).get();
		Resource resource = document.getSingleData().get();
		Assertions.assertFalse(resource.getAttributes().containsKey("dueDate"));
	}

	@Test
	public void testOptionalSerializedIfSet() {
		Schedule schedule = new Schedule();
		schedule.setDueDate(Optional.of(OffsetDateTime.now()));

		QuerySpecAdapter queryAdapter = (QuerySpecAdapter) toAdapter(new QuerySpec(Project.class));
		Document document = mapper.toDocument(toResponse(schedule), queryAdapter, mappingConfig).get();
		Resource resource = document.getSingleData().get();

		Assertions.assertTrue(resource.getAttributes().containsKey("dueDate"));
		JsonNode node = resource.getAttributes().get("dueDate");
		Assertions.assertTrue(node.asText().length() > 0);
	}


	@Test
	public void testCompactModeWithNullData() {
		QuerySpecAdapter queryAdapter = (QuerySpecAdapter) toAdapter(new QuerySpec(Task.class));

		queryAdapter.setCompactMode(true);
		Document compactDocument = mapper.toDocument(new JsonApiResponse(), queryAdapter, mappingConfig).get();
		queryAdapter.setCompactMode(false);
		Document standardDocument = mapper.toDocument(new JsonApiResponse(), queryAdapter, mappingConfig).get();

		// The only difference standard document only has is a self link
		standardDocument.setLinks(null);
		Assertions.assertEquals(standardDocument, compactDocument);
	}

	@Test
	public void testDocumentInformation() {
		Task task = createTask(2, "sample task");

		TestLinksInformation links = new TestLinksInformation();
		links.value = new DefaultLink("linksValue");

		TestMetaInformation meta = new TestMetaInformation();
		meta.value = "metaValue";

		JsonApiResponse response = toResponse(task);
		response.setMetaInformation(meta);
		response.setLinksInformation(links);

		Document document = mapper.toDocument(response, createAdapter(Task.class), mappingConfig).get();
		Assertions.assertEquals("linksValue", getLinkText(document.getLinks().get("value")));
		Assertions.assertEquals("metaValue", document.getMeta().get("value").asText());
	}

	@Test
	public void testResourceInformation() {
		TestLinksInformation links = new TestLinksInformation();
		links.value = new DefaultLink("linksValue");

		TestMetaInformation meta = new TestMetaInformation();
		meta.value = "metaValue";

		Task task = createTask(2, "sample task");
		task.setMetaInformation(meta);
		task.setLinksInformation(links);

		Document document = mapper.toDocument(toResponse(task), createAdapter(Task.class), mappingConfig).get();
		Resource resource = document.getSingleData().get();
		Assertions.assertEquals("linksValue", getLinkText(resource.getLinks().get("value")));
		Assertions.assertEquals("metaValue", resource.getMeta().get("value").asText());
	}

	@Test
	public void testErrors() {
		JsonApiResponse response = new JsonApiResponse();

		ErrorData error = Mockito.mock(ErrorData.class);
		response.setErrors(Arrays.asList(error));

		Document document = mapper.toDocument(response, createAdapter(Task.class), mappingConfig).get();
		List<ErrorData> errors = document.getErrors();
		Assertions.assertEquals(1, errors.size());
		Assertions.assertSame(error, errors.get(0));
	}

	@Test
	public void testRelationshipSingleValuedEager() {
		LazyTask task = createLazyTask(2);
		Project project = createProject(3, "sample project");
		task.setProject(project);

		Document document = mapper.toDocument(toResponse(task), createAdapter(Task.class), mappingConfig).get();
		Resource resource = document.getSingleData().get();
		Assertions.assertEquals("2", resource.getId());

		Relationship relationship = resource.getRelationships().get("project");
		Assertions.assertNotNull(relationship);
		ResourceIdentifier relationshipData = relationship.getSingleData().get();
		Assertions.assertNotNull(relationshipData);
		Assertions.assertEquals("3", relationshipData.getId());
		Assertions.assertEquals("projects", relationshipData.getType());

		Assertions.assertTrue(document.getIncluded().isEmpty());
	}

	@Test
	public void testRelationshipLazyMultiValued() {
		LazyTask task = createLazyTask(2);
		Project project1 = createProject(3, "sample project");
		Project project2 = createProject(4, "sample project");
		task.setProjects(Arrays.asList(project1, project2));

		Document document = mapper.toDocument(toResponse(task), createAdapter(Task.class), mappingConfig).get();
		Resource resource = document.getSingleData().get();
		Assertions.assertEquals("2", resource.getId());

		Relationship relationship = resource.getRelationships().get("projects");
		Assertions.assertNotNull(relationship);
		Nullable<List<ResourceIdentifier>> relationshipData = relationship.getCollectionData();
		Assertions.assertFalse(relationshipData.isPresent());
		Assertions.assertTrue(document.getIncluded().isEmpty());
	}

	@Test
	public void testRelationshipIncludeMultiValued() {
		LazyTask task = createLazyTask(2);
		Project project1 = createProject(3, "sample project3");
		Project project2 = createProject(4, "sample project4");
		task.setProjects(Arrays.asList(project1, project2));

		QuerySpec querySpec = new QuerySpec(LazyTask.class);
		querySpec.includeRelation(Arrays.asList("projects"));

		Document document = mapper.toDocument(toResponse(task), toAdapter(querySpec), mappingConfig).get();
		Resource resource = document.getSingleData().get();
		Assertions.assertEquals("2", resource.getId());

		Relationship relationship = resource.getRelationships().get("projects");
		Assertions.assertNotNull(relationship);
		List<ResourceIdentifier> relationshipData = relationship.getCollectionData().get();
		Assertions.assertNotNull(relationshipData);
		Assertions.assertEquals(2, relationshipData.size());
		Assertions.assertEquals("3", relationshipData.get(0).getId());
		Assertions.assertEquals("projects", relationshipData.get(0).getType());
		Assertions.assertEquals("4", relationshipData.get(1).getId());
		Assertions.assertEquals("projects", relationshipData.get(1).getType());

		Assertions.assertFalse(document.getIncluded().isEmpty());

		List<Resource> included = document.getIncluded();
		Assertions.assertEquals(2, included.size());
		Assertions.assertEquals("3", included.get(0).getId());
		Assertions.assertEquals("projects", included.get(0).getType());
		Assertions.assertEquals("sample project3", included.get(0).getAttributes().get("name").asText());
		Assertions.assertEquals("4", included.get(1).getId());
		Assertions.assertEquals("projects", included.get(1).getType());
		Assertions.assertEquals("sample project4", included.get(1).getAttributes().get("name").asText());
	}

	@Test
	public void testRelationshipIncludeRelation() {
		LazyTask task = createLazyTask(2);
		Project project = createProject(3, "sample project");
		task.setProject(project);

		QuerySpec querySpec = new QuerySpec(LazyTask.class);
		querySpec.includeRelation(Arrays.asList("project"));

		Document document = mapper.toDocument(toResponse(task), toAdapter(querySpec), mappingConfig).get();
		Resource resource = document.getSingleData().get();
		Assertions.assertEquals("2", resource.getId());

		Relationship relationship = resource.getRelationships().get("project");
		Assertions.assertNotNull(relationship);
		ResourceIdentifier relationshipData = relationship.getSingleData().get();
		Assertions.assertNotNull(relationshipData);
		Assertions.assertEquals("3", relationshipData.getId());
		Assertions.assertEquals("projects", relationshipData.getType());

		List<Resource> included = document.getIncluded();
		Assertions.assertEquals(1, included.size());
		Assertions.assertEquals("3", included.get(0).getId());
		Assertions.assertEquals("projects", included.get(0).getType());
		Assertions.assertEquals("sample project", included.get(0).getAttributes().get("name").asText());
	}

	@Test
	public void testRelationshipCyclicInclusion() {
		Task task = createTask(2, "sample task");
		Project project = createProject(3, "sample project");
		task.setProject(project);
		project.setTask(task);

		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.includeRelation(Arrays.asList("project"));
		querySpec.getOrCreateQuerySpec(Project.class).includeRelation(Arrays.asList("task"));

		Document document = mapper.toDocument(toResponse(task), toAdapter(querySpec), mappingConfig).get();
		Resource resource = document.getSingleData().get();
		Assertions.assertEquals("2", resource.getId());

		Relationship relationship = resource.getRelationships().get("project");
		Assertions.assertNotNull(relationship);
		ResourceIdentifier relationshipData = relationship.getSingleData().get();
		Assertions.assertNotNull(relationshipData);
		Assertions.assertEquals("3", relationshipData.getId());
		Assertions.assertEquals("projects", relationshipData.getType());

		List<Resource> included = document.getIncluded();
		Assertions.assertEquals(1, included.size());
		Assertions.assertEquals("3", included.get(0).getId());
		Assertions.assertEquals("projects", included.get(0).getType());
		Assertions.assertEquals("sample project", included.get(0).getAttributes().get("name").asText());
		Assertions.assertEquals("2", included.get(0).getRelationships().get("task").getSingleData().get().getId());
	}

	@Test
	public void testRelationshipSingleValuedIncludeByDefault() {
		Task task = createTask(2, "sample task");
		Project project = createProject(3, "sample project");
		task.setProject(project);

		Document document = mapper.toDocument(toResponse(task), createAdapter(Task.class), mappingConfig).get();
		Resource resource = document.getSingleData().get();
		Assertions.assertEquals("2", resource.getId());
		Assertions.assertEquals("tasks", resource.getType());
		Assertions.assertEquals("sample task", resource.getAttributes().get("name").asText());

		Relationship relationship = resource.getRelationships().get("project");
		Assertions.assertNotNull(relationship);
		Assertions.assertEquals("http://127.0.0.1/tasks/2/relationships/project",
				getLinkText(relationship.getLinks().get("self")));
		Assertions.assertEquals("http://127.0.0.1/tasks/2/project", getLinkText(relationship.getLinks().get("related")));
		ResourceIdentifier relationshipData = relationship.getSingleData().get();
		Assertions.assertNotNull(relationshipData);
		Assertions.assertEquals("3", relationshipData.getId());
		Assertions.assertEquals("projects", relationshipData.getType());

		List<Resource> included = document.getIncluded();
		Assertions.assertEquals(1, included.size());
		Assertions.assertEquals("3", included.get(0).getId());
		Assertions.assertEquals("projects", included.get(0).getType());
		Assertions.assertEquals("sample project", included.get(0).getAttributes().get("name").asText());
	}

	@Test
	public void testMultipleInclusions() {
		Task task1 = createTask(1, "other task");
		Task task2 = createTask(2, "other task");
		Task task3 = createTask(3, "sample task");

		Project project = new Project();
		project.setName("someProject");
		project.setId(3L);
		project.setTask(task1);
		project.setTasks(Arrays.asList(task2, task3));

		QuerySpec querySpec = new QuerySpec(Project.class);
		querySpec.includeRelation(Arrays.asList("tasks"));
		querySpec.includeRelation(Arrays.asList("task"));
		QuerySpecAdapter queryAdapter = (QuerySpecAdapter) toAdapter(querySpec);

		Document document = mapper.toDocument(toResponse(project), queryAdapter, new DocumentMappingConfig()).get();
		Resource resource = document.getSingleData().get();
		Assertions.assertEquals("3", resource.getId());
		Assertions.assertEquals("projects", resource.getType());

		Relationship taskRel = resource.getRelationships().get("task");
		Assertions.assertNotNull(taskRel.getLinks());
		Assertions.assertTrue(taskRel.getData().isPresent());
		Assertions.assertNotNull(taskRel.getData().get());

		Relationship tasksRel = resource.getRelationships().get("tasks");
		Assertions.assertNotNull(tasksRel.getLinks());
		Assertions.assertTrue(tasksRel.getData().isPresent());
		Assertions.assertNotNull(tasksRel.getData().get());
		Assertions.assertEquals(2, tasksRel.getCollectionData().get().size());

		Assertions.assertEquals(3, document.getIncluded().size());
	}

	@Test
	public void testConvergingInclusionPaths() {
		Task task1 = createTask(1, "other task");
		Task task2 = createTask(2, "other task");

		Project project1 = new Project();
		project1.setName("someProject");
		project1.setId(3L);
		project1.setTasks(Arrays.asList(task1, task2));

		Project project2 = new Project();
		project2.setName("someProject");
		project2.setId(2L);

		task1.setProject(project1);
		task1.setProjectsInit(Arrays.asList(project2));
		task1.setLinksInformation(new DefaultSelfLinksInformation());

		// come back/converge to same task
		project1.setTask(task2);
		project2.setTask(task2);

		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.includeRelation(Arrays.asList("project", "task"));
		querySpec.includeRelation(Arrays.asList("projectsInit", "task"));
		QuerySpecAdapter queryAdapter = (QuerySpecAdapter) toAdapter(querySpec);

		Document document = mapper.toDocument(toResponse(task1), queryAdapter, new DocumentMappingConfig()).get();
		Resource resource = document.getSingleData().get();
		Assertions.assertEquals("1", resource.getId());
		Assertions.assertEquals("tasks", resource.getType());

		Relationship projectRel = resource.getRelationships().get("project");
		Assertions.assertNotNull(projectRel.getLinks());
		Assertions.assertTrue(projectRel.getData().isPresent());
		Assertions.assertNotNull(projectRel.getData().get());

		Relationship projectsRel = resource.getRelationships().get("projectsInit");
		Assertions.assertNotNull(projectsRel.getLinks());
		Assertions.assertTrue(projectsRel.getData().isPresent());
		Assertions.assertNotNull(projectsRel.getData().get());
		Assertions.assertEquals(1, projectsRel.getCollectionData().get().size());

		Assertions.assertEquals(3, document.getIncluded().size());

		List<Resource> included = document.getIncluded();
		Resource projectResource2 = included.get(0);
		Resource projectResource3 = included.get(1);

		Assertions.assertTrue(projectResource2.getRelationships().get("task").getData().isPresent());
		Assertions.assertTrue(projectResource3.getRelationships().get("task").getData().isPresent());
	}

	@Test
	public void testRelationshipSingleValuedLazy() {
		LazyTask task = createLazyTask(2);
		Project project = createProject(3, "sample project");
		task.setLazyProject(project);

		Document document = mapper.toDocument(toResponse(task), createAdapter(Task.class), mappingConfig).get();
		Resource resource = document.getSingleData().get();
		Assertions.assertEquals("2", resource.getId());
		Assertions.assertEquals("lazy_tasks", resource.getType());

		Relationship relationship = resource.getRelationships().get("lazyProject");
		Assertions.assertNotNull(relationship);
		Assertions.assertEquals("http://127.0.0.1/lazy_tasks/2/relationships/lazyProject",
				getLinkText(relationship.getLinks().get("self")));
		Assertions.assertEquals("http://127.0.0.1/lazy_tasks/2/lazyProject",
				getLinkText(relationship.getLinks().get("related")));
		Nullable<ResourceIdentifier> relationshipData = relationship.getSingleData();
		Assertions.assertFalse(relationshipData.isPresent());
		Assertions.assertTrue(document.getIncluded().isEmpty());
	}

	@Test
	public void testAttributesSelection() {
		Task task = createTask(2, "sample task");
		task.setCategory("sample category");
		task.setProject(new Project());
		JsonApiResponse response = new JsonApiResponse();
		response.setEntity(task);

		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.includeField(Arrays.asList("category"));

		Document document = mapper.toDocument(response, toAdapter(querySpec), mappingConfig).get();
		Resource resource = document.getSingleData().get();
		Assertions.assertEquals("2", resource.getId());
		Assertions.assertEquals("tasks", resource.getType());
		Assertions.assertNull(resource.getAttributes().get("name"));
		Assertions.assertNull(resource.getRelationships().get("project"));
		Assertions.assertEquals("sample category", resource.getAttributes().get("category").asText());
	}

	@Test
	public void testAttributesOrdering() {
		Task task = createTask(3, "sample task");
		task.setCategory("sample category");
		task.setName("sample name");
		JsonApiResponse response = new JsonApiResponse();
		response.setEntity(task);

		Document document = mapper.toDocument(response, createAdapter(Task.class), mappingConfig).get();
		Resource resource = document.getSingleData().get();
		Assertions.assertEquals("3", resource.getId());
		Assertions.assertEquals("tasks", resource.getType());
		// check if the attributes are returned in alphabetical order as per the JsonPropertyOrder on Task
		Assertions.assertTrue(resource.getAttributes() instanceof LinkedHashMap);
		Assertions.assertEquals(resource.getAttributes().keySet(),
				Stream.of("category", "completed", "deleted", "name", "otherTasks", "readOnlyValue", "status")
						.collect(Collectors.toCollection(LinkedHashSet::new)));
		Assertions.assertEquals("sample name", resource.getAttributes().get("name").asText());
		Assertions.assertEquals("sample category", resource.getAttributes().get("category").asText());
	}

	private Project createProject(long id, String name) {
		Project project = new Project();
		project.setId(id);
		project.setName(name);
		return project;
	}

	private Task createTask(long id, String name) {
		Task task = new Task();
		task.setId(id);
		task.setName(name);
		task.setLinksInformation(new DefaultSelfLinksInformation());
		return task;
	}

	private LazyTask createLazyTask(long id) {
		LazyTask task = new LazyTask();
		task.setId(id);
		return task;
	}

	public static class TestLinksInformation implements LinksInformation {

		public Link value;

		public Link getValue() {
			return value;
		}

		// used to test the LinksInformationSerializer -> should not be serialized
		@JsonIgnore
		public Link getOtherValue() {
			return null;
		}
	}

	public static class TestMetaInformation implements MetaInformation {

		public String value;

		public String getValue() {
			return value;
		}
	}

}
