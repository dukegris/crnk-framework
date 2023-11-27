package io.crnk.example.springboot.simple;

import java.io.Serializable;
import java.util.Arrays;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.crnk.client.CrnkClient;
import io.crnk.client.http.HttpAdapter;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.example.springboot.microservice.MicroServiceApplication;
import io.crnk.example.springboot.microservice.project.Project;
import io.crnk.example.springboot.microservice.task.Task;
import io.crnk.testkit.RandomWalkLinkChecker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

public class MicroServiceApplicationTest {

	private ConfigurableApplicationContext projectApp;

	private ConfigurableApplicationContext taskApp;

	private CrnkClient taskClient;

	@BeforeEach
	public void setup() {
		projectApp = MicroServiceApplication.startProjectApplication();
		taskApp = MicroServiceApplication.startTaskApplication();

		String url = "http://127.0.0.1:" + MicroServiceApplication.TASK_PORT;
		taskClient = new CrnkClient(url);
		RestAssured.baseURI = url;
	}

	@Test
	public void test() {
		checkInclusionOfRemoteResource();
		checkRemoteProjectNotExposedInHome();
		checkRemoteProjectNotExposed();
		checkRandomWalk();
	}

	private void checkRandomWalk() {
		HttpAdapter httpAdapter = taskClient.getHttpAdapter();

		RandomWalkLinkChecker linkChecker = new RandomWalkLinkChecker(httpAdapter);
		linkChecker.setWalkLength(100);
		linkChecker.addStartUrl(taskClient.getServiceUrlProvider().getUrl());
		linkChecker.performCheck();
	}

	private void checkInclusionOfRemoteResource() {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.setLimit(10L);
		querySpec.includeRelation(Arrays.asList("project"));

		ResourceRepository<Task, Serializable> repository = taskClient.getRepositoryForType(Task.class);
		ResourceList<Task> tasks = repository.findAll(querySpec);
		Assertions.assertNotEquals(0, tasks.size());
		for (Task task : tasks) {
			Assertions.assertEquals("http://127.0.0.1:12001/task/" + task.getId(), task.getLinks().getSelf().getHref());
			Project project = task.getProject();
			Assertions.assertNotNull(task.getProject());
			Assertions.assertEquals("http://127.0.0.1:12002/project/" + project.getId(), project.getLinks().getSelf().getHref());
		}
	}

	private void checkRemoteProjectNotExposedInHome() {
		Response response = RestAssured.given().when().get("/");
		response.then().assertThat().statusCode(200);
		String body = response.getBody().print();
		Assertions.assertTrue(body.contains("/task"), body);
		Assertions.assertTrue(!body.contains("/project"), body);
	}

	private void checkRemoteProjectNotExposed() {
		Response response = RestAssured.given().when().get("/project");
		response.then().assertThat().statusCode(404);
	}

	@AfterEach
	public void tearDown() {
		projectApp.close();
		taskApp.close();
	}
}
