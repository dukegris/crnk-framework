package io.crnk.core.queryspec.pagingspec;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.information.resource.ResourceFieldImpl;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.mock.models.Task;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.queryspec.mapper.DefaultQuerySpecUrlMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PagingSpecUrlBuilderTest {

    @Mock
    private RepositoryRequestSpec repositoryRequestSpec;

    @Mock
    private ResourceRegistry resourceRegistry;

    private ModuleRegistry moduleRegistry;

    @BeforeEach
    public void setUp() {
        when(resourceRegistry.getResourceUrl(any(QueryContext.class), any(ResourceInformation.class), any())).thenReturn("tasks");
        when(resourceRegistry.getEntry(any(Class.class))).thenReturn(mock(RegistryEntry.class));
        when(repositoryRequestSpec.getId()).thenReturn(1);
        when(repositoryRequestSpec.getRelationshipField()).thenReturn(
                new ResourceFieldImpl("any", "any", ResourceFieldType.ATTRIBUTE, String.class, String.class, null)
        );

        moduleRegistry = new ModuleRegistry();
        moduleRegistry.setUrlMapper(new DefaultQuerySpecUrlMapper());
        moduleRegistry.setResourceRegistry(resourceRegistry);
        moduleRegistry.init(new ObjectMapper());
    }

    @Test
    public void testSelfLink() {
        QueryContext queryContext = new QueryContext();
        queryContext.setBaseUrl("http://localhost");
        queryContext.setRequestPath("/relationships/self");
        QuerySpecAdapter adapter = new QuerySpecAdapter(new QuerySpec(Task.class), resourceRegistry, queryContext);

        PagingSpecUrlBuilder urlBuilder = new PagingSpecUrlBuilder(moduleRegistry, repositoryRequestSpec);
        Assertions.assertEquals("tasks/relationships/any", urlBuilder.build(adapter));
    }

    @Test
    public void testRelatedLink() {
        QueryContext queryContext = new QueryContext();
        queryContext.setBaseUrl("http://localhost");
        queryContext.setRequestPath("/related");
        QuerySpecAdapter adapter = new QuerySpecAdapter(new QuerySpec(Task.class), resourceRegistry, queryContext);

        PagingSpecUrlBuilder urlBuilder = new PagingSpecUrlBuilder(moduleRegistry, repositoryRequestSpec);
        Assertions.assertEquals("tasks/any", urlBuilder.build(adapter));
    }
}
