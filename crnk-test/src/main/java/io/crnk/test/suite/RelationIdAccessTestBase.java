package io.crnk.test.suite;

import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.RelationIdTestResource;
import io.crnk.test.mock.models.Schedule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Checks support for @JsonApiRelationId
 */
public abstract class RelationIdAccessTestBase {

	protected TestContainer testContainer;

	protected ResourceRepository<Project, Long> projectRepo;

	protected ResourceRepository<Schedule, Long> scheduleRepo;

	protected RelationshipRepository<Schedule, Long, Project, Long> relRepo;

	private Project project;

	private Project project2;

	@BeforeEach
	public void setup() {
		testContainer.start();
		scheduleRepo = testContainer.getRepositoryForType(Schedule.class);
		projectRepo = testContainer.getRepositoryForType(Project.class);
		relRepo = testContainer.getRepositoryForType(Schedule.class, Project.class);
	}

	@AfterEach
	public void tearDown() {
		testContainer.stop();
	}

	@Test
	public void checkCrud() {
		Schedule schedule = checkPost();
		checkFindWithoutInclusion(schedule);
		checkFindWithInclusion(schedule);
		schedule = checkPatch(schedule);
		checkPatchRelationship(schedule);
	}

	@Test
	public void checkResourceIdentifierField() {
		Schedule schedule = new Schedule();
		schedule.setId(13L);
		schedule.setName("mySchedule");
		Schedule savedSchedule = scheduleRepo.create(schedule);

		RelationIdTestResource resource = new RelationIdTestResource();
		resource.setId(14L);
		resource.setName("test");
		resource.setTestResourceIdRefId(new ResourceIdentifier("13", "schedule"));

		ResourceRepository<RelationIdTestResource, Serializable> repository =
				testContainer.getRepositoryForType(RelationIdTestResource.class);
		RelationIdTestResource createdResource = repository.create(resource);
		Assertions.assertEquals(resource.getTestResourceIdRefId(), createdResource.getTestResourceIdRefId());

		RelationIdTestResource serverResource = testContainer.getTestData(RelationIdTestResource.class, 14L);
		Assertions.assertEquals(resource.getTestResourceIdRefId(), serverResource.getTestResourceIdRefId());

		QuerySpec querySpec = new QuerySpec(RelationIdTestResource.class);
		RelationIdTestResource getResource = repository.findOne(14L, querySpec);
		Assertions.assertNull(getResource.getTestResourceIdRefId());
		Assertions.assertNull(createdResource.getTestResourceIdRef());

		querySpec = new QuerySpec(RelationIdTestResource.class);
		querySpec.includeRelation(Arrays.asList("testResourceIdRef"));
		getResource = repository.findOne(14L, querySpec);
		Assertions.assertEquals(resource.getTestResourceIdRefId(), getResource.getTestResourceIdRefId());
		Assertions.assertNotNull(getResource.getTestResourceIdRef());
	}

	private void checkFindWithoutInclusion(Schedule schedule) {
		QuerySpec querySpec = new QuerySpec(Schedule.class);
		Schedule foundSchedule = scheduleRepo.findOne(schedule.getId(), querySpec);
		Assertions.assertEquals(project.getId(), foundSchedule.getProjectId());
		Assertions.assertNull(foundSchedule.getProject());

		Assertions.assertEquals(1, foundSchedule.getProjectIds().size());
		Assertions.assertEquals(project.getId(), foundSchedule.getProjectIds().get(0));
		Assertions.assertNotNull(foundSchedule.getProjects());

		// TODO list should contain proxies in the future
		List<Project> projects = foundSchedule.getProjects();
		Assertions.assertEquals(1, projects.size());
		Assertions.assertNull(projects.get(0).getName()); // not initialized, id-only
	}

	private void checkFindWithInclusion(Schedule schedule) {
		QuerySpec querySpec = new QuerySpec(Schedule.class);
		querySpec.includeRelation(Arrays.asList("project"));
		querySpec.includeRelation(Arrays.asList("projects"));
		Schedule foundSchedule = scheduleRepo.findOne(schedule.getId(), querySpec);
		Assertions.assertEquals(project.getId(), foundSchedule.getProjectId());
		Assertions.assertNotNull(foundSchedule.getProject());
		Assertions.assertEquals(project.getId(), foundSchedule.getProject().getId());


		Assertions.assertEquals(1, foundSchedule.getProjectIds().size());
		Assertions.assertEquals(1, foundSchedule.getProjects().size());
		Assertions.assertEquals(project.getId(), foundSchedule.getProjectIds().get(0));
		Assertions.assertEquals(project.getId(), foundSchedule.getProjects().get(0).getId());
	}

	private Schedule checkPost() {
		project = new Project();
		project.setName("myProject");
		project = projectRepo.create(project);

		Schedule schedule = new Schedule();
		schedule.setId(1L);
		schedule.setName("mySchedule");
		schedule.setProjectId(project.getId());
		schedule.setProjectIds(Arrays.asList(project.getId()));
		Schedule savedSchedule = scheduleRepo.create(schedule);

		Schedule serverSchedule = testContainer.getTestData(Schedule.class, 1L);
		Assertions.assertEquals(project.getId(), serverSchedule.getProjectId());
		Assertions.assertEquals(1, serverSchedule.getProjectIds().size());
		Assertions.assertEquals(project.getId(), serverSchedule.getProjectIds().get(0));
		Assertions.assertNull(serverSchedule.getProject());

		Assertions.assertNotSame(schedule, savedSchedule);
		Assertions.assertEquals(project.getId(), savedSchedule.getProjectId());
		Assertions.assertEquals(1, savedSchedule.getProjectIds().size());
		return savedSchedule;
	}

	private Schedule checkPatch(Schedule schedule) {
		project2 = new Project();
		project2.setName("myProject2");
		project2 = projectRepo.create(project2);

		schedule.setProjectId(project2.getId());
		schedule.setProjectIds(Arrays.asList(project.getId(), project2.getId()));
		Schedule savedSchedule = scheduleRepo.save(schedule);

		Schedule serverSchedule = testContainer.getTestData(Schedule.class, 1L);
		Assertions.assertEquals(project2.getId(), serverSchedule.getProjectId());
		Assertions.assertEquals(2, serverSchedule.getProjectIds().size());
		Assertions.assertEquals(project.getId(), serverSchedule.getProjectIds().get(0));
		Assertions.assertEquals(project2.getId(), serverSchedule.getProjectIds().get(1));
		Assertions.assertNull(serverSchedule.getProject());

		Assertions.assertNotSame(schedule, savedSchedule);
		Assertions.assertEquals(project2.getId(), savedSchedule.getProjectId());
		Assertions.assertEquals(2, savedSchedule.getProjectIds().size());
		return savedSchedule;
	}

	private Schedule checkPatchRelationship(Schedule schedule) {

		RelationshipRepository<Schedule, Serializable, Project, Serializable> relRepository =
				testContainer.getRepositoryForType(Schedule.class, Project.class);

		relRepository.setRelation(schedule, project.getId(), "project");
		schedule = scheduleRepo.findOne(schedule.getId(), new QuerySpec(Schedule.class));
		Assertions.assertEquals(project.getId(), schedule.getProjectId());

		relRepository.setRelation(schedule, project2.getId(), "project");
		schedule = scheduleRepo.findOne(schedule.getId(), new QuerySpec(Schedule.class));
		Assertions.assertEquals(project2.getId(), schedule.getProjectId());

		relRepository.setRelations(schedule, Collections.emptyList(), "projects");
		schedule = scheduleRepo.findOne(schedule.getId(), new QuerySpec(Schedule.class));
		Assertions.assertEquals(Collections.emptyList(), schedule.getProjectIds());

		relRepository.addRelations(schedule, Arrays.asList(project.getId()), "projects");
		schedule = scheduleRepo.findOne(schedule.getId(), new QuerySpec(Schedule.class));
		Assertions.assertEquals(Arrays.asList(project.getId()), schedule.getProjectIds());

		relRepository.addRelations(schedule, Arrays.asList(project2.getId()), "projects");
		schedule = scheduleRepo.findOne(schedule.getId(), new QuerySpec(Schedule.class));
		Assertions.assertEquals(Arrays.asList(project.getId(), project2.getId()), schedule.getProjectIds());

		relRepository.removeRelations(schedule, Arrays.asList(project.getId()), "projects");
		schedule = scheduleRepo.findOne(schedule.getId(), new QuerySpec(Schedule.class));
		Assertions.assertEquals(Arrays.asList(project2.getId()), schedule.getProjectIds());

		return schedule;
	}

}
