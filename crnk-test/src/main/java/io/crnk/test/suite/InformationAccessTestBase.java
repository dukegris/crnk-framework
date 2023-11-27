package io.crnk.test.suite;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Project.ProjectLinks;
import io.crnk.test.mock.models.Project.ProjectMeta;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.repository.ProjectRepository.ProjectsLinksInformation;
import io.crnk.test.mock.repository.ProjectRepository.ProjectsMetaInformation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class InformationAccessTestBase {

	protected TestContainer testContainer;

	protected ResourceRepository<Task, Long> taskRepo;

	protected ResourceRepository<Project, Long> projectRepo;

	@BeforeEach
	public void setup() {
		testContainer.start();
		taskRepo = testContainer.getRepositoryForType(Task.class);
		projectRepo = testContainer.getRepositoryForType(Project.class);

		Project project = new Project();
		project.setId(14L);
		projectRepo.create(project);
	}

	@AfterEach
	public void tearDown() {
		testContainer.stop();
	}


	@Test
	public void testMeta() {
		QuerySpec querySpec = new QuerySpec(Project.class);
		ResourceList<Project> list = projectRepo.findAll(querySpec);
		ProjectsMetaInformation metaInformation = list.getMeta(ProjectsMetaInformation.class);
		Assertions.assertEquals("testMeta", metaInformation.getMetaValue());
		Project project = list.get(0);
		ProjectMeta projectMeta = project.getMeta();
		Assertions.assertNotNull(projectMeta);
		Assertions.assertEquals("someMetaValue", projectMeta.getValue());
	}

	@Test
	public void checkEchoMetaOnPost() {
		ProjectMeta meta = new ProjectMeta();
		meta.setValue("metaValue...");

		Project project = new Project();
		project.setName("test");
		project.setMeta(meta);

		Project createdProject = projectRepo.create(project);
		Assertions.assertEquals(project.getName(), createdProject.getName());
		ProjectMeta createdMeta = createdProject.getMeta();
		Assertions.assertEquals(meta.getValue(), createdMeta.getValue());
	}

	@Test
	public void checkEchoMetaOnPatch() {
		ProjectMeta meta = new ProjectMeta();
		meta.setValue("post...");

		Project project = new Project();
		project.setName("test");
		project.setMeta(meta);

		project = projectRepo.create(project);
		project.getMeta().setValue("patch...");

		Project updatedProject = projectRepo.save(project);
		Assertions.assertEquals(project.getName(), updatedProject.getName());
		ProjectMeta updatedMeta = updatedProject.getMeta();
		Assertions.assertEquals("patch...", updatedMeta.getValue());
	}

	@Test
	public void testLinks() {
		QuerySpec querySpec = new QuerySpec(Project.class);
		querySpec.setLimit(1L);
		ResourceList<Project> list = projectRepo.findAll(querySpec);
		ProjectsLinksInformation lnksInformation = list.getLinks(ProjectsLinksInformation.class);
		Assertions.assertEquals("testLink", lnksInformation.getLinkValue().getHref());
		Project project = list.get(0);
		ProjectLinks projectLinks = project.getLinks();
		Assertions.assertNotNull(projectLinks);
		Assertions.assertEquals("someLinkValue", projectLinks.getValue().getHref());
	}

	@Test
	public void checkEchoLinksOnPost() {
		ProjectLinks links = new ProjectLinks();
		links.setValue("linksValue...");

		Project project = new Project();
		project.setName("test");
		project.setLinks(links);

		Project createdProject = projectRepo.create(project);
		Assertions.assertEquals(project.getName(), createdProject.getName());
		ProjectLinks createdLinks = createdProject.getLinks();
		Assertions.assertEquals("linksValue...", createdLinks.getValue().getHref());
	}

	@Test
	public void checkEchoLinksOnPatch() {
		ProjectLinks meta = new ProjectLinks();
		meta.setValue("post...");

		Project project = new Project();
		project.setName("test");
		project.setLinks(meta);

		project = projectRepo.create(project);
		project.getLinks().setValue("patch...");

		Project updatedProject = projectRepo.save(project);
		Assertions.assertEquals(project.getName(), updatedProject.getName());
		ProjectLinks updatedLinks = updatedProject.getLinks();
		Assertions.assertEquals("patch...", updatedLinks.getValue().getHref());
	}
}