package io.crnk.client.action;

import io.crnk.client.AbstractClientTest;
import io.crnk.client.CrnkTestFeature;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.filter.DocumentFilter;
import io.crnk.core.engine.filter.DocumentFilterChain;
import io.crnk.core.engine.filter.DocumentFilterContext;
import io.crnk.core.engine.internal.dispatcher.path.ActionPath;
import io.crnk.core.engine.internal.dispatcher.path.ResourcePath;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.repository.ScheduleRepository;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.HashMap;
import java.util.Map;

public class JsonApiActionResponseTest extends AbstractClientTest {

    protected ScheduleRepository scheduleRepository;

    private DocumentFilter filter;

    @BeforeEach
    public void setup() {
        SLF4JBridgeHandler.install();
        super.setup();
        scheduleRepository = client.getRepositoryForInterface(ScheduleRepository.class);
    }

    class TestFilter implements DocumentFilter {

        @Override
        public Response filter(DocumentFilterContext filterRequestContext, DocumentFilterChain chain) {
            return chain.doFilter(filterRequestContext);
        }
    }

    @Override
    protected void setupFeature(CrnkTestFeature feature) {
        filter = Mockito.spy(new TestFilter());
        SimpleModule testModule = new SimpleModule("testFilter");
        testModule.addFilter(filter);
        feature.addModule(testModule);
    }

    @Override
    protected TestApplication configure() {
        return new TestApplication(true, false);
    }

    @Test
    public void testCrudFind() {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setName("schedule");
        scheduleRepository.create(schedule);

        Iterable<Schedule> schedules = scheduleRepository.findAll(new QuerySpec(Schedule.class));
        schedule = schedules.iterator().next();
        Assertions.assertEquals("schedule", schedule.getName());

        scheduleRepository.delete(schedule.getId());
        schedules = scheduleRepository.findAll(new QuerySpec(Schedule.class));
        Assertions.assertFalse(schedules.iterator().hasNext());
    }

    @Test
    @Disabled
    // The DocumentFilterContext is not invoked with this request any more
    public void testInvokeRepositoryAction() {
        // tag::invokeService[]
        String result = scheduleRepository.repositoryAction("hello");
        Assertions.assertEquals("repository action: hello", result);
        // end::invokeService[]

        // check filters
        ArgumentCaptor<DocumentFilterContext> contexts = ArgumentCaptor.forClass(DocumentFilterContext.class);
        Mockito.verify(filter, Mockito.times(1)).filter(contexts.capture(), Mockito.any(DocumentFilterChain.class));
        DocumentFilterContext actionContext = contexts.getAllValues().get(0);
        Assertions.assertEquals("GET", actionContext.getMethod());
        Assertions.assertTrue(actionContext.getJsonPath() instanceof ActionPath);
    }

    @Test
    public void testInvokeRepositoryActionWithResourceResult() {
        // setup related data, will be included
        ResourceRepository<Project, Object> projectRepo = client.getRepositoryForType(Project.class);
        Project project = new Project();
        project.setId(2L);
        project.setName("Crnk");
        projectRepo.create(project);

        // resources should be received in json api format
        String url = getBaseUri() + "schedules/repositoryActionWithResourceResult?msg=hello&include=project";
        io.restassured.response.Response response = RestAssured.get(url);
        Assertions.assertEquals(200, response.getStatusCode());
        response.then().assertThat().body("data.attributes.name", Matchers.equalTo("hello"));

        response.then().assertThat().body("included", Matchers.hasSize(1));

        // check filters
        ArgumentCaptor<DocumentFilterContext> contexts = ArgumentCaptor.forClass(DocumentFilterContext.class);
        Mockito.verify(filter, Mockito.times(2)).filter(contexts.capture(), Mockito.any(DocumentFilterChain.class));

        // post of project
        DocumentFilterContext actionContext = contexts.getAllValues().get(0);
        Assertions.assertEquals("POST", actionContext.getMethod());
        Assertions.assertTrue(actionContext.getJsonPath() instanceof ResourcePath);

        // action
        DocumentFilterContext includeContext = contexts.getAllValues().get(1);
        Assertions.assertEquals("GET", includeContext.getMethod());
        Assertions.assertTrue(includeContext.getJsonPath() instanceof ActionPath);
    }

    @Test
    public void testInvokeRepositoryActionWithException() {
        // resources should be received in json api format
        String url = getBaseUri() + "schedules/repositoryActionWithException?msg=hello";
        io.restassured.response.Response response = RestAssured.get(url);
        Assertions.assertEquals(403, response.getStatusCode());

        response.then().assertThat().body("errors[0].status", Matchers.equalTo("403"));

        // check filters
        ArgumentCaptor<DocumentFilterContext> contexts = ArgumentCaptor.forClass(DocumentFilterContext.class);
        Mockito.verify(filter, Mockito.times(1)).filter(contexts.capture(), Mockito.any(DocumentFilterChain.class));
        DocumentFilterContext actionContext = contexts.getAllValues().get(0);
        Assertions.assertEquals("GET", actionContext.getMethod());
        Assertions.assertTrue(actionContext.getJsonPath() instanceof ActionPath);
    }

    @Test
    public void testInvokeResourceAction() {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setName("scheduleName");
        scheduleRepository.create(schedule);

        String result = scheduleRepository.resourceAction(1, "hello");
        Assertions.assertEquals("{\"data\":\"resource action: hello@scheduleName\"}", result);

        // check filters
        ArgumentCaptor<DocumentFilterContext> contexts = ArgumentCaptor.forClass(DocumentFilterContext.class);
        Mockito.verify(filter, Mockito.times(2)).filter(contexts.capture(), Mockito.any(DocumentFilterChain.class));
        DocumentFilterContext actionContext = contexts.getAllValues().get(1);
        Assertions.assertEquals("GET", actionContext.getMethod());
        Assertions.assertTrue(actionContext.getJsonPath() instanceof ActionPath);
    }

    @Test
    public void testJsonInclude() {
        Map<String, String> customData  = new HashMap<String, String>() {{
            put("key1", "value1");
            put("key2", "value2");
        }};

        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setCustomData(customData);
        scheduleRepository.create(schedule);

        String url = getBaseUri() + "schedules/1";
        io.restassured.response.Response response = RestAssured.get(url);
        Assertions.assertEquals(200, response.getStatusCode());
        response.then().assertThat().body("data.attributes.customData", Matchers.notNullValue());
    }
}
