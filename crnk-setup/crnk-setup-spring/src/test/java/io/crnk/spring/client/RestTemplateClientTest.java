package io.crnk.spring.client;

import io.crnk.client.CrnkClient;
import io.crnk.client.http.HttpAdapterListener;
import io.crnk.client.http.HttpAdapterRequest;
import io.crnk.client.http.HttpAdapterResponse;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.links.SelfLinksInformation;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.JsonLinksInformation;
import io.crnk.spring.app.TestConfiguration;
import io.crnk.test.mock.TestModule;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.repository.ScheduleRepository;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
public class RestTemplateClientTest {

    @Value("${local.server.port}")
    private int port;

    private RestTemplateAdapterListener listener;

    private CrnkClient client;

    protected ResourceRepository<Task, Long> taskRepo;

    protected ResourceRepository<Project, Long> projectRepo;

    protected RelationshipRepository<Task, Long, Project, Long> relRepo;

    protected RelationshipRepository<Schedule, Long, Task, Long> scheduleTaskRepo;

    protected RelationshipRepository<Task, Long, Schedule, Long> taskScheduleRepo;

    private HttpAdapterListener adapterListener;

    @BeforeEach
    public void setupClient() {
        client = new CrnkClient("http://127.0.0.1:" + port);
        client.findModules();

        RestTemplateAdapter adapter = RestTemplateAdapter.newInstance();
        adapter.setReceiveTimeout(30000, TimeUnit.MILLISECONDS);
        adapterListener = Mockito.spy(new HttpAdapterListener() {
            @Override
            public void onRequest(HttpAdapterRequest request) {
                request.header("X-TEST", "Hello");
            }

            @Override
            public void onResponse(HttpAdapterRequest request, HttpAdapterResponse response) {
            }
        });
        adapter.addListener(adapterListener);
        listener = Mockito.mock(RestTemplateAdapterListener.class);
        adapter.addListener(listener);
        adapter.addListener(new RestTemplateAdapterListenerBase());
        adapter.getImplementation().setRequestFactory(new OkHttp3ClientHttpRequestFactory());
        client.setHttpAdapter(adapter);

        taskRepo = client.getRepositoryForType(Task.class);
        projectRepo = client.getRepositoryForType(Project.class);
        relRepo = client.getRepositoryForType(Task.class, Project.class);
        scheduleTaskRepo = client.getRepositoryForType(Schedule.class, Task.class);
        taskScheduleRepo = client.getRepositoryForType(Task.class, Schedule.class);

        TestModule.clear();
    }

    @AfterEach
    public void tearTown() {
        TestModule.clear();
    }

    @Test
    public void testListenerInvoked() {
        ResourceList<Task> results = taskRepo.findAll(new QuerySpec(Task.class));

        ArgumentCaptor<RestTemplate> captor = ArgumentCaptor.forClass(RestTemplate.class);
        Mockito.verify(listener, Mockito.times(1)).onBuild(captor.capture());

        // links manipulated by test header sent along by http listener
        JsonLinksInformation links = (JsonLinksInformation) results.getLinks();
        String modifiedUrl = links.as(SelfLinksInformation.class).getSelf().getHref();
        Assertions.assertEquals("Hello", modifiedUrl);
    }

    @Test
    public void testCreate() {
        ScheduleRepository scheduleRepository = client.getRepositoryForInterface(ScheduleRepository.class);

        Schedule schedule = new Schedule();
        schedule.setName("mySchedule");
        scheduleRepository.create(schedule);

        QuerySpec querySpec = new QuerySpec(Schedule.class);
        ScheduleRepository.ScheduleList list = scheduleRepository.findAll(querySpec);
        Assertions.assertEquals(1, list.size());
        schedule = list.get(0);
        Assertions.assertNotNull(schedule.getId());
        ScheduleRepository.ScheduleListMeta meta = list.getMeta();
        ScheduleRepository.ScheduleListLinks links = list.getLinks();
        Assertions.assertNotNull(meta);
        Assertions.assertNotNull(links);
    }

    @Test
    public void testFindEmpty() {
        QuerySpec querySpec = new QuerySpec(Task.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, "doesNotExist"));
        List<Task> tasks = taskRepo.findAll(querySpec);
        Assertions.assertTrue(tasks.isEmpty());
    }

    @Test
    public void testFindNull() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            taskRepo.findOne(123422L, new QuerySpec(Task.class));
		});
    }

    @Test
    public void testCreateAndFind() {
        Task task = new Task();
        task.setId(1L);
        task.setName("test");
        taskRepo.create(task);

        // check retrievable with findAll
        List<Task> tasks = taskRepo.findAll(new QuerySpec(Task.class));
        Assertions.assertEquals(1, tasks.size());
        Task savedTask = tasks.get(0);
        Assertions.assertEquals(task.getId(), savedTask.getId());
        Assertions.assertEquals(task.getName(), savedTask.getName());

        // check retrievable with findAll(ids)
        tasks = taskRepo.findAll(Arrays.asList(1L), new QuerySpec(Task.class));
        Assertions.assertEquals(1, tasks.size());
        savedTask = tasks.get(0);
        Assertions.assertEquals(task.getId(), savedTask.getId());
        Assertions.assertEquals(task.getName(), savedTask.getName());

        // check retrievable with findOne
        savedTask = taskRepo.findOne(1L, new QuerySpec(Task.class));
        Assertions.assertEquals(task.getId(), savedTask.getId());
        Assertions.assertEquals(task.getName(), savedTask.getName());
    }

    @Test
    public void testUpdate() {
        final List<String> methods = new ArrayList<>();
        final List<String> paths = new ArrayList<>();
        final Interceptor interceptor = new Interceptor() {

            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();

                methods.add(request.method());
                paths.add(request.url().encodedPath());

                return chain.proceed(request);
            }
        };

        Task task = new Task();
        task.setId(1L);
        task.setName("test");
        taskRepo.create(task);

        Task savedTask = taskRepo.findOne(1L, new QuerySpec(Task.class));
        Assertions.assertNotNull(savedTask);

        // perform update
        task.setName("updatedName");
        taskRepo.save(task);

        // check updated
        savedTask = taskRepo.findOne(1L, new QuerySpec(Task.class));
        Assertions.assertNotNull(savedTask);
        Assertions.assertEquals("updatedName", task.getName());
    }

    @Test
    public void testDelete() {
        Task task = new Task();
        task.setId(1L);
        task.setName("test");
        taskRepo.create(task);

        taskRepo.delete(1L);

        List<Task> tasks = taskRepo.findAll(new QuerySpec(Task.class));
        Assertions.assertEquals(0, tasks.size());
    }

}
