package io.crnk.core.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.core.CoreTestModule;
import io.crnk.core.engine.dispatcher.RequestDispatcher;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.filter.DocumentFilter;
import io.crnk.core.engine.http.HttpStatusBehavior;
import io.crnk.core.engine.information.contributor.ResourceFieldContributor;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.document.mapper.DocumentMappingConfig;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.result.Result;
import io.crnk.core.engine.security.SecurityProvider;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.engine.url.ServiceUrlProvider;
import io.crnk.core.mock.models.Task;
import io.crnk.core.module.Module;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.core.module.discovery.ServiceDiscoveryFactory;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.queryspec.mapper.DefaultQuerySpecUrlMapper;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import io.crnk.core.repository.decorate.RepositoryDecoratorFactory;
import io.crnk.core.repository.response.JsonApiResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Properties;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;

public class CrnkBootTest {

    private ServiceDiscoveryFactory serviceDiscoveryFactory;

    private ServiceDiscovery serviceDiscovery;

    @BeforeEach
    public void setup() {
        serviceDiscoveryFactory = mock(ServiceDiscoveryFactory.class);
        serviceDiscovery = mock(ServiceDiscovery.class);
        Mockito.when(serviceDiscoveryFactory.getInstance()).thenReturn(serviceDiscovery);
    }

    @Test
    public void setObjectMapper() {
        CrnkBoot boot = new CrnkBoot();
        ObjectMapper mapper = new ObjectMapper();
        boot.setObjectMapper(mapper);
        Assertions.assertSame(mapper, boot.getObjectMapper());
    }

    @Test
    public void checkCannotBootTwice() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
        CrnkBoot boot = new CrnkBoot();
        boot.boot();
        boot.boot();
		});
    }

    @Test
    public void checkCanBootOnce() {
        CrnkBoot boot = new CrnkBoot();
        boot.boot();
    }

    @Test
    public void setServiceDiscovery() {
        CrnkBoot boot = new CrnkBoot();
        ServiceDiscovery serviceDiscovery = mock(ServiceDiscovery.class);
        boot.setServiceDiscovery(serviceDiscovery);
        Assertions.assertSame(serviceDiscovery, boot.getServiceDiscovery());
    }

    @Test
    public void setServiceDiscoveryFactory() {
        CrnkBoot boot = new CrnkBoot();
        boot.setServiceDiscoveryFactory(serviceDiscoveryFactory);
        boot.setServiceUrlProvider(mock(ServiceUrlProvider.class));
        boot.boot();
        Mockito.verify(serviceDiscoveryFactory, Mockito.times(1)).getInstance();
        Assertions.assertNotNull(boot.getServiceDiscovery());
    }

    @Test
    public void getPropertiesProvider() {
        CrnkBoot boot = new CrnkBoot();
        boot.setServiceDiscoveryFactory(serviceDiscoveryFactory);
        boot.setServiceUrlProvider(mock(ServiceUrlProvider.class));
        boot.boot();
        Assertions.assertNotNull(boot.getPropertiesProvider());
    }

    @Test
    public void setInvalidRepository() {
        SimpleModule module = new SimpleModule("test");
        module.addRepository("not a repository");
        CrnkBoot boot = new CrnkBoot();
        boot.boot();
    }


    @Test
    public void testServiceDiscovery() {
        Module module = mock(Module.class);
        RepositoryDecoratorFactory decoratorFactory = mock(RepositoryDecoratorFactory.class);
        ResourceFieldContributor resourceFieldContributor = mock(ResourceFieldContributor.class);
        DocumentFilter filter = mock(DocumentFilter.class);
        SecurityProvider securityProvider = mock(SecurityProvider.class);
        ExceptionMapper exceptionMapper = new TestExceptionMapper();

        Mockito.when(serviceDiscovery.getInstancesByType(eq(DocumentFilter.class))).thenReturn(Arrays.asList(filter));
        HttpStatusBehavior statusBehavior = mock(HttpStatusBehavior.class);
        Mockito.when(serviceDiscovery.getInstancesByType(eq(HttpStatusBehavior.class))).thenReturn(Arrays.asList(statusBehavior));
        Mockito.when(serviceDiscovery.getInstancesByType(eq(RepositoryDecoratorFactory.class)))
                .thenReturn(Arrays.asList(decoratorFactory));
        Mockito.when(serviceDiscovery.getInstancesByType(eq(ResourceFieldContributor.class)))
                .thenReturn(Arrays.asList(resourceFieldContributor));
        Mockito.when(serviceDiscovery.getInstancesByType(eq(Module.class))).thenReturn(Arrays.asList(module));
        Mockito.when(serviceDiscovery.getInstancesByType(eq(SecurityProvider.class))).thenReturn(Arrays.asList(securityProvider));
        Mockito.when(serviceDiscovery.getInstancesByType(eq(ExceptionMapper.class)))
                .thenReturn(Arrays.asList(exceptionMapper));

        CrnkBoot boot = new CrnkBoot();
        boot.setServiceDiscoveryFactory(serviceDiscoveryFactory);
        boot.setServiceUrlProvider(mock(ServiceUrlProvider.class));
        boot.addModule(module);
        boot.boot();

        ModuleRegistry moduleRegistry = boot.getModuleRegistry();
        Assertions.assertTrue(moduleRegistry.getModules().contains(module));
        Assertions.assertTrue(moduleRegistry.getFilters().contains(filter));
        Assertions.assertTrue(moduleRegistry.getResourceFieldContributors().contains(resourceFieldContributor));
        Assertions.assertEquals(2, moduleRegistry.getHttpStatusProviders().size());
        Assertions.assertTrue(moduleRegistry.getRepositoryDecoratorFactories().contains(decoratorFactory));
        Assertions.assertTrue(moduleRegistry.getExceptionMapperLookup().getExceptionMappers().contains(exceptionMapper));
        Assertions.assertTrue(moduleRegistry.getSecurityProviders().contains(securityProvider));
    }

    class TestExceptionMapper implements ExceptionMapper<IllegalStateException> {

        @Override
        public ErrorResponse toErrorResponse(IllegalStateException exception) {
            return null;
        }

        @Override
        public IllegalStateException fromErrorResponse(ErrorResponse errorResponse) {
            return null;
        }

        @Override
        public boolean accepts(ErrorResponse errorResponse) {
            return false;
        }
    }

    @Test
    public void setServiceUrlProvider() {
        CrnkBoot boot = new CrnkBoot();
        boot.setServiceDiscoveryFactory(serviceDiscoveryFactory);
        ServiceUrlProvider serviceUrlProvider = mock(ServiceUrlProvider.class);
        boot.setServiceUrlProvider(serviceUrlProvider);
        boot.boot();
        Assertions.assertEquals(serviceUrlProvider, boot.getServiceUrlProvider());
    }

    @Test
    public void setAllowUnknownAttributes() {
        CrnkBoot boot = new CrnkBoot();
        boot.setAllowUnknownAttributes();
        boot.boot();

        DefaultQuerySpecUrlMapper urlMapper = (DefaultQuerySpecUrlMapper) boot.getUrlMapper();
        Assertions.assertTrue(urlMapper.getAllowUnknownAttributes());
    }

    @Test
    public void setAllowUnknownParameters() {
        CrnkBoot boot = new CrnkBoot();
        boot.setAllowUnknownParameters();
        boot.boot();

        DefaultQuerySpecUrlMapper urlMapper = (DefaultQuerySpecUrlMapper) boot.getUrlMapper();
        Assertions.assertTrue(urlMapper.getAllowUnknownParameters());
    }


    @Test
    public void setConstantServiceUrlProvider() {
        CrnkBoot boot = new CrnkBoot();
        boot.setServiceDiscoveryFactory(serviceDiscoveryFactory);
        final Properties properties = new Properties();
        properties.put(CrnkProperties.RESOURCE_DEFAULT_DOMAIN, "http://something");
        PropertiesProvider propertiesProvider = new PropertiesProvider() {

            @Override
            public String getProperty(String key) {
                return (String) properties.get(key);
            }
        };
        boot.setPropertiesProvider(propertiesProvider);
        boot.boot();

        ServiceUrlProvider serviceUrlProvider = boot.getServiceUrlProvider();
        Assertions.assertTrue(serviceUrlProvider instanceof ConstantServiceUrlProvider);
        Assertions.assertEquals("http://something", serviceUrlProvider.getUrl());
    }

    @Test
    public void testReconfigurationProtection() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
        CrnkBoot boot = new CrnkBoot();
        boot.setServiceDiscoveryFactory(serviceDiscoveryFactory);
        boot.boot();
        boot.setObjectMapper(null);
		});
    }

    @Test
    public void boot() {
        CrnkBoot boot = new CrnkBoot();
        boot.setServiceUrlProvider(() -> "http://127.0.0.1");
        boot.addModule(new CoreTestModule());
        boot.addModule(new SimpleModule("test"));
        boot.boot();

        QueryContext queryContext = new QueryContext();
        queryContext.setBaseUrl(boot.getServiceUrlProvider().getUrl());

        RequestDispatcher requestDispatcher = boot.getRequestDispatcher();

        ResourceRegistry resourceRegistry = boot.getResourceRegistry();
        RegistryEntry taskEntry = resourceRegistry.getEntry(Task.class);
        ResourceRepositoryAdapter repositoryAdapter = taskEntry.getResourceRepository();
        Assertions.assertNotNull(repositoryAdapter.getImplementation());
        JsonApiResponse response = repositoryAdapter.findAll(new QuerySpecAdapter(new QuerySpec(Task.class), resourceRegistry, queryContext)).get();
        Assertions.assertNotNull(response);

        Assertions.assertNotNull(requestDispatcher);

        ServiceDiscovery serviceDiscovery = boot.getServiceDiscovery();
        Assertions.assertNotNull(serviceDiscovery);
        Assertions.assertNotNull(boot.getModuleRegistry());
        Assertions.assertNotNull(boot.getExceptionMapperRegistry());

        boot.setDefaultPageLimit(20L);
        boot.setMaxPageLimit(100L);

        Assertions.assertEquals(1, boot.getPagingBehaviors().size());
        Assertions.assertTrue(boot.getPagingBehaviors().get(0) instanceof OffsetLimitPagingBehavior);
    }

    @Test
    public void testSetServerInfo() {
        CrnkBoot boot = new CrnkBoot();
        boot.addModule(new CoreTestModule());
        boot.putServerInfo("a", "b");
        boot.boot();

        DocumentMapper documentMapper = boot.getDocumentMapper();
        DocumentMappingConfig mappingConfig = new DocumentMappingConfig();
        QuerySpecAdapter querySpecAdapter =
                new QuerySpecAdapter(new QuerySpec(Task.class), boot.getResourceRegistry(), new QueryContext());

        JsonApiResponse response = new JsonApiResponse();
        Result<Document> document = documentMapper.toDocument(response, querySpecAdapter, mappingConfig);
        ObjectNode jsonapi = document.get().getJsonapi();
        Assertions.assertNotNull(jsonapi);
        Assertions.assertNotNull(jsonapi.get("a"));
        Assertions.assertEquals("b", jsonapi.get("a").asText());
    }

    @Test
    public void testEmptyServerInfo() {
        CrnkBoot boot = new CrnkBoot();
        boot.addModule(new CoreTestModule());
        boot.boot();

        DocumentMapper documentMapper = boot.getDocumentMapper();
        DocumentMappingConfig mappingConfig = new DocumentMappingConfig();
        QuerySpecAdapter querySpecAdapter =
                new QuerySpecAdapter(new QuerySpec(Task.class), boot.getResourceRegistry(), new QueryContext());

        JsonApiResponse response = new JsonApiResponse();
        Result<Document> document = documentMapper.toDocument(response, querySpecAdapter, mappingConfig);
        ObjectNode jsonapi = document.get().getJsonapi();
        Assertions.assertNull(jsonapi);
    }
}
