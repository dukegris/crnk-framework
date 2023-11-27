package io.crnk.servlet.reactive;

import io.crnk.client.ClientException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.servlet.AsyncCrnkServlet;
import io.crnk.servlet.reactive.model.SlowResourceRepository;
import io.crnk.servlet.reactive.model.SlowTask;
import io.crnk.servlet.resource.ReactiveServletTestApplication;
import io.crnk.servlet.resource.ReactiveServletTestContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.Serializable;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ReactiveServletTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
public class AsyncServletTimeoutTest {

	@Autowired
	private ReactiveServletTestContainer testContainer;

	@Autowired
	private SlowResourceRepository slowResourceRepository;

	@Autowired
	private AsyncCrnkServlet servlet;


	@Test
	public void testNoTimeout() {
		servlet.setTimeout(Duration.ofMillis(2000));
		slowResourceRepository.setDelay(100);

		ResourceRepository<SlowTask, Serializable> repository = testContainer.getRepositoryForType(SlowTask.class);
		QuerySpec querySpec = new QuerySpec(SlowTask.class);
		ResourceList<SlowTask> list = repository.findAll(querySpec);
		Assertions.assertEquals(1, list.size());
	}

	@Test
	public void testTimeout() {
		servlet.setTimeout(Duration.ofMillis(2000));
		slowResourceRepository.setDelay(4000);

		ResourceRepository<SlowTask, Serializable> repository = testContainer.getRepositoryForType(SlowTask.class);
		QuerySpec querySpec = new QuerySpec(SlowTask.class);
		try {
			repository.findAll(querySpec);
			Assertions.fail();
		} catch (ClientException e) {
			Assertions.assertTrue(e.getCause() instanceof TimeoutException);
		}
	}
}
