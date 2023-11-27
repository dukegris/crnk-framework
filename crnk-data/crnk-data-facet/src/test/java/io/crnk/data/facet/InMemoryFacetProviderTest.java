package io.crnk.data.facet;

import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.data.facet.internal.FacetRepositoryImpl;
import io.crnk.data.facet.setup.FacetTestSetup;
import io.crnk.data.facet.setup.FacetedProject;
import io.crnk.data.facet.setup.FacetedProjectRepository;
import io.crnk.data.facet.setup.FacetedTask;
import io.crnk.test.mock.TestModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

public class InMemoryFacetProviderTest {

    private FacetTestSetup setup;

    private FacetRepositoryImpl repository;

    @BeforeEach
    public void setup() {
        setup = new FacetTestSetup();
        setup.boot();
        repository = setup.getRepository();
    }

    @AfterEach
    public void teardown() {
        TestModule.clear();
    }

    @Test
    public void checkFindAll() {
        QuerySpec querySpec = new QuerySpec(FacetResource.class);
        ResourceList<FacetResource> list = repository.findAll(querySpec);
        Assertions.assertEquals(2, list.size());

        FacetResource nameFacets = list.get(0);
        Assertions.assertEquals("projects_name", nameFacets.getId());
        Assertions.assertEquals("projects", nameFacets.getResourceType());
        Assertions.assertEquals("name", nameFacets.getName());
        Assertions.assertEquals(Arrays.asList("project3", "project2", "project1", "project0"), nameFacets.getLabels());
        Map<String, FacetValue> values = nameFacets.getValues();
        FacetValue value0 = values.get("project0");
        Assertions.assertEquals(1, value0.getCount());
        FacetValue value1 = values.get("project1");
        Assertions.assertEquals(3, value1.getCount());
        FacetValue value2 = values.get("project2");
        Assertions.assertEquals(5, value2.getCount());
        FacetValue value3 = values.get("project3");
        Assertions.assertEquals(7, value3.getCount());
        Assertions.assertEquals("project3", value3.getLabel());
        Assertions.assertEquals(PathSpec.of("name").filter(FilterOperator.EQ, "project3"), value3.getFilterSpec());
        Assertions.assertEquals("project3", value3.getValue());

        FacetResource priorityFacet = list.get(1);
        Assertions.assertEquals("projects_priority", priorityFacet.getId());
        Assertions.assertEquals("projects", priorityFacet.getResourceType());
        Assertions.assertEquals("priority", priorityFacet.getName());
        Assertions.assertEquals(2, priorityFacet.getValues().size());
    }

    @Test
    public void checkValuesSortedByTotalCount() {
        QuerySpec querySpec = new QuerySpec(FacetResource.class);
        ResourceList<FacetResource> list = repository.findAll(querySpec);
        Assertions.assertEquals(2, list.size());

        FacetResource nameFacets = list.get(0);
        Assertions.assertEquals("projects_name", nameFacets.getId());
        Assertions.assertEquals("projects", nameFacets.getResourceType());
        Assertions.assertEquals("name", nameFacets.getName());
        Assertions.assertEquals(Arrays.asList("project3", "project2", "project1", "project0"), nameFacets.getLabels());
        Map<String, FacetValue> values = nameFacets.getValues();
        Iterator<Map.Entry<String, FacetValue>> iterator = values.entrySet().iterator();

        // must be sorted by count!
        Map.Entry<String, FacetValue> entry3 = iterator.next();
        Map.Entry<String, FacetValue> entry2 = iterator.next();
        Map.Entry<String, FacetValue> entry1 = iterator.next();
        Map.Entry<String, FacetValue> entry0 = iterator.next();

        Assertions.assertEquals("project0", entry0.getKey());
        Assertions.assertEquals(1, entry0.getValue().getCount());
        Assertions.assertEquals("project1", entry1.getKey());
        Assertions.assertEquals(3, entry1.getValue().getCount());
        Assertions.assertEquals("project2", entry2.getKey());
        Assertions.assertEquals(5, entry2.getValue().getCount());
        Assertions.assertEquals("project3", entry3.getKey());
        Assertions.assertEquals(7, entry3.getValue().getCount());
    }

    @Test
    public void checkFacetOrder1() {
        QuerySpec querySpec = new QuerySpec(FacetResource.class);
        querySpec.addFilter(PathSpec.of(FacetResource.ATTR_NAME).filter(FilterOperator.EQ, Arrays.asList("name", "priority")));
        ResourceList<FacetResource> list = repository.findAll(querySpec);
        Assertions.assertEquals(2, list.size());

        FacetResource nameFacets = list.get(0);
        Assertions.assertEquals("projects", nameFacets.getResourceType());
        Assertions.assertEquals("name", nameFacets.getName());

        FacetResource priorityFacet = list.get(1);
        Assertions.assertEquals("projects", priorityFacet.getResourceType());
        Assertions.assertEquals("priority", priorityFacet.getName());
    }

    @Test
    public void checkFacetOrder2() {
        QuerySpec querySpec = new QuerySpec(FacetResource.class);
        querySpec.addFilter(PathSpec.of(FacetResource.ATTR_NAME).filter(FilterOperator.EQ, Arrays.asList("priority", "name")));
        ResourceList<FacetResource> list = repository.findAll(querySpec);
        Assertions.assertEquals(2, list.size());

        FacetResource priorityFacet = list.get(0);
        Assertions.assertEquals("projects", priorityFacet.getResourceType());
        Assertions.assertEquals("priority", priorityFacet.getName());

        FacetResource nameFacets = list.get(1);
        Assertions.assertEquals("projects", nameFacets.getResourceType());
        Assertions.assertEquals("name", nameFacets.getName());
    }


    @Test
    public void checkNestedFacetFilter() {
        QuerySpec querySpec = new QuerySpec(FacetResource.class);
        querySpec.addFilter(PathSpec.of(FacetResource.ATTR_NAME).filter(FilterOperator.EQ, Arrays.asList("priority", "name")));
        querySpec.addFilter(PathSpec.of(FacetResource.ATTR_VALUES, "priority").filter(FilterOperator.SELECT, "1"));
        ResourceList<FacetResource> list = repository.findAll(querySpec);
        Assertions.assertEquals(2, list.size());

        FacetResource priorityFacet = list.get(0);
        Assertions.assertEquals("projects", priorityFacet.getResourceType());
        Assertions.assertEquals("priority", priorityFacet.getName());
        Assertions.assertEquals(2, priorityFacet.getValues().size());

        // filtered by priority, counts reduced accordingly
        FacetResource nameFacets = list.get(1);
        Assertions.assertEquals("projects", nameFacets.getResourceType());
        Assertions.assertEquals("name", nameFacets.getName());

        Map<String, FacetValue> values = nameFacets.getValues();
        FacetValue value1 = values.get("project1");
        Assertions.assertEquals(2, value1.getCount());
        FacetValue value2 = values.get("project2");
        Assertions.assertEquals(2, value2.getCount());
        FacetValue value3 = values.get("project3");
        Assertions.assertEquals(4, value3.getCount());
    }

    @Test
    public void checkNestedFacetFilterAllSelected() {
        QuerySpec querySpec = new QuerySpec(FacetResource.class);
        querySpec.addFilter(PathSpec.of(FacetResource.ATTR_NAME).filter(FilterOperator.EQ, Arrays.asList("priority", "name")));
        querySpec.addFilter(PathSpec.of(FacetResource.ATTR_VALUES, "priority").filter(FilterOperator.SELECT, Arrays.asList("0", "1")));
        ResourceList<FacetResource> list = repository.findAll(querySpec);
        Assertions.assertEquals(2, list.size());

        FacetResource priorityFacet = list.get(0);
        Assertions.assertEquals("projects", priorityFacet.getResourceType());
        Assertions.assertEquals("priority", priorityFacet.getName());
        Assertions.assertEquals(2, priorityFacet.getValues().size());

        // filtered by priority, counts reduced accordingly
        FacetResource nameFacets = list.get(1);
        Assertions.assertEquals("projects", nameFacets.getResourceType());
        Assertions.assertEquals("name", nameFacets.getName());

        Map<String, FacetValue> values = nameFacets.getValues();
        FacetValue value1 = values.get("project1");
        Assertions.assertEquals(3, value1.getCount());
        FacetValue value2 = values.get("project2");
        Assertions.assertEquals(5, value2.getCount());
        FacetValue value3 = values.get("project3");
        Assertions.assertEquals(7, value3.getCount());
    }

    @Test
    public void checkGrouping() {
        QuerySpec querySpec = new QuerySpec(FacetResource.class);
        querySpec.addFilter(PathSpec.of("values").filter(FilterOperator.GROUP, "priority"));
        ResourceList<FacetResource> list = repository.findAll(querySpec);
        Assertions.assertEquals(4, list.size());

        // groups must come first
        FacetResource priorityFacet0 = list.get(0);
        Assertions.assertEquals("projects_priority_0", priorityFacet0.getId());
        Assertions.assertEquals("projects", priorityFacet0.getResourceType());
        Assertions.assertEquals("priority", priorityFacet0.getName());
        Assertions.assertEquals(Arrays.asList("0"), priorityFacet0.getLabels());
        Assertions.assertEquals("0", priorityFacet0.getGroups().get("priority"));
        Assertions.assertEquals(8, priorityFacet0.getValues().get("0").getCount());

        FacetResource priorityFacet1 = list.get(1);
        Assertions.assertEquals("projects_priority_1", priorityFacet1.getId());
        Assertions.assertEquals("projects", priorityFacet1.getResourceType());
        Assertions.assertEquals("priority", priorityFacet1.getName());
        Assertions.assertEquals(Arrays.asList("1"), priorityFacet1.getLabels());
        Assertions.assertEquals("1", priorityFacet1.getGroups().get("priority"));
        Assertions.assertEquals(8, priorityFacet1.getValues().get("1").getCount());

        FacetResource nameFacet = list.get(2);
        Assertions.assertEquals("name", nameFacet.getName());
        Assertions.assertEquals(4, nameFacet.getValues().size());
        Assertions.assertTrue(nameFacet.getValues().containsKey("project0"));
        Assertions.assertTrue(nameFacet.getValues().containsKey("project1"));
        Assertions.assertTrue(nameFacet.getValues().containsKey("project2"));
        Assertions.assertTrue(nameFacet.getValues().containsKey("project3"));

    }

    @Test
    public void checkNull() {
        RegistryEntry entry = setup.getBoot().getResourceRegistry().getEntry(FacetedProject.class);
        FacetedProjectRepository projectRepository = (FacetedProjectRepository) entry.getResourceRepository().getImplementation();
        projectRepository.clear();

        FacetedProject project = new FacetedProject();
        project.setId(12L);
        project.setPriority(1);
        projectRepository.create(project);

        QuerySpec querySpec = new QuerySpec(FacetResource.class);
        ResourceList<FacetResource> list = repository.findAll(querySpec);

        FacetResource nameFacet = list.get(0);
        Assertions.assertEquals("name", nameFacet.getName());
        Assertions.assertEquals(1, nameFacet.getValues().size());
        Assertions.assertTrue(nameFacet.getValues().containsKey("null"));
        FacetValue value = nameFacet.getValues().get("null");
        Assertions.assertEquals(1, value.getCount());
        Assertions.assertEquals(PathSpec.of("name").filter(FilterOperator.EQ, null), value.getFilterSpec());
    }

    @Test
    public void checkNotExposedNotFacetted() {
        QuerySpec querySpec = new QuerySpec(FacetResource.class);
        querySpec.addFilter(PathSpec.of(FacetResource.ATTR_RESOURCE_TYPE).filter(FilterOperator.EQ, FacetedTask.RESOURCE_TYPE));
        ResourceList<FacetResource> list = repository.findAll(querySpec);
        Assertions.assertTrue(list.isEmpty());
    }

    @Test
    public void checkNestedGrouping() {
        QuerySpec querySpec = new QuerySpec(FacetResource.class);
        querySpec.addFilter(PathSpec.of("values").filter(FilterOperator.GROUP, Arrays.asList("name", "priority")));
        querySpec.addSort(PathSpec.of("name").sort(Direction.ASC));
        querySpec.addSort(PathSpec.of("id").sort(Direction.ASC));
        ResourceList<FacetResource> list = repository.findAll(querySpec);
        Assertions.assertEquals(11, list.size());

        // 0, 1, 1, 1, 2, 2, 2, 2, 2, 3 => Math.sqrt in setup
        int[] counts = {1, 3, 5, 7};

        for (int i = 0; i < 3; i++) {
            FacetResource nameFacet = list.get(i);
            String name = nameFacet.getLabels().get(0);
            Assertions.assertEquals(1, nameFacet.getGroups().size());
            Assertions.assertEquals(1, nameFacet.getLabels().size());
            Assertions.assertEquals(1, nameFacet.getValues().size());
            Assertions.assertEquals("projects_name_" + name, nameFacet.getId());
            int expectedCount = counts[i];
            Assertions.assertEquals(expectedCount, nameFacet.getValues().get(name).getCount());
        }


        for (int i = 0; i < 7; i++) {
            FacetResource priorityFacet = list.get(4 + i);

            Assertions.assertEquals(1, priorityFacet.getLabels().size());
            Assertions.assertEquals(1, priorityFacet.getValues().size());
            Assertions.assertEquals(2, priorityFacet.getGroups().size());
            int group = Integer.parseInt(priorityFacet.getGroups().get("name").substring("project".length()));
            String priority = priorityFacet.getLabels().get(0);
            Assertions.assertTrue(priorityFacet.getGroups().containsKey("priority"));
            long count = priorityFacet.getValues().get(priority).getCount();
            Assertions.assertEquals("projects_name_project" + group + "_priority_" + priority, priorityFacet.getId());

            Assertions.assertTrue(count == counts[group] / 2 || count == counts[group] / 2 + 1);
        }
    }


    @Test
    public void checkInvalidGroupNameThrowsException() {
        Assertions.assertThrows(BadRequestException.class, () -> {
        QuerySpec querySpec = new QuerySpec(FacetResource.class);
        querySpec.addFilter(PathSpec.of("values").filter(FilterOperator.GROUP, Arrays.asList("doesNotExist", "priority")));
        querySpec.addSort(PathSpec.of("name").sort(Direction.ASC));
        querySpec.addSort(PathSpec.of("id").sort(Direction.ASC));
        repository.findAll(querySpec);
        });
    }

    @Test
    public void matchFilterByType() {
        QuerySpec querySpec = new QuerySpec(FacetResource.class);
        querySpec.addFilter(PathSpec.of("resourceType").filter(FilterOperator.EQ, "projects"));
        ResourceList<FacetResource> list = repository.findAll(querySpec);
        Assertions.assertEquals(2, list.size());
    }

    @Test
    public void mismatchFilterByType() {
        QuerySpec querySpec = new QuerySpec(FacetResource.class);
        querySpec.addFilter(PathSpec.of("resourceType").filter(FilterOperator.EQ, "doesNotExist"));
        ResourceList<FacetResource> list = repository.findAll(querySpec);
        Assertions.assertEquals(0, list.size());
    }

    @Test
    public void matchFilterByFacet() {
        QuerySpec querySpec = new QuerySpec(FacetResource.class);
        querySpec.addFilter(PathSpec.of("name").filter(FilterOperator.EQ, "name"));
        ResourceList<FacetResource> list = repository.findAll(querySpec);
        Assertions.assertEquals(1, list.size());
    }

    @Test
    public void mismatchFilterByFacet() {
        QuerySpec querySpec = new QuerySpec(FacetResource.class);
        querySpec.addFilter(PathSpec.of("name").filter(FilterOperator.EQ, "doesNotExist"));
        ResourceList<FacetResource> list = repository.findAll(querySpec);
        Assertions.assertEquals(0, list.size());
    }
}
