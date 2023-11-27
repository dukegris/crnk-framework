package io.crnk.core.queryspec.mapper;

import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.exception.ParametersDeserializationException;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.queryspec.QuerySpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DefaultQuerySpecUrlMapperDeserializerTest extends DefaultQuerySpecUrlMapperDeserializerTestBase {


    private QueryContext queryContext = new QueryContext().setRequestVersion(0);

    @BeforeEach
    public void setup() {
        super.setup();
        urlMapper.setEnforceDotPathSeparator(true);
        Assertions.assertTrue(urlMapper.getEnforceDotPathSeparator());
    }

    @Test
    public void testDotNotationDisallowsBrackets() {
		Assertions.assertThrows(ParametersDeserializationException.class, () -> {
	        Map<String, Set<String>> params = new HashMap<>();
	        add(params, "filter[projects][tasks][name]", "test");
	        urlMapper.deserialize(taskInformation, params, queryContext);
		});
    }

    @Test
    public void testCannotFilterNonFilterableAttribute() {
        Map<String, Set<String>> params = new HashMap<>();
        add(params, "filter[deleted]", "true");
        try {
            urlMapper.deserialize(taskInformation, params, queryContext);
            Assertions.fail();
        } catch (BadRequestException e) {
            Assertions.assertEquals("path [deleted] is not filterable", e.getMessage());
        }
    }

    @Test
    public void testCannotSortNonSortableAttribute() {
        Map<String, Set<String>> params = new HashMap<>();
        add(params, "sort", "deleted");
        try {
            urlMapper.deserialize(taskInformation, params, queryContext);
            Assertions.fail();
        } catch (BadRequestException e) {
            Assertions.assertEquals("path [deleted] is not sortable", e.getMessage());
        }
    }


    @Test
    public void testNoAmbiguityForType() {
        Map<String, Set<String>> params = new HashMap<>();
        // note that there is both a type and an attribute on tasks called
        // projects
        add(params, "filter[projects][name]", "test");
        QuerySpec querySpec = urlMapper.deserialize(taskInformation, params, queryContext);
        Assertions.assertEquals(Task.class, querySpec.getResourceClass());
        Assertions.assertEquals(0, querySpec.getFilters().size());
        QuerySpec projectQuerySpec = querySpec.getQuerySpec(Project.class);
        Assertions.assertEquals(1, projectQuerySpec.getFilters().size());
        Assertions.assertEquals(Arrays.asList("name"), projectQuerySpec.getFilters().get(0).getAttributePath());
    }

    @Test
    public void testNoAmbiguityForAttribute() {
        // note that there is both a type and an attribute on tasks called
        // projects, here the attribute should match
        Map<String, Set<String>> params = new HashMap<>();
        add(params, "filter[projects]", "someValue");
        urlMapper.setIgnoreParseExceptions(true);
        QuerySpec querySpec = urlMapper.deserialize(taskInformation, params, queryContext);
        Assertions.assertEquals(Task.class, querySpec.getResourceClass());
        Assertions.assertEquals(Arrays.asList("projects"), querySpec.getFilters().get(0).getAttributePath());
        Assertions.assertNull(querySpec.getQuerySpec(Project.class));
    }
}
