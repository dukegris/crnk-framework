package io.crnk.client.suite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.crnk.client.CrnkClient;
import io.crnk.client.http.HttpAdapter;
import io.crnk.client.http.HttpAdapterListener;
import io.crnk.client.http.HttpAdapterRequest;
import io.crnk.client.http.HttpAdapterResponse;
import io.crnk.client.http.okhttp.OkHttpAdapter;
import io.crnk.client.http.okhttp.OkHttpAdapterListener;
import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.repository.ScheduleRepository;
import io.crnk.test.mock.repository.ScheduleRepository.ScheduleList;
import io.crnk.test.mock.repository.ScheduleRepository.ScheduleListLinks;
import io.crnk.test.mock.repository.ScheduleRepository.ScheduleListMeta;
import io.crnk.test.suite.BasicRepositoryAccessTestBase;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class RepositoryAccessClientTest extends BasicRepositoryAccessTestBase {

    private HttpAdapterListener listener;

    public RepositoryAccessClientTest() {
        ClientTestContainer testContainer = new ClientTestContainer();
        testContainer.setClientSetupConsumer(this::setupClient);
        this.testContainer = testContainer;
    }

    @BeforeAll
    public static void prepare() {
        ClientTestContainer.prepare();
    }

    protected void setupClient(CrnkClient client) {
    }

    @Test
    public void testGetters() {
        Assertions.assertEquals(Task.class, taskRepo.getResourceClass());
        Assertions.assertEquals(Task.class, relRepo.getSourceResourceClass());
        Assertions.assertEquals(Project.class, relRepo.getTargetResourceClass());
    }

    @Test
    public void testInterfaceAccess() {
        // tag::interfaceAccess[]
        ScheduleRepository scheduleRepository = ((ClientTestContainer) testContainer).getClient().getRepositoryForInterface(ScheduleRepository.class);

        Schedule schedule = new Schedule();
        schedule.setId(13L);
        schedule.setName("mySchedule");
        scheduleRepository.create(schedule);

        QuerySpec querySpec = new QuerySpec(Schedule.class);
        ScheduleList list = scheduleRepository.findAll(querySpec);
        Assertions.assertEquals(1, list.size());
        ScheduleListMeta meta = list.getMeta();
        ScheduleListLinks links = list.getLinks();
        Assertions.assertNotNull(meta);
        Assertions.assertNotNull(links);
        // end::interfaceAccess[]
    }

    @Test
    public void testCreate() {
        ScheduleRepository scheduleRepository = ((ClientTestContainer) testContainer).getClient().getRepositoryForInterface(ScheduleRepository.class);

        Schedule schedule = new Schedule();
        schedule.setName("mySchedule");
        scheduleRepository.create(schedule);

        QuerySpec querySpec = new QuerySpec(Schedule.class);
        ScheduleList list = scheduleRepository.findAll(querySpec);
        Assertions.assertEquals(1, list.size());
        schedule = list.get(0);
        Assertions.assertNotNull(schedule.getId());
        ScheduleListMeta meta = list.getMeta();
        ScheduleListLinks links = list.getLinks();
        Assertions.assertNotNull(meta);
        Assertions.assertNotNull(links);
    }


    @Test
    public void testHttpAdapterListenerInvoked() {
        CrnkClient client = ((ClientTestContainer) testContainer).getClient();
        listener = Mockito.mock(HttpAdapterListener.class);

        client.getHttpAdapter().addListener(listener);

        ArgumentCaptor<HttpAdapterRequest> requestCaptor = ArgumentCaptor.forClass(HttpAdapterRequest.class);
        ArgumentCaptor<HttpAdapterResponse> responseCaptor = ArgumentCaptor.forClass(HttpAdapterResponse.class);

        ScheduleRepository scheduleRepository = client.getRepositoryForInterface(ScheduleRepository.class);
        Schedule schedule = new Schedule();
        schedule.setName("mySchedule");
        scheduleRepository.create(schedule);

        Mockito.verify(listener, Mockito.times(1)).onRequest(requestCaptor.capture());
        Mockito.verify(listener, Mockito.times(1)).onResponse(Mockito.any(HttpAdapterRequest.class), responseCaptor.capture());

        HttpAdapterRequest request = requestCaptor.getValue();
        Assertions.assertEquals(client.getServiceUrlProvider().getUrl() + "/schedules", request.getUrl());
        Assertions.assertEquals(HttpMethod.POST, request.getHttpMethod());
        Assertions.assertEquals(HttpHeaders.JSONAPI_CONTENT_TYPE_AND_CHARSET, request.getHeaderValue(HttpHeaders.HTTP_CONTENT_TYPE));
        Assertions.assertTrue(request.getHeadersNames().contains(HttpHeaders.HTTP_CONTENT_TYPE));
        Assertions.assertTrue(request.getHeadersNames().contains(HttpHeaders.HTTP_HEADER_ACCEPT));

        HttpAdapterResponse response = responseCaptor.getValue();
        Assertions.assertTrue(response.getHeaderNames().contains(HttpHeaders.HTTP_CONTENT_TYPE));
        Assertions.assertEquals(HttpHeaders.JSONAPI_CONTENT_TYPE_AND_CHARSET, response.getResponseHeader(HttpHeaders.HTTP_CONTENT_TYPE));
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

        HttpAdapter httpAdapter = ((ClientTestContainer) testContainer).getClient().getHttpAdapter();
        if (httpAdapter instanceof OkHttpAdapter) {
            ((OkHttpAdapter) httpAdapter).addListener(new OkHttpAdapterListener() {

                @Override
                public void onBuild(Builder builder) {
                    builder.addInterceptor(interceptor);
                }
            });
        }

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

        if (httpAdapter instanceof OkHttpAdapter) {
            // check HTTP handling
            Assertions.assertEquals(4, methods.size());
            Assertions.assertEquals(4, paths.size());
            Assertions.assertEquals("POST", methods.get(0));
            Assertions.assertEquals("GET", methods.get(1));
            Assertions.assertEquals("PATCH", methods.get(2));
            Assertions.assertEquals("/tasks/1", paths.get(2));
            Assertions.assertEquals("GET", methods.get(3));

            Assertions.assertEquals("/tasks", paths.get(0));
            Assertions.assertEquals("/tasks/1", paths.get(1));
            Assertions.assertEquals("/tasks/1", paths.get(3));
        }
    }
}
