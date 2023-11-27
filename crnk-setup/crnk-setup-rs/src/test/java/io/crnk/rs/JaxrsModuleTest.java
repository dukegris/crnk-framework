package io.crnk.rs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.repository.RepositoryAction;
import io.crnk.core.engine.information.repository.RepositoryAction.RepositoryActionType;
import io.crnk.core.engine.information.repository.RepositoryInformationProviderContext;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceInformationProvider;
import io.crnk.core.engine.internal.information.DefaultInformationBuilder;
import io.crnk.core.engine.internal.information.resource.DefaultResourceFieldInformationProvider;
import io.crnk.core.engine.internal.information.resource.DefaultResourceInformationProvider;
import io.crnk.core.engine.internal.jackson.JacksonResourceFieldInformationProvider;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.security.SecurityProvider;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import io.crnk.core.queryspec.pagingspec.PagingBehavior;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.legacy.registry.DefaultResourceInformationProviderContext;
import io.crnk.rs.internal.JaxrsModule;
import io.crnk.test.mock.models.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.SecurityContext;
import java.util.Map;

public class JaxrsModuleTest {

    private JaxrsModule.JaxrsResourceRepositoryInformationProvider builder;

    private RepositoryInformationProviderContext context;

    @BeforeEach
    public void setup() {
        final ModuleRegistry moduleRegistry = new ModuleRegistry();
        builder = new JaxrsModule.JaxrsResourceRepositoryInformationProvider();
        final ResourceInformationProvider resourceInformationProvider = new DefaultResourceInformationProvider(
                moduleRegistry.getPropertiesProvider(),
                ImmutableList.<PagingBehavior>of(new OffsetLimitPagingBehavior()),
                new DefaultResourceFieldInformationProvider(),
                new JacksonResourceFieldInformationProvider());
        resourceInformationProvider
                .init(new DefaultResourceInformationProviderContext(resourceInformationProvider,
                        new DefaultInformationBuilder(moduleRegistry.getTypeParser()), moduleRegistry.getTypeParser(),
                        () -> new ObjectMapper()));
        context = new RepositoryInformationProviderContext() {

            @Override
            public ResourceInformationProvider getResourceInformationBuilder() {
                return resourceInformationProvider;
            }

            @Override
            public TypeParser getTypeParser() {
                return moduleRegistry.getTypeParser();
            }

            @Override
            public InformationBuilder builder() {
                return new DefaultInformationBuilder(moduleRegistry.getTypeParser());
            }
        };
    }

    @Test
    public void testGetter() {
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        JaxrsModule module = new JaxrsModule(securityContext);
        Assertions.assertEquals("jaxrs", module.getModuleName());
    }

    @Test
    public void checkSecurityProviderRegistered() {
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        JaxrsModule module = new JaxrsModule(securityContext);

        CrnkBoot boot = new CrnkBoot();
        boot.addModule(module);
        boot.boot();

        SecurityProvider securityProvider = boot.getModuleRegistry().getSecurityProvider();
        Assertions.assertNotNull(securityProvider);

        Mockito.when(securityContext.isUserInRole("admin")).thenReturn(true);

        Assertions.assertTrue(securityProvider.isUserInRole("admin", null));
        Assertions.assertFalse(securityProvider.isUserInRole("other", null));
    }

    @Test
    public void testActionDetection() {
        ResourceRepositoryInformation information = (ResourceRepositoryInformation) builder.build(TaskRepository.class,
                context);
        Map<String, RepositoryAction> actions = information.getActions();
        Assertions.assertEquals(5, actions.size());
        RepositoryAction action = actions.get("repositoryAction");
        Assertions.assertNotNull(actions.get("repositoryPostAction"));
        Assertions.assertNotNull(actions.get("repositoryDeleteAction"));
        Assertions.assertNotNull(actions.get("repositoryPutAction"));
        Assertions.assertNull(actions.get("notAnAction"));
        Assertions.assertNotNull(action);
        Assertions.assertEquals("repositoryAction", action.getName());
        Assertions.assertEquals(RepositoryActionType.REPOSITORY, action.getActionType());
        Assertions.assertEquals(RepositoryActionType.RESOURCE, actions.get("resourceAction").getActionType());
    }

    @Test
    public void testInvalidRootPathRepository() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
            builder.build(InvalidRootPathRepository.class, context);
		});
    }

    @Test
    public void testInvalidIdPathRepository1() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
            builder.build(InvalidIdPathRepository1.class, context);
		});
    }

    @Test
    public void testInvalidIdPathRepository2() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
            builder.build(InvalidIdPathRepository2.class, context);
		});
    }

    @Test
    public void testPathToLongRepository() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
            builder.build(PathToLongRepository.class, context);
		});
    }

    @Test
    public void testMissingPathRepository1() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
            builder.build(MissingPathRepository1.class, context);
		});
    }

    @Test
    public void testMissingPathRepository2() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
            builder.build(MissingPathRepository2.class, context);
		});
    }

    @Path("schedules")
    public interface TaskRepository extends ResourceRepository<Task, Long> {

        @GET
        @Path("repositoryAction")
        String repositoryAction(@QueryParam(value = "msg") String msg);

        @POST
        @Path("repositoryPostAction")
        String repositoryPostAction();

        @DELETE
        @Path("repositoryDeleteAction")
        String repositoryDeleteAction();

        @PUT
        @Path("/repositoryPutAction/")
        String repositoryPutAction();

        @GET
        @Path("{id}/resourceAction")
        String resourceAction(@PathParam("id") long id, @QueryParam(value = "msg") String msg);

    }

    @Path("schedules")
    public interface InvalidRootPathRepository extends ResourceRepository<Task, Long> {

        @GET
        @Path("")
        String resourceAction();

    }

    @Path("schedules")
    public interface MissingPathRepository1 extends ResourceRepository<Task, Long> {

        @GET
        String resourceAction();

    }

    @Path("schedules")
    public interface MissingPathRepository2 extends ResourceRepository<Task, Long> {

        String resourceAction(@PathParam("id") long id);

    }

    @Path("schedules")
    public interface PathToLongRepository extends ResourceRepository<Task, Long> {

        @GET
        @Path("a/b/c")
        String resourceAction();

    }

    @Path("schedules")
    public interface InvalidIdPathRepository1 extends ResourceRepository<Task, Long> {

        @GET
        @Path("{something}/test")
        String resourceAction();

    }

    @Path("schedules")
    public interface InvalidIdPathRepository2 extends ResourceRepository<Task, Long> {

        @GET
        @Path("{id}")
        String resourceAction();

    }

}
