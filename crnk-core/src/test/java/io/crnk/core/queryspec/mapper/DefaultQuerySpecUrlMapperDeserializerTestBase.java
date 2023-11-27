package io.crnk.core.queryspec.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.CoreTestContainer;
import io.crnk.core.CoreTestModule;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.exception.ParametersDeserializationException;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Schedule;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.models.TaskWithLookup;
import io.crnk.core.mock.repository.TaskWithPagingBehavior;
import io.crnk.core.mock.repository.TaskWithPagingBehaviorRepository;
import io.crnk.core.mock.repository.TaskWithPagingBehaviorToProjectRepository;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.queryspec.AbstractQuerySpecTest;
import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.IncludeFieldSpec;
import io.crnk.core.queryspec.IncludeRelationSpec;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.SortSpec;
import io.crnk.core.queryspec.pagingspec.CustomOffsetLimitPagingBehavior;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
// import org.junit.Rule;
import org.junit.jupiter.api.Test;
// import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class DefaultQuerySpecUrlMapperDeserializerTestBase extends AbstractQuerySpecTest {

    // RCS innecesario
    // @Rule
    // public ExpectedException expectedException = ExpectedException.none();

    protected DefaultQuerySpecUrlMapper urlMapper;

    protected ResourceInformation taskInformation;

    private ResourceInformation taskWithPagingBehaviorInformation;

    private QuerySpecUrlContext deserializerContext;

    @BeforeEach
    public void setup() {
        super.setup();

        deserializerContext = new QuerySpecUrlContext() {

            @Override
            public ResourceRegistry getResourceRegistry() {
                return resourceRegistry;
            }

            @Override
            public TypeParser getTypeParser() {
                return moduleRegistry.getTypeParser();
            }

            @Override
            public ObjectMapper getObjectMapper() {
                return container.getObjectMapper();
            }

			@Override
			public UrlBuilder getUrlBuilder() {
				return container.getModuleRegistry().getUrlBuilder();
			}
		};

        urlMapper = new DefaultQuerySpecUrlMapper();
        urlMapper.init(deserializerContext);

        RegistryEntry taskEntry = resourceRegistry.getEntry(Task.class);
        taskInformation = taskEntry.getResourceInformation();
        taskWithPagingBehaviorInformation = resourceRegistry.getEntry(TaskWithPagingBehavior.class).getResourceInformation();
    }


    @Override
    protected void setup(CoreTestContainer container) {
        container.addModule(new CoreTestModule());

        SimpleModule customPagingModule = new SimpleModule("customPaging");
        customPagingModule.addRepository(new TaskWithPagingBehaviorRepository());
        customPagingModule.addRepository(new TaskWithPagingBehaviorToProjectRepository());
        customPagingModule.addPagingBehavior(new OffsetLimitPagingBehavior());
        customPagingModule.addPagingBehavior(new CustomOffsetLimitPagingBehavior());
        container.addModule(customPagingModule);
    }

    @Test
    public void operations() {
        Assertions.assertFalse(urlMapper.getAllowUnknownAttributes());
        urlMapper.setAllowUnknownAttributes(true);
        Assertions.assertTrue(urlMapper.getAllowUnknownAttributes());
        urlMapper.getSupportedOperators().clear();
        urlMapper.setDefaultOperator(FilterOperator.LIKE);
        urlMapper.addSupportedOperator(FilterOperator.LIKE);
        Assertions.assertEquals(FilterOperator.LIKE, urlMapper.getDefaultOperator());
        Assertions.assertEquals(1, urlMapper.getSupportedOperators().size());
    }

    @Test
    public void checkIgnoreParseExceptions() {
        Assertions.assertFalse(urlMapper.isIgnoreParseExceptions());
        urlMapper.setIgnoreParseExceptions(true);
        Assertions.assertTrue(urlMapper.isIgnoreParseExceptions());

        Map<String, Set<String>> params = new HashMap<>();
        add(params, "filter[id]", "notAnInteger");
        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params, queryContext);
        QuerySpec expectedSpec = new QuerySpec(Task.class);
        expectedSpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.EQ, "notAnInteger"));
        Assertions.assertEquals(expectedSpec, actualSpec);
    }

    @Test
    public void throwParseExceptionsByDefault() {
		Assertions.assertThrows(ParametersDeserializationException.class, () -> {
	        Assertions.assertFalse(urlMapper.isIgnoreParseExceptions());

	        Map<String, Set<String>> params = new HashMap<>();
	        add(params, "filter[id]", "notAnInteger");
	        urlMapper.deserialize(taskInformation, params, queryContext);
		});
    }

    @Test
    public void throwParseExceptionsForMultiValuedOffset() {
		Assertions.assertThrows(ParametersDeserializationException.class, () -> {
	        Map<String, Set<String>> params = new HashMap<>();
	        params.put("page[offset]", new HashSet<>(Arrays.asList("1", "2")));
	        urlMapper.deserialize(taskInformation, params, queryContext);
		});
    }

    @Test
    public void throwParseExceptionsWhenBracketsNotClosed() {
		Assertions.assertThrows(ParametersDeserializationException.class, () -> {
	        Map<String, Set<String>> params = new HashMap<>();
	        add(params, "filter[", "someValue");
	        urlMapper.deserialize(taskInformation, params, queryContext);
		});
    }

    @Test
    public void testFindAll() {
        Map<String, Set<String>> params = new HashMap<>();

        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params, queryContext);
        QuerySpec expectedSpec = new QuerySpec(Task.class);
        Assertions.assertEquals(expectedSpec, actualSpec);
    }

    @Test
    public void defaultPaginationOnRoot() {
        Map<String, Set<String>> params = new HashMap<>();
        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params, queryContext);
        Assertions.assertEquals(0L, actualSpec.getOffset());
        Assertions.assertNull(actualSpec.getLimit());
    }

    @Test
    public void defaultPaginationOnRelation() {
        Map<String, Set<String>> params = new HashMap<>();
        add(params, "sort[projects]", "name");
        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params, queryContext);
        Assertions.assertEquals(0L, actualSpec.getOffset());
        Assertions.assertNull(actualSpec.getLimit());
        QuerySpec projectQuerySpec = actualSpec.getQuerySpec(Project.class);
        Assertions.assertNotNull(projectQuerySpec);
        Assertions.assertEquals(0L, projectQuerySpec.getOffset());
        Assertions.assertNull(projectQuerySpec.getLimit());

        Map<String, Set<String>> serialized = urlMapper.serialize(actualSpec, queryContext);
        Assertions.assertEquals(serialized, params);
    }

    @Test
    public void mapJsonToJavaNames() {
        ResourceInformation scheduleInformation = resourceRegistry.getEntry(Schedule.class).getResourceInformation();
        Map<String, Set<String>> params = new HashMap<>();
        add(params, "sort", "description");
        add(params, "filter[description]", "test");
        add(params, "fields", "description");
        add(params, "include", "followup");
        QuerySpec actualSpec = urlMapper.deserialize(scheduleInformation, params, queryContext);

        Assertions.assertEquals(1, actualSpec.getSort().size());
        Assertions.assertEquals(1, actualSpec.getFilters().size());
        Assertions.assertEquals(1, actualSpec.getIncludedFields().size());
        Assertions.assertEquals(1, actualSpec.getIncludedRelations().size());

        SortSpec sortSpec = actualSpec.getSort().get(0);
        FilterSpec filterSpec = actualSpec.getFilters().get(0);
        IncludeRelationSpec includeRelationSpec = actualSpec.getIncludedRelations().get(0);
        IncludeFieldSpec includeFieldSpec = actualSpec.getIncludedFields().get(0);

        Assertions.assertEquals(Arrays.asList("desc"), sortSpec.getAttributePath());
        Assertions.assertEquals(Arrays.asList("desc"), filterSpec.getAttributePath());
        Assertions.assertEquals(Arrays.asList("followupProject"), includeRelationSpec.getAttributePath());
        Assertions.assertEquals(Arrays.asList("desc"), includeFieldSpec.getAttributePath());
    }

    @Test
    public void customPaginationOnRoot() {
        Map<String, Set<String>> params = new HashMap<>();
        QuerySpec actualSpec = urlMapper.deserialize(taskWithPagingBehaviorInformation, params, queryContext);
        Assertions.assertEquals(1L, actualSpec.getOffset());
        Assertions.assertEquals(10L, actualSpec.getLimit().longValue());
    }

    @Test
    public void customPaginationOnRelation() {
        Map<String, Set<String>> params = new HashMap<>();
        add(params, "sort[projects]", "name");
        QuerySpec actualSpec = urlMapper.deserialize(taskWithPagingBehaviorInformation, params, queryContext);
        Assertions.assertEquals(1L, actualSpec.getOffset());
        Assertions.assertEquals(10L, actualSpec.getLimit().longValue());
        QuerySpec projectQuerySpec = actualSpec.getQuerySpec(Project.class);
        Assertions.assertNotNull(projectQuerySpec);
        Assertions.assertEquals(0L, projectQuerySpec.getOffset());
        Assertions.assertNull(projectQuerySpec.getLimit());
    }

    @Test
    public void testFindAllOrderByAsc() {
        QuerySpec expectedSpec = new QuerySpec(Task.class);
        expectedSpec.addSort(new SortSpec(Arrays.asList("name"), Direction.ASC));

        Map<String, Set<String>> params = new HashMap<>();
        add(params, "sort", "name");
        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params, queryContext);
        Assertions.assertEquals(expectedSpec, actualSpec);

        Map<String, Set<String>> serialized = urlMapper.serialize(actualSpec, queryContext);
        Assertions.assertEquals(serialized, params);
    }

    @Test
    public void testFollowNestedObjectWithinResource() {
        // follow ProjectData.data
        QuerySpec expectedSpec = new QuerySpec(Task.class);
        expectedSpec.addSort(new SortSpec(Arrays.asList("data", "data"), Direction.ASC));

        ResourceInformation projectInformation = resourceRegistry.getEntry(Project.class).getResourceInformation();

        Map<String, Set<String>> params = new HashMap<>();
        add(params, "sort", "data.data");
        QuerySpec actualSpec = urlMapper.deserialize(projectInformation, params, queryContext);
        Assertions.assertEquals(expectedSpec, actualSpec);

        Map<String, Set<String>> serialized = urlMapper.serialize(actualSpec, queryContext);
        Assertions.assertEquals(new HashSet(Arrays.asList("data.data")), serialized.get("sort"));
    }

    @Test
    public void testOrderByMultipleAttributes() {
        QuerySpec expectedSpec = new QuerySpec(Task.class);
        expectedSpec.addSort(new SortSpec(Arrays.asList("name"), Direction.ASC));
        expectedSpec.addSort(new SortSpec(Arrays.asList("id"), Direction.ASC));

        Map<String, Set<String>> params = new HashMap<>();
        add(params, "sort", "name,id");
        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params, queryContext);
        Assertions.assertEquals(expectedSpec, actualSpec);

        Map<String, Set<String>> serialized = urlMapper.serialize(actualSpec, queryContext);
        Assertions.assertEquals(serialized, params);
    }

    @Test
    public void testFindAllOrderByDesc() {
        QuerySpec expectedSpec = new QuerySpec(Task.class);
        expectedSpec.addSort(new SortSpec(Arrays.asList("name"), Direction.DESC));

        Map<String, Set<String>> params = new HashMap<>();
        add(params, "sort", "-name");

        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params, queryContext);
        Assertions.assertEquals(expectedSpec, actualSpec);

        Map<String, Set<String>> serialized = urlMapper.serialize(actualSpec, queryContext);
        Assertions.assertEquals(serialized, params);
    }

    @Test
    public void testFilterWithDefaultOp() {
        QuerySpec expectedSpec = new QuerySpec(Task.class);
        expectedSpec.addFilter(new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, "value"));

        Map<String, Set<String>> params = new HashMap<>();
        add(params, "filter[tasks][name]", "value");

        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params, queryContext);
        Assertions.assertEquals(expectedSpec, actualSpec);
    }

    @Test
    public void testFilterWithPrettyJson() {
        String expectedJson = "{ \"OR\": [ {\"id\": [12, 13, 14]}, {\"completed\": true} ] }";
        QuerySpec jsonSpec = new QuerySpec(Task.class);
        jsonSpec.addFilter(new FilterSpec(expectedJson));

        Map<String, Set<String>> expectedParams = new HashMap<>();
        add(expectedParams, "filter", "{\"OR\":[{\"id\":[12,13,14]},{\"completed\":true}]}");

        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, expectedParams, queryContext);

        FilterSpec idFilterSpec = PathSpec.of("id").filter(FilterOperator.EQ, Arrays.asList(12L, 13L, 14L));
        FilterSpec completedFilterSpec = PathSpec.of("completed").filter(FilterOperator.EQ, true);
        QuerySpec expectedQuerySpec = new QuerySpec(Task.class);
        expectedQuerySpec.addFilter(FilterSpec.or(idFilterSpec, completedFilterSpec));
        Assertions.assertEquals(expectedQuerySpec.getFilters(), actualSpec.getFilters());
        Assertions.assertEquals(expectedQuerySpec, actualSpec);

        Map<String, Set<String>> actualParams = urlMapper.serialize(actualSpec, queryContext);
        Assertions.assertEquals(expectedParams, actualParams);
    }

    @Test
    public void testFilterSpecJsonWithDefaultOperator() {
        String expectedJson = "[{ \"path\": \"completed\", \"value\": true }]";
        QuerySpec jsonSpec = new QuerySpec(Task.class);
        jsonSpec.addFilter(new FilterSpec(expectedJson));

        Map<String, Set<String>> expectedParams = new HashMap<>();
        add(expectedParams, "filter", expectedJson);

        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, expectedParams, queryContext);
        FilterSpec actualFilter = actualSpec.getFilters().get(0);
        Assertions.assertEquals(PathSpec.of("completed"), actualFilter.getPath());
        Assertions.assertEquals(FilterOperator.EQ, actualFilter.getOperator());
        Assertions.assertEquals(Boolean.TRUE, actualFilter.getValue());
    }

    @Test
    public void testFilterSpecJsonWithOperator() {
        String expectedJson = "[{ \"path\": \"completed\", \"operator\": \"NEQ\", \"value\": true }]";
        QuerySpec jsonSpec = new QuerySpec(Task.class);
        jsonSpec.addFilter(new FilterSpec(expectedJson));

        Map<String, Set<String>> expectedParams = new HashMap<>();
        add(expectedParams, "filter", expectedJson);

        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, expectedParams, queryContext);
        FilterSpec actualFilter = actualSpec.getFilters().get(0);
        Assertions.assertEquals(PathSpec.of("completed"), actualFilter.getPath());
        Assertions.assertEquals(FilterOperator.NEQ, actualFilter.getOperator());
        Assertions.assertEquals(Boolean.TRUE, actualFilter.getValue());
    }

    @Test
    public void testFilterSpecJsonWithNestedFilters() {
        String expectedJson = "[{ \"operator\": \"OR\", \"expression\": [{ \"path\": \"completed\", \"operator\": \"NEQ\", \"value\": true }] }]";
        QuerySpec jsonSpec = new QuerySpec(Task.class);
        jsonSpec.addFilter(new FilterSpec(expectedJson));

        Map<String, Set<String>> expectedParams = new HashMap<>();
        add(expectedParams, "filter", expectedJson);

        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, expectedParams, queryContext);
        FilterSpec actualFilter = actualSpec.getFilters().get(0);
        Assertions.assertEquals(FilterOperator.OR, actualFilter.getOperator());

        Assertions.assertEquals(1, actualFilter.getExpression().size());
        FilterSpec nestedFilter = actualFilter.getExpression().get(0);
        Assertions.assertEquals(PathSpec.of("completed"), nestedFilter.getPath());
        Assertions.assertEquals(FilterOperator.NEQ, nestedFilter.getOperator());
        Assertions.assertEquals(Boolean.TRUE, nestedFilter.getValue());
    }

    @Test
    public void testFilterOnRelatedWithJson() {
        Map<String, Set<String>> expectedParams = new HashMap<>();
        add(expectedParams, "filter[projects]", "{\"OR\":[{\"id\":[12,13,14]},{\"name\":\"Test\"}]}");

        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, expectedParams, queryContext);
        Assertions.assertTrue(actualSpec.getFilters().isEmpty(), "must filter on related");
        QuerySpec actualRelatedSpec = actualSpec.getQuerySpec(Project.class);
        Assertions.assertNotNull(actualRelatedSpec);

        FilterSpec idFilterSpec = PathSpec.of("id").filter(FilterOperator.EQ, Arrays.asList(12L, 13L, 14L));
        FilterSpec nameFilterSpec = PathSpec.of("name").filter(FilterOperator.EQ, "Test");
        QuerySpec expectedRelatedQuerySpec = new QuerySpec(Project.class);
        expectedRelatedQuerySpec.addFilter(FilterSpec.or(idFilterSpec, nameFilterSpec));
        QuerySpec expectedQuerySpec = new QuerySpec(Task.class);
        expectedQuerySpec.putRelatedSpec(Project.class, expectedRelatedQuerySpec);

        Assertions.assertEquals(expectedQuerySpec.getFilters(), actualSpec.getFilters());
        Assertions.assertEquals(expectedRelatedQuerySpec.getFilters(), actualRelatedSpec.getFilters());
        Assertions.assertEquals(expectedRelatedQuerySpec, actualRelatedSpec);

        Map<String, Set<String>> actualParams = urlMapper.serialize(actualSpec, queryContext);
        Assertions.assertEquals(expectedParams, actualParams);
    }

    @Test
    public void testFilterByNull() {
        QuerySpec expectedSpec = new QuerySpec(Task.class);
        expectedSpec.addFilter(new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, null));

        Map<String, Set<String>> params = new HashMap<>();
        add(params, "filter[name]", "null");

        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params, queryContext);
        Assertions.assertEquals(expectedSpec, actualSpec);
    }

    @Test
    public void testFilterWithComputedAttribute() {
        // if computeAttribte is not found, module order is wrong
        // tests setup with information builder must be hardened
        QuerySpec expectedSpec = new QuerySpec(Task.class);
        expectedSpec.addFilter(new FilterSpec(Arrays.asList("computedAttribute"), FilterOperator.EQ, 13));

        Map<String, Set<String>> params = new HashMap<>();
        add(params, "filter[tasks][computedAttribute]", "13");

        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params, queryContext);
        Assertions.assertEquals(expectedSpec, actualSpec);
    }

    @Test
    public void testFilterWithDotNotation() {
        QuerySpec expectedSpec = new QuerySpec(Task.class);
        expectedSpec.addFilter(new FilterSpec(Arrays.asList("project", "name"), FilterOperator.EQ, "value"));

        Map<String, Set<String>> params = new HashMap<>();
        add(params, "filter[project.name]", "value");

        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params, queryContext);
        Assertions.assertEquals(expectedSpec, actualSpec);

        Map<String, Set<String>> serialized = urlMapper.serialize(actualSpec, queryContext);
        Assertions.assertEquals(new HashSet(Arrays.asList("value")), serialized.get("filter[project.name]"));
    }

    @Test
    public void testFilterWithCommaSeparation() {
        Assertions.assertTrue(urlMapper.getAllowCommaSeparatedValue());
        HashSet<String> values = new HashSet<>();
        values.add("john");
        values.add("jane");
        QuerySpec expectedSpec = new QuerySpec(Task.class);
        expectedSpec.addFilter(new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, values));

        Map<String, Set<String>> params = new HashMap<>();
        add(params, "filter[name]", "john,jane");

        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params, queryContext);
        Assertions.assertEquals(expectedSpec.getFilters(), actualSpec.getFilters());
    }

    @Test
    public void testFilterWithoutCommaSeparation() {
        urlMapper.setAllowCommaSeparatedValue(false);
        QuerySpec expectedSpec = new QuerySpec(Task.class);
        expectedSpec.addFilter(new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, "john,jane"));

        Map<String, Set<String>> params = new HashMap<>();
        add(params, "filter[name]", "john,jane");

        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params, queryContext);
        Assertions.assertEquals(expectedSpec.getFilters(), actualSpec.getFilters());
    }

    @Test
    public void testFilterWithDotNotationMultipleElements() {
        QuerySpec expectedSpec = new QuerySpec(Task.class);
        expectedSpec.addFilter(new FilterSpec(Arrays.asList("project", "task", "name"), FilterOperator.EQ, "value"));

        Map<String, Set<String>> params = new HashMap<>();
        add(params, "filter[project.task.name]", "value");

        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params, queryContext);

        Assertions.assertEquals(expectedSpec, actualSpec);

        Map<String, Set<String>> serialized = urlMapper.serialize(actualSpec, queryContext);
        Assertions.assertEquals(new HashSet(Arrays.asList("value")), serialized.get("filter[project.task.name]"));
    }

    @Test
    public void testUnknownPropertyAllowed() {
        QuerySpec expectedSpec = new QuerySpec(Task.class);
        expectedSpec.addFilter(new FilterSpec(Arrays.asList("doesNotExists"), FilterOperator.EQ, "value"));

        urlMapper.setAllowUnknownAttributes(true);

        Map<String, Set<String>> params = new HashMap<>();
        add(params, "filter[tasks][doesNotExists]", "value");

        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params, queryContext);
        Assertions.assertEquals(expectedSpec, actualSpec);
    }

    @Test
    public void testUnknownPropertyNotAllowed() {
		Assertions.assertThrows(BadRequestException.class, () -> {
	        QuerySpec expectedSpec = new QuerySpec(Task.class);
	        expectedSpec.addFilter(new FilterSpec(Arrays.asList("doesNotExists"), FilterOperator.EQ, "value"));

	        urlMapper.setAllowUnknownAttributes(false);

	        Map<String, Set<String>> params = new HashMap<>();
	        add(params, "filter[tasks][doesNotExists]", "value");

	        urlMapper.deserialize(taskInformation, params, queryContext);
		});
    }

    @Test
    public void testUnknownParameterAllowed() {
        urlMapper.setAllowUnknownParameters(true);

        Map<String, Set<String>> params = new HashMap<>();
        add(params, "doesNotExists[tasks]", "value");

        Assertions.assertNotNull(urlMapper.deserialize(taskInformation, params, queryContext));
    }

    @Test
    public void testUnknownParameterNotAllowed() {
		Assertions.assertThrows(ParametersDeserializationException.class, () -> {
	        urlMapper.setAllowUnknownParameters(false);

	        Map<String, Set<String>> params = new HashMap<>();
	        add(params, "doesNotExists[tasks]", "value");

	        urlMapper.deserialize(taskInformation, params, queryContext);
		});
    }

    @Test
    public void testFilterByOne() {
        QuerySpec expectedSpec = new QuerySpec(Task.class);
        expectedSpec.addFilter(new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, "value"));

        Map<String, Set<String>> params = new HashMap<>();
        add(params, "filter[tasks][name][EQ]", "value");

        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params, queryContext);
        Assertions.assertEquals(expectedSpec, actualSpec);
    }

    @Test
    public void testFilterByMany() {
        QuerySpec expectedSpec = new QuerySpec(Task.class);
        expectedSpec.addFilter(
                new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, new HashSet<>(Arrays.asList("value1", "value2"))));

        Map<String, Set<String>> params = new HashMap<>();
        params.put("filter[tasks][name][EQ]", new HashSet<>(Arrays.asList("value1", "value2")));

        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params, queryContext);
        Assertions.assertEquals(expectedSpec, actualSpec);
    }

    @Test
    public void testFilterEquals() {
        QuerySpec expectedSpec = new QuerySpec(Task.class);
        expectedSpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.EQ, 1L));

        Map<String, Set<String>> params = new HashMap<>();
        add(params, "filter[tasks][id][EQ]", "1");

        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params, queryContext);
        Assertions.assertEquals(expectedSpec, actualSpec);
    }

    @Test
    public void testFilterGreater() {
        QuerySpec expectedSpec = new QuerySpec(Task.class);
        expectedSpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.LE, 1L));

        Map<String, Set<String>> params = new HashMap<>();
        add(params, "filter[tasks][id][LE]", "1");

        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params, queryContext);
        Assertions.assertEquals(expectedSpec, actualSpec);
    }

    @Test
    public void testFilterGreaterOnRoot() {
        QuerySpec expectedSpec = new QuerySpec(Task.class);
        expectedSpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.LE, 1L));

        Map<String, Set<String>> params = new HashMap<>();
        add(params, "filter[id][LE]", "1");

        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params, queryContext);
        Assertions.assertEquals(expectedSpec, actualSpec);
    }

    @Test
    public void testPaging() {
        QuerySpec expectedSpec = new QuerySpec(Task.class);
        expectedSpec.setPaging(new OffsetLimitPagingSpec(1L, 2L));

        Map<String, Set<String>> params = new HashMap<>();
        add(params, "page[offset]", "1");
        add(params, "page[limit]", "2");

        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params, queryContext);
        Assertions.assertEquals(expectedSpec, actualSpec);
    }

    @Test
    public void deserializeUnknownParameter() {
        Map<String, Set<String>> params = new HashMap<>();
        add(params, "doesNotExist[something]", "someValue");

        final boolean[] deserialized = new boolean[1];
        urlMapper = new DefaultQuerySpecUrlMapper() {

            @Override
            protected void deserializeUnknown(final QuerySpec querySpec, final QueryParameter parameter) {
                Assertions.assertEquals(QueryParameterType.UNKNOWN, parameter.getType());
                Assertions.assertEquals("doesNotExist[something]", parameter.getName());
                Assertions.assertNull(parameter.getResourceInformation());
                Assertions.assertEquals(FilterOperator.EQ, parameter.getOperator());
                Assertions.assertNull(parameter.getAttributePath());
                Assertions.assertEquals(1, parameter.getValues().size());
                deserialized[0] = true;
            }
        };
        urlMapper.init(deserializerContext);
        urlMapper.deserialize(taskInformation, params, queryContext);
        Assertions.assertTrue(deserialized[0]);
    }


    @Test
    public void testInvalidPagingType() {
		Assertions.assertThrows(ParametersDeserializationException.class, () -> {
	        Map<String, Set<String>> params = new HashMap<>();
	        add(params, "page[doesNotExist]", "1");
	        urlMapper.deserialize(taskInformation, params, queryContext);
		});
    }

    @Test
    public void testPagingError() {
		Assertions.assertThrows(ParametersDeserializationException.class, () -> {
	        QuerySpec expectedSpec = new QuerySpec(Task.class);
	        expectedSpec.setLimit(2L);
	        expectedSpec.setOffset(1L);

	        Map<String, Set<String>> params = new HashMap<>();
	        add(params, "page[offset]", "notANumber");
	        add(params, "page[limit]", "2");

	        urlMapper.deserialize(taskInformation, params, queryContext);
		});
    }

    @Test
    public void testPagingMaxLimitNotAllowed() {
        Map<String, Set<String>> params = new HashMap<>();
        add(params, "page[offset]", "1");
        add(params, "page[limit]", "30");

        // RCS deprecated
        // expectedException.expect(BadRequestException.class);
        Assertions.assertThrows(BadRequestException.class, () -> {
            urlMapper.deserialize(taskWithPagingBehaviorInformation, params, queryContext);
        });
    }

    @Test
    public void testPagingMaxLimitAllowed() {
        QuerySpec expectedSpec = new QuerySpec(Task.class);
        expectedSpec.setOffset(1L);
        expectedSpec.setLimit(5L);

        Map<String, Set<String>> params = new HashMap<>();
        add(params, "page[offset]", "1");
        add(params, "page[limit]", "5");

        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params, queryContext);
        Assertions.assertEquals(expectedSpec, actualSpec);
    }

    @Test
    public void testIncludeRelations() {
        QuerySpec expectedSpec = new QuerySpec(Task.class);
        expectedSpec.includeRelation(Arrays.asList("project"));

        Map<String, Set<String>> params = new HashMap<>();
        add(params, "include[tasks]", "project");

        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params, queryContext);
        Assertions.assertEquals(expectedSpec, actualSpec);
    }

    @Test
    public void testIncludeRelationsOnRoot() {
        QuerySpec expectedSpec = new QuerySpec(Task.class);
        expectedSpec.includeRelation(Arrays.asList("project"));

        Map<String, Set<String>> params = new HashMap<>();
        add(params, "include", "project");

        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params, queryContext);
        Assertions.assertEquals(expectedSpec, actualSpec);
    }

    @Test
    public void testIncludeAttributes() {
        QuerySpec expectedSpec = new QuerySpec(Task.class);
        expectedSpec.includeField(Arrays.asList("name"));

        Map<String, Set<String>> params = new HashMap<>();
        add(params, "fields[tasks]", "name");

        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params, queryContext);
        Assertions.assertEquals(expectedSpec, actualSpec);
    }

    @Test
    public void testIncludeAttributesOnRoot() {
        QuerySpec expectedSpec = new QuerySpec(Task.class);
        expectedSpec.includeField(Arrays.asList("name"));

        Map<String, Set<String>> params = new HashMap<>();
        add(params, "fields", "name");

        QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params, queryContext);
        Assertions.assertEquals(expectedSpec, actualSpec);
    }

    @Test
    public void testHyphenIsAllowedInResourceName() {

        QuerySpec expectedSpec = new QuerySpec(Task.class);
        expectedSpec.addSort(new SortSpec(Arrays.asList("id"), Direction.ASC));

        Map<String, Set<String>> params = new HashMap<>();
        add(params, "sort[task-with-lookup]", "id");

        ResourceInformation taskWithLookUpInformation =
                resourceRegistry.getEntry(TaskWithLookup.class).getResourceInformation();
        QuerySpec actualSpec = urlMapper.deserialize(taskWithLookUpInformation, params, queryContext);
        Assertions.assertEquals(expectedSpec, actualSpec);
    }

    @Test
    public void testIngoreParseException() {
        Map<String, Set<String>> params = new HashMap<>();
        add(params, "filter[id]", "NotAnId");
        urlMapper.setIgnoreParseExceptions(true);
        QuerySpec querySpec = urlMapper.deserialize(taskInformation, params, queryContext);
        Assertions.assertEquals(Task.class, querySpec.getResourceClass());
        Assertions.assertEquals(Arrays.asList("id"), querySpec.getFilters().get(0).getAttributePath());
        Assertions.assertEquals("NotAnId", querySpec.getFilters().get(0).getValue());
        Assertions.assertNull(querySpec.getQuerySpec(Project.class));
    }

    @Test
    public void testGenericCast() {
        Map<String, Set<String>> params = new HashMap<>();
        add(params, "filter[id]", "12");
        add(params, "filter[name]", "test");
        add(params, "filter[completed]", "true");
        urlMapper.setIgnoreParseExceptions(false);
        QuerySpec querySpec = urlMapper.deserialize(taskInformation, params, queryContext);
        Assertions.assertEquals(Task.class, querySpec.getResourceClass());
        Assertions.assertEquals(Arrays.asList("id"), querySpec.getFilters().get(2).getAttributePath());
        Long id = querySpec.getFilters().get(2).getValue();
        Assertions.assertEquals(Long.valueOf(12), id);
        String name = querySpec.getFilters().get(0).getValue();
        Assertions.assertEquals("test", name);
        Boolean completed = querySpec.getFilters().get(1).getValue();
        Assertions.assertEquals(Boolean.TRUE, completed);
        Assertions.assertNull(querySpec.getQuerySpec(Project.class));
    }

    @Test
    public void testFailOnParseException() {
		Assertions.assertThrows(ParametersDeserializationException.class, () -> {
	        Map<String, Set<String>> params = new HashMap<>();
	        add(params, "filter[id]", "NotAnId");
	        urlMapper.setIgnoreParseExceptions(false);
	        urlMapper.deserialize(taskInformation, params, queryContext);
		});
    }

    @Test
    public void testUnknownProperty() {
		Assertions.assertThrows(ParametersDeserializationException.class, () -> {
	        Map<String, Set<String>> params = new HashMap<>();
	        add(params, "group", "test");
	        urlMapper.setIgnoreParseExceptions(false);
	        urlMapper.deserialize(taskInformation, params, queryContext);
		});
    }

    protected void add(Map<String, Set<String>> params, String key, String... value) {
        params.put(key, new HashSet<>(Arrays.asList(value)));
    }
}
