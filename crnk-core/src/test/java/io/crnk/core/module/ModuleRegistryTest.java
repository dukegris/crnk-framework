package io.crnk.core.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.filter.DocumentFilter;
import io.crnk.core.engine.filter.RepositoryFilter;
import io.crnk.core.engine.filter.ResourceModificationFilter;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.repository.RepositoryInformationProvider;
import io.crnk.core.engine.information.repository.RepositoryInformationProviderContext;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.information.resource.ResourceInformationProvider;
import io.crnk.core.engine.information.resource.ResourceInformationProviderModule;
import io.crnk.core.engine.internal.CoreModule;
import io.crnk.core.engine.internal.dispatcher.filter.TestFilter;
import io.crnk.core.engine.internal.dispatcher.filter.TestRepositoryDecorator;
import io.crnk.core.engine.internal.dispatcher.filter.TestRepositoryDecorator.DecoratedScheduleRepository;
import io.crnk.core.engine.internal.exception.ExceptionMapperLookup;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistry;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistryTest;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistryTest.IllegalStateExceptionMapper;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistryTest.SomeIllegalStateExceptionMapper;
import io.crnk.core.engine.internal.information.DefaultInformationBuilder;
import io.crnk.core.engine.internal.jackson.JacksonModule;
import io.crnk.core.engine.internal.registry.ResourceRegistryImpl;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.registry.DefaultResourceRegistryPart;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.security.SecurityProvider;
import io.crnk.core.engine.security.SecurityProviderContext;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.exception.ForbiddenException;
import io.crnk.core.mock.models.ComplexPojo;
import io.crnk.core.mock.models.Document;
import io.crnk.core.mock.models.FancyProject;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Schedule;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.models.Thing;
import io.crnk.core.mock.models.User;
import io.crnk.core.mock.repository.DocumentRepository;
import io.crnk.core.mock.repository.PojoRepository;
import io.crnk.core.mock.repository.ProjectRepository;
import io.crnk.core.mock.repository.RelationIdTestRepository;
import io.crnk.core.mock.repository.ResourceWithoutRepositoryToProjectRepository;
import io.crnk.core.mock.repository.ScheduleRepository;
import io.crnk.core.mock.repository.ScheduleRepositoryImpl;
import io.crnk.core.mock.repository.TaskRepository;
import io.crnk.core.mock.repository.TaskToProjectRepository;
import io.crnk.core.mock.repository.TaskWithLookupRepository;
import io.crnk.core.mock.repository.UserRepository;
import io.crnk.core.mock.repository.UserToProjectRepository;
import io.crnk.core.module.discovery.ResourceLookup;
import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import io.crnk.core.repository.InMemoryResourceRepository;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.repository.decorate.RepositoryDecoratorFactory;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.utils.Prioritizable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModuleRegistryTest {

    private ResourceRegistry resourceRegistry;

    private ModuleRegistry moduleRegistry;

    private TestModule testModule;

    private ServiceDiscovery serviceDiscovery = Mockito.mock(ServiceDiscovery.class);

    @BeforeEach
    public void setup() {
        moduleRegistry = new ModuleRegistry();
        moduleRegistry.getHttpRequestContextProvider().setServiceUrlProvider(new ConstantServiceUrlProvider("http://localhost"));
        resourceRegistry = new ResourceRegistryImpl(new DefaultResourceRegistryPart(), moduleRegistry);

        moduleRegistry.setServiceDiscovery(serviceDiscovery);

        testModule = new TestModule();
        moduleRegistry.addModule(testModule);
        moduleRegistry.addModule(new CoreModule());
        moduleRegistry.addModule(new JacksonModule(new ObjectMapper(), false));

        moduleRegistry.addPagingBehavior(new OffsetLimitPagingBehavior());
        moduleRegistry.addModule(new ResourceInformationProviderModule());
        moduleRegistry.init(new ObjectMapper());

        Assertions.assertEquals(resourceRegistry, moduleRegistry.getResourceRegistry());
    }

    interface PrioDocumentFilter extends DocumentFilter, Prioritizable {

    }

    @Test
    public void checkAddingPagingBehavior() {
        Assertions.assertEquals(1, moduleRegistry.getPagingBehaviors().size());
    }

    @Test
    public void checkDocumentFilterPriority() {
        PrioDocumentFilter filter1 = Mockito.mock(PrioDocumentFilter.class);
        PrioDocumentFilter filter2 = Mockito.mock(PrioDocumentFilter.class);
        Mockito.when(filter1.getPriority()).thenReturn(2);
        Mockito.when(filter2.getPriority()).thenReturn(1);

        ModuleRegistry moduleRegistry = new ModuleRegistry();
        SimpleModule module = new SimpleModule("test");
        module.addFilter(filter1);
        module.addFilter(filter2);
        moduleRegistry.setResourceRegistry(new ResourceRegistryImpl(new DefaultResourceRegistryPart(), moduleRegistry));
        moduleRegistry.addModule(module);
        moduleRegistry.init(new ObjectMapper());

        List<DocumentFilter> filters = moduleRegistry.getFilters();
        Assertions.assertSame(filter2, filters.get(0));
        Assertions.assertSame(filter1, filters.get(1));
    }


    interface PrioResourceModificationFilter extends ResourceModificationFilter, Prioritizable {

    }


    @Test
    public void checkResourceModificationFilterPriority() {
        PrioResourceModificationFilter filter1 = Mockito.mock(PrioResourceModificationFilter.class);
        PrioResourceModificationFilter filter2 = Mockito.mock(PrioResourceModificationFilter.class);
        Mockito.when(filter1.getPriority()).thenReturn(2);
        Mockito.when(filter2.getPriority()).thenReturn(1);

        ModuleRegistry moduleRegistry = new ModuleRegistry();
        SimpleModule module = new SimpleModule("test");
        module.addResourceModificationFilter(filter1);
        module.addResourceModificationFilter(filter2);
        moduleRegistry.addModule(module);
        moduleRegistry.setResourceRegistry(new ResourceRegistryImpl(new DefaultResourceRegistryPart(), moduleRegistry));
        moduleRegistry.init(new ObjectMapper());

        List<ResourceModificationFilter> filters = moduleRegistry.getResourceModificationFilters();
        Assertions.assertSame(filter2, filters.get(0));
        Assertions.assertSame(filter1, filters.get(1));
    }

    @Test
    public void checkNullResourcePath() {
        ModuleRegistry moduleRegistry = new ModuleRegistry();
        SimpleModule module = new SimpleModule("test");
        moduleRegistry.setResourceRegistry(new ResourceRegistryImpl(new DefaultResourceRegistryPart(), moduleRegistry));
        moduleRegistry.addModule(module);
        moduleRegistry.init(new ObjectMapper());
        Assertions.assertEquals(moduleRegistry.getResourceInformationBuilder().getResourcePath(TestResource2.class), null);
    }

    interface PrioRepositoryFilter extends RepositoryFilter, Prioritizable {

    }


    @Test
    public void checkRepositoryFilterPriority() {
        PrioRepositoryFilter filter1 = Mockito.mock(PrioRepositoryFilter.class);
        PrioRepositoryFilter filter2 = Mockito.mock(PrioRepositoryFilter.class);
        Mockito.when(filter1.getPriority()).thenReturn(2);
        Mockito.when(filter2.getPriority()).thenReturn(1);

        ModuleRegistry moduleRegistry = new ModuleRegistry();
        SimpleModule module = new SimpleModule("test");
        module.addRepositoryFilter(filter1);
        module.addRepositoryFilter(filter2);
        moduleRegistry.addModule(module);
        moduleRegistry.setResourceRegistry(new ResourceRegistryImpl(new DefaultResourceRegistryPart(), moduleRegistry));
        moduleRegistry.init(new ObjectMapper());

        List<RepositoryFilter> filters = moduleRegistry.getRepositoryFilters();
        Assertions.assertSame(filter2, filters.get(0));
        Assertions.assertSame(filter1, filters.get(1));
    }


    @Test
    public void getModules() {
        Assertions.assertEquals(4, moduleRegistry.getModules().size());
    }

    @Test
    public void testGetServiceDiscovery() {
        Assertions.assertEquals(serviceDiscovery, moduleRegistry.getServiceDiscovery());
        Assertions.assertEquals(serviceDiscovery, testModule.context.getServiceDiscovery());
    }

    @Test
    public void invalidRepository() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
        moduleRegistry.getRepositoryInformationBuilder().build("no resource", null);
		});
    }

    @Test
    public void getModuleContext() {
        Assertions.assertNotNull(moduleRegistry.getContext());
        Assertions.assertNotNull(moduleRegistry.getContext().getObjectMapper());
    }

    @Test
    public void repositoryInformationBuilderAccept() {
        RepositoryInformationProvider builder = moduleRegistry.getRepositoryInformationBuilder();
        Assertions.assertFalse(builder.accept("no resource"));
        Assertions.assertFalse(builder.accept(String.class));
        Assertions.assertTrue(builder.accept(TaskRepository.class));
        Assertions.assertTrue(builder.accept(ProjectRepository.class));
        Assertions.assertTrue(builder.accept(TaskToProjectRepository.class));
        Assertions.assertTrue(builder.accept(new TaskRepository()));
        Assertions.assertTrue(builder.accept(new TaskToProjectRepository()));
    }

    @Test
    public void buildWithInvalidRepositoryClass() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
        RepositoryInformationProviderContext context = Mockito.mock(RepositoryInformationProviderContext.class);
        moduleRegistry.getRepositoryInformationBuilder().build(String.class, context);
		});
    }

    @Test
    public void buildResourceRepositoryInformationFromClass() {
        RepositoryInformationProvider builder = moduleRegistry.getRepositoryInformationBuilder();

        ResourceRepositoryInformation info =
                (ResourceRepositoryInformation) builder.build(TaskRepository.class, newRepositoryInformationBuilderContext());
        Assertions.assertEquals(Task.class, info.getResourceInformation().get().getResourceClass());
        Assertions.assertEquals("tasks", info.getPath());
    }

    @Test
    public void buildResourceRepositoryInformationFromInstance() {
        RepositoryInformationProvider builder = moduleRegistry.getRepositoryInformationBuilder();

        ResourceRepositoryInformation info =
                (ResourceRepositoryInformation) builder.build(new TaskRepository(), newRepositoryInformationBuilderContext());
        Assertions.assertEquals(Task.class, info.getResourceInformation().get().getResourceClass());
        Assertions.assertEquals("tasks", info.getPath());
    }

    private RepositoryInformationProviderContext newRepositoryInformationBuilderContext() {
        return new RepositoryInformationProviderContext() {

            @Override
            public ResourceInformationProvider getResourceInformationBuilder() {
                return moduleRegistry.getResourceInformationBuilder();
            }

            @Override
            public TypeParser getTypeParser() {
                return moduleRegistry.getTypeParser();
            }

            @Override
            public InformationBuilder builder() {
                return new DefaultInformationBuilder(getTypeParser());
            }
        };
    }

    @Test
    public void testNotInitialized() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
        moduleRegistry = new ModuleRegistry();
        moduleRegistry.getResourceRegistry();
		});
    }

    @Test
    public void testDuplicateInitialization() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
        ObjectMapper objectMapper = new ObjectMapper();
        moduleRegistry.init(objectMapper);
		});
    }

    @Test
    public void checkGetModule() {
        Module notRegisteredModule = Mockito.mock(Module.class);
        Assertions.assertNotNull(moduleRegistry.getModule(TestModule.class).get());
        Assertions.assertFalse(moduleRegistry.getModule(notRegisteredModule.getClass()).isPresent());
    }


    @Test
    public void testExceptionMappers() {
        ExceptionMapperLookup exceptionMapperLookup = moduleRegistry.getExceptionMapperLookup();
        List<ExceptionMapper> exceptionMappers = exceptionMapperLookup.getExceptionMappers();
        Set<Class<?>> classes = new HashSet<>();
        for (ExceptionMapper exceptionMapper : exceptionMappers) {
            classes.add(exceptionMapper.getClass());
        }
        Assertions.assertTrue(classes.contains(IllegalStateExceptionMapper.class));
        Assertions.assertTrue(classes.contains(SomeIllegalStateExceptionMapper.class));
    }

    @Test
    public void testInitCalled() {
        Assertions.assertTrue(testModule.initialized);
    }

    @Test
    public void testModuleChangeAfterAddModule() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
        SimpleModule module = new SimpleModule("test2");
        moduleRegistry.addModule(module);
        module.addFilter(new TestFilter());
		});
    }

    @Test
    public void testGetResourceRegistry() {
        Assertions.assertSame(resourceRegistry, testModule.getContext().getResourceRegistry());
    }

    @Test
    public void testNoResourceRegistryBeforeInitialization() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
        ModuleRegistry registry = new ModuleRegistry();
        registry.addModule(new SimpleModule("test") {

            @Override
            public void setupModule(ModuleContext context) {
                context.getResourceRegistry(); // fail
            }
        });
		});
    }

    @Test
    public void testInformationBuilder() {
        ResourceInformationProvider informationProvider = moduleRegistry.getResourceInformationBuilder();

        Assertions.assertTrue(informationProvider.accept(ComplexPojo.class));
        Assertions.assertTrue(informationProvider.accept(Document.class));
        Assertions.assertTrue(informationProvider.accept(FancyProject.class));
        Assertions.assertTrue(informationProvider.accept(Project.class));
        Assertions.assertTrue(informationProvider.accept(Task.class));
        Assertions.assertTrue(informationProvider.accept(Thing.class));
        Assertions.assertTrue(informationProvider.accept(User.class));
        Assertions.assertTrue(informationProvider.accept(TestResource.class));

        Assertions.assertFalse(informationProvider.accept(TestRepository.class));
        Assertions.assertFalse(informationProvider.accept(DocumentRepository.class));
        Assertions.assertFalse(informationProvider.accept(PojoRepository.class));
        Assertions.assertFalse(informationProvider.accept(ProjectRepository.class));
        Assertions.assertFalse(informationProvider.accept(ResourceWithoutRepositoryToProjectRepository.class));
        Assertions.assertFalse(informationProvider.accept(TaskToProjectRepository.class));
        Assertions.assertFalse(informationProvider.accept(TaskWithLookupRepository.class));
        Assertions.assertFalse(informationProvider.accept(UserRepository.class));
        Assertions.assertFalse(informationProvider.accept(UserToProjectRepository.class));

        Assertions.assertFalse(informationProvider.accept(Object.class));
        Assertions.assertFalse(informationProvider.accept(String.class));

        try {
            informationProvider.build(Object.class);
            Assertions.fail();
        } catch (UnsupportedOperationException e) {
            // ok
        }

        ResourceInformation userInfo = informationProvider.build(User.class);
        Assertions.assertEquals("loginId", userInfo.getIdField().getUnderlyingName());

        ResourceInformation testInfo = informationProvider.build(TestResource.class);
        Assertions.assertEquals("id", testInfo.getIdField().getUnderlyingName());

        // setup by TestResourceInformationProvider
        Assertions.assertEquals("testId", testInfo.getIdField().getJsonName());
    }

    @Test
    public void testResourceLookup() {
        ResourceLookup resourceLookup = moduleRegistry.getResourceLookup();

        Assertions.assertFalse(resourceLookup.getResourceClasses().contains(Object.class));
        Assertions.assertFalse(resourceLookup.getResourceClasses().contains(String.class));
        Assertions.assertTrue(resourceLookup.getResourceClasses().contains(TestResource.class));
    }

    @Test
    public void testJacksonModule() {
        List<com.fasterxml.jackson.databind.Module> jacksonModules = moduleRegistry.getJacksonModules();
        Assertions.assertEquals(1, jacksonModules.size());
        com.fasterxml.jackson.databind.Module jacksonModule = jacksonModules.get(0);
        Assertions.assertEquals("test", jacksonModule.getModuleName());
    }

    @Test
    public void testFilter() {
        List<DocumentFilter> filters = moduleRegistry.getFilters();
        Assertions.assertEquals(1, filters.size());
    }

    @Test
    public void checkCombinedResourceInformationBuilderGetResurceType() {
        Class<?> noResourceClass = String.class;
        Assertions.assertNull(moduleRegistry.getResourceInformationBuilder().getResourceType(noResourceClass));
        Assertions.assertNotNull(moduleRegistry.getResourceInformationBuilder().getResourceType(Task.class));
    }

    @Test
    public void testDecorators() {
        List<RepositoryDecoratorFactory> decorators = moduleRegistry.getRepositoryDecoratorFactories();
        Assertions.assertEquals(1, decorators.size());

        RegistryEntry entry = this.resourceRegistry.getEntry(Schedule.class);
        Object resourceRepository = entry.getResourceRepository().getImplementation();
        Assertions.assertNotNull(resourceRepository);
        Assertions.assertTrue(resourceRepository instanceof ScheduleRepository);
        Assertions.assertTrue(resourceRepository instanceof DecoratedScheduleRepository);
    }

    @Test
    public void testSecurityProvider() {
        Assertions.assertTrue(moduleRegistry.getSecurityProvider().isUserInRole("testRole", null));
        Assertions.assertFalse(moduleRegistry.getSecurityProvider().isUserInRole("nonExistingRole", null));
        Assertions.assertTrue(testModule.getContext().getSecurityProvider().isUserInRole("testRole", null));
    }

    @Test
    public void testRepositoryRegistration() {
        RegistryEntry entry = resourceRegistry.getEntry(TestResource2.class);
        ResourceInformation info = entry.getResourceInformation();
        Assertions.assertEquals(TestResource2.class, info.getResourceClass());

        Assertions.assertNotNull(entry.getResourceRepository());
        RelationshipRepositoryAdapter relationshipRepositoryAdapter = entry.getRelationshipRepository("parent");
        Assertions.assertNotNull(relationshipRepositoryAdapter);
    }

    @Test
    public void checkOverrideDefaultExceptionMapper() {
        SimpleModule module = new SimpleModule("test2");
        module.addExceptionMapper(new IllegalStateExceptionMapper());
        module.addExceptionMapper(new ExceptionMapperRegistryTest.CustomForbiddenExceptionMapper());

        moduleRegistry = new ModuleRegistry();
        moduleRegistry.setResourceRegistry(resourceRegistry);
        moduleRegistry.addModule(module);
        moduleRegistry.init(new ObjectMapper());

        ExceptionMapperRegistry registry = moduleRegistry.getExceptionMapperRegistry();
        Response response = registry.toResponse(new ForbiddenException("test"));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST_400, response.getHttpStatus().intValue());
    }

    @Test
    public void checkNotOverrideDefaultExceptionMapper() {
        moduleRegistry = new ModuleRegistry();
        moduleRegistry.setResourceRegistry(resourceRegistry);
        moduleRegistry.init(new ObjectMapper());

        Response response = moduleRegistry.getExceptionMapperRegistry().toResponse(new ForbiddenException("test"));
        Assertions.assertEquals(HttpStatus.FORBIDDEN_403, response.getHttpStatus().intValue());
    }

    @Test
    public void checkExceptionMapperLowPriority() {
        SimpleModule module = new SimpleModule("illegalException");
        module.addExceptionMapper(new IllegalStateExceptionMapper());
        module.addExceptionMapper(new ExceptionMapperRegistryTest.SecondIllegalStateExceptionMapper(1));

        moduleRegistry = new ModuleRegistry();
        moduleRegistry.setResourceRegistry(resourceRegistry);
        moduleRegistry.addModule(module);
        moduleRegistry.init(new ObjectMapper());

        Response response = moduleRegistry.getExceptionMapperRegistry().toResponse(new IllegalStateException());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST_400, response.getHttpStatus().intValue());
    }

    @Test
    public void checkExceptionMapperHighPriority() {
        SimpleModule module = new SimpleModule("illegalException");
        module.addExceptionMapper(new IllegalStateExceptionMapper());
        module.addExceptionMapper(new ExceptionMapperRegistryTest.SecondIllegalStateExceptionMapper(-1));

        moduleRegistry = new ModuleRegistry();
        moduleRegistry.setResourceRegistry(resourceRegistry);
        moduleRegistry.addModule(module);
        moduleRegistry.init(new ObjectMapper());

        ExceptionMapperRegistry registry = moduleRegistry.getExceptionMapperRegistry();
        Response response = registry.toResponse(new IllegalStateException());
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500, response.getHttpStatus().intValue());
    }

    @JsonApiResource(type = "test2")
    static class TestResource2 {

        @JsonApiId
        private int id;

        @JsonApiRelation
        private TestResource2 parent;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public TestResource2 getParent() {
            return parent;
        }

        public void setParent(TestResource2 parent) {
            this.parent = parent;
        }
    }

    @JsonApiResource(type = "test3", resourcePath = "/testResource3s")
    static class TestResource3 {

        @JsonApiId
        private int id;
    }

    class TestModule implements InitializingModule {

        private ModuleContext context;

        private boolean initialized;

        @Override
        public String getModuleName() {
            return "test";
        }

        public ModuleContext getContext() {
            return context;
        }

        @Override
        public void setupModule(ModuleContext context) {
            this.context = context;
            context.addResourceLookup(new TestResourceLookup());
            context.addResourceInformationProvider(new TestResourceInformationProvider());

            context.addJacksonModule(new com.fasterxml.jackson.databind.module.SimpleModule() {

                private static final long serialVersionUID = 7829254359521781942L;

                @Override
                public String getModuleName() {
                    return "test";
                }
            });

            context.addSecurityProvider(new SecurityProvider() {
                @Override
                public boolean isUserInRole(String role, SecurityProviderContext context) {
                    return "testRole".equals(role);
                }

                @Override
                public boolean isAuthenticated(SecurityProviderContext context) {
                    return true;
                }
            });

            context.addRepositoryDecoratorFactory(new TestRepositoryDecorator());
            context.addFilter(new TestFilter());
            context.addRepository(new ScheduleRepositoryImpl());
            context.addRepository(new RelationIdTestRepository());
            context.addRepository(new TestRepository2());
            context.addRepository(new TestRelationshipRepository2());

            context.addRepository(new InMemoryResourceRepository<>(TestResource.class));

            context.addExceptionMapper(new IllegalStateExceptionMapper());
            context.addExceptionMapperLookup(() -> {
                List<ExceptionMapper> list = new ArrayList<>();
                list.add(new SomeIllegalStateExceptionMapper());
                return list;
            });
        }

        @Override
        public void init() {
            initialized = true;
        }
    }

    class TestRelationshipRepository2 implements RelationshipRepository<TestResource2, Integer, TestResource2, Integer> {

        @Override
        public void setRelation(TestResource2 source, Integer targetId, String fieldName) {
        }

        @Override
        public void setRelations(TestResource2 source, Collection<Integer> targetIds, String fieldName) {
        }

        @Override
        public void addRelations(TestResource2 source, Collection<Integer> targetIds, String fieldName) {
        }

        @Override
        public void removeRelations(TestResource2 source, Collection<Integer> targetIds, String fieldName) {
        }

        @Override
        public TestResource2 findOneTarget(Integer sourceId, String fieldName, QuerySpec queryParams) {
            return null;
        }

        @Override
        public ResourceList<TestResource2> findManyTargets(Integer sourceId, String fieldName, QuerySpec queryParams) {
            return null;
        }

        @Override
        public Class<TestResource2> getSourceResourceClass() {
            return TestResource2.class;
        }

        @Override
        public Class<TestResource2> getTargetResourceClass() {
            return TestResource2.class;
        }
    }

    class TestRepository2 implements ResourceRepository<TestResource2, Integer> {

        @Override
        public <S extends TestResource2> S save(S entity) {
            return null;
        }

        @Override
        public void delete(Integer id) {
        }

        @Override
        public Class<TestResource2> getResourceClass() {
            return TestResource2.class;
        }

        @Override
        public TestResource2 findOne(Integer id, QuerySpec querySpec) {
            return null;
        }

        @Override
        public ResourceList<TestResource2> findAll(QuerySpec querySpec) {
            return null;
        }

        @Override
        public ResourceList<TestResource2> findAll(Collection<Integer> ids, QuerySpec querySpec) {
            return null;
        }

        @Override
        public <S extends TestResource2> S create(S entity) {
            return null;
        }
    }
}
