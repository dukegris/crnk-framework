package io.crnk.core.queryspec;

import io.crnk.core.engine.document.Resource;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.queryspec.pagingspec.NumberSizePagingSpec;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpec;
import io.crnk.core.queryspec.pagingspec.PagingSpec;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

public class QuerySpecTest {

    @Test
    public void testEqualContract() {
        EqualsVerifier.forClass(QuerySpec.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void testGetOrCreate() {
        QuerySpec querySpec = new QuerySpec(Task.class, "tasks");
        Assertions.assertSame(querySpec, querySpec.getOrCreateQuerySpec(Task.class));
        Assertions.assertSame(querySpec, querySpec.getOrCreateQuerySpec("tasks"));
        Assertions.assertEquals(OffsetLimitPagingSpec.class, querySpec.getPaging().getClass());

        querySpec = new QuerySpec(Task.class, null);
        Assertions.assertSame(querySpec, querySpec.getOrCreateQuerySpec(Task.class));
        Assertions.assertNotSame(querySpec, querySpec.getOrCreateQuerySpec(Project.class));
        Assertions.assertEquals(OffsetLimitPagingSpec.class, querySpec.getPaging().getClass());

        querySpec = new QuerySpec(null, "tasks");
        Assertions.assertSame(querySpec, querySpec.getOrCreateQuerySpec("tasks"));
        Assertions.assertNotSame(querySpec, querySpec.getOrCreateQuerySpec("other"));
        Assertions.assertEquals(OffsetLimitPagingSpec.class, querySpec.getPaging().getClass());

        querySpec = new QuerySpec(null, "tasks");
        querySpec.setPaging(new NumberSizePagingSpec());
        Assertions.assertEquals(NumberSizePagingSpec.class, querySpec.getPaging().getClass());
    }

    @Test
    public void testCannotCreateResourceInstance() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new QuerySpec(Resource.class);
		});
    }


    @Test
    public void testCannotGetAndCreateWithResourceClass() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new QuerySpec(Task.class).getOrCreateQuerySpec(Resource.class);
		});
    }

    @Test
    public void testResourceClassIgnored() {
        QuerySpec querySpec = new QuerySpec(Resource.class, "tasks");
        assertNull(querySpec.getResourceClass());
    }


    @Test
    public void checkToString() {
        QuerySpec spec = new QuerySpec("projects");
        Assertions.assertEquals("QuerySpec[resourceType=projects, paging=OffsetLimitPagingSpec[offset=0]]", spec.toString());

        spec = new QuerySpec(Project.class);
        Assertions.assertEquals("QuerySpec[resourceClass=io.crnk.core.mock.models.Project, resourceType=projects, paging=OffsetLimitPagingSpec[offset=0]]",
                spec.toString());

        spec.addFilter(new FilterSpec(Arrays.asList("filterAttr"), FilterOperator.EQ, "test"));
        Assertions.assertEquals(
                "QuerySpec[resourceClass=io.crnk.core.mock.models.Project, resourceType=projects, paging=OffsetLimitPagingSpec[offset=0], "
                        + "filters=[filterAttr EQ test]]",
                spec.toString());

        spec.addSort(new SortSpec(Arrays.asList("sortAttr"), Direction.ASC));
        Assertions.assertEquals(
                "QuerySpec[resourceClass=io.crnk.core.mock.models.Project, resourceType=projects, paging=OffsetLimitPagingSpec[offset=0], "
                        + "filters=[filterAttr EQ test], sort=[sortAttr ASC]]",
                spec.toString());

        spec.includeField(Arrays.asList("includedField"));
        Assertions.assertEquals(
                "QuerySpec[resourceClass=io.crnk.core.mock.models.Project, resourceType=projects, paging=OffsetLimitPagingSpec[offset=0], "
                        + "filters=[filterAttr EQ test], sort=[sortAttr ASC], "
                        + "includedFields=[includedField]]",
                spec.toString());

        spec.includeRelation(Arrays.asList("includedRelation"));
        Assertions.assertEquals(
                "QuerySpec[resourceClass=io.crnk.core.mock.models.Project, resourceType=projects, paging=OffsetLimitPagingSpec[offset=0], "
                        + "filters=[filterAttr EQ test], sort=[sortAttr ASC], "
                        + "includedFields=[includedField], includedRelations=[includedRelation]]",
                spec.toString());

        spec.setPaging(new OffsetLimitPagingSpec(12L, 13L));
        Assertions.assertEquals(
                "QuerySpec[resourceClass=io.crnk.core.mock.models.Project, resourceType=projects, paging=OffsetLimitPagingSpec[offset=12, limit=13], "
                        + "filters=[filterAttr EQ test], "
                        + "sort=[sortAttr ASC], includedFields=[includedField], includedRelations=[includedRelation]]",
                spec.toString());
    }

    @Test
    public void testBasic() {
        FilterSpec filterSpec = new FilterSpec(Arrays.asList("filterAttr"), FilterOperator.EQ, "test");
        QuerySpec spec = new QuerySpec(Project.class);
        spec.addFilter(filterSpec);
        spec.addSort(new SortSpec(Arrays.asList("sortAttr"), Direction.ASC));
        spec.includeField(Arrays.asList("includedField"));
        spec.includeRelation(Arrays.asList("includedRelation"));

        Assertions.assertEquals(1, spec.getFilters().size());
        Assertions.assertEquals(filterSpec, spec.findFilter(PathSpec.of("filterAttr")).get());
        Assertions.assertEquals(1, spec.getSort().size());
        Assertions.assertEquals(1, spec.getIncludedFields().size());
        Assertions.assertEquals(1, spec.getIncludedRelations().size());
        spec.setFilters(new ArrayList<FilterSpec>());
        spec.setIncludedRelations(new ArrayList<IncludeRelationSpec>());
        spec.setIncludedFields(new ArrayList<IncludeFieldSpec>());
        spec.setSort(new ArrayList<SortSpec>());
        Assertions.assertEquals(0, spec.getFilters().size());
        Assertions.assertEquals(0, spec.getSort().size());
        Assertions.assertEquals(0, spec.getIncludedFields().size());
        Assertions.assertEquals(0, spec.getIncludedRelations().size());

        Assertions.assertEquals(0, spec.getNestedSpecs().size());
        QuerySpec relatedSpec = new QuerySpec(Task.class);
        spec.putRelatedSpec(Task.class, relatedSpec);
        Assertions.assertSame(relatedSpec, spec.getQuerySpec(Task.class));
        Assertions.assertEquals(1, spec.getNestedSpecs().size());
        spec.setNestedSpecs(new ArrayList<>());
    }


    @Test
    public void testFilterNotFound() {
        QuerySpec spec = new QuerySpec(Project.class);
        spec.addFilter(new FilterSpec(Arrays.asList("filterAttr"), FilterOperator.EQ, "test"));
        Optional<FilterSpec> filterSpec = spec.findFilter(PathSpec.of("unknown"));
        assertFalse(filterSpec.isPresent());
    }


    @Test
    public void testClone() {
        FilterSpec filterSpec = new FilterSpec(Arrays.asList("filterAttr"), FilterOperator.EQ, "test");
        SortSpec sortSpec = new SortSpec(Arrays.asList("sortAttr"), Direction.ASC);
        QuerySpec spec = new QuerySpec(Project.class);
        spec.addFilter(filterSpec);
        spec.addSort(sortSpec);
        spec.includeField(Arrays.asList("includedField"));
        spec.includeRelation(Arrays.asList("includedRelation"));
        spec.setLimit(2L);
        spec.setOffset(1L);

        QuerySpec duplicate = spec.clone();
        Assertions.assertNotSame(spec, duplicate);
        Assertions.assertNotSame(spec.getFilters().get(0), duplicate.getFilters().get(0));
        Assertions.assertNotSame(spec.getFilters().get(0).getPath(), duplicate.getFilters().get(0).getPath());
        Assertions.assertNotSame(spec.getSort(), duplicate.getSort());
        Assertions.assertNotSame(spec.getSort().get(0), duplicate.getSort().get(0));
        Assertions.assertNotSame(spec.getSort().get(0).getPath(), duplicate.getSort().get(0).getPath());
        Assertions.assertNotSame(spec.getIncludedFields(), duplicate.getIncludedFields());
        Assertions.assertNotSame(spec.getIncludedRelations(), duplicate.getIncludedRelations());
        Assertions.assertNotSame(spec.getIncludedRelations().get(0), duplicate.getIncludedRelations().get(0));
        Assertions.assertNotSame(spec.getPaging(), duplicate.getPaging());
        Assertions.assertEquals(spec, duplicate);
    }

    @Test
    public void testDuplicateWithRelations() {
        QuerySpec spec = new QuerySpec(Project.class);
        QuerySpec relatedSpec = new QuerySpec(Task.class);
        spec.putRelatedSpec(Task.class, relatedSpec);

        QuerySpec duplicate = spec.clone();
        Assertions.assertNotSame(spec, duplicate);
        Assertions.assertEquals(spec, duplicate);
        Assertions.assertNotSame(spec.getQuerySpec(Task.class), duplicate.getQuerySpec(Task.class));
        Assertions.assertEquals(spec.getQuerySpec(Task.class), duplicate.getQuerySpec(Task.class));
    }

    @Test
    public void setNestedSpecWithClass() {
        QuerySpec spec = new QuerySpec(Project.class);
        QuerySpec relatedSpec = new QuerySpec(Task.class);
        spec.setNestedSpecs(Arrays.asList(relatedSpec));
        Assertions.assertSame(relatedSpec, spec.getQuerySpec(Task.class));
    }

    @Test
    public void setNestedSpecWithResourceType() {
        QuerySpec spec = new QuerySpec("projects");
        QuerySpec relatedSpec = new QuerySpec("tasks");
        spec.setNestedSpecs(Arrays.asList(relatedSpec));
        Assertions.assertSame(spec, spec.getQuerySpec("projects"));
        Assertions.assertSame(relatedSpec, spec.getQuerySpec("tasks"));
        Assertions.assertSame(relatedSpec, spec.getOrCreateQuerySpec("tasks"));
        Assertions.assertNotSame(relatedSpec, spec.getOrCreateQuerySpec("schedules"));
    }

    @Test
    public void putRelatedSpecShouldFailIfClassMatchesRoot() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
	        QuerySpec spec = new QuerySpec(Project.class);
	        QuerySpec relatedSpec = new QuerySpec(Task.class);
	        spec.putRelatedSpec(Project.class, relatedSpec);
		});
    }

    @Test
    public void testEquals() {
        QuerySpec spec1 = new QuerySpec(Task.class);
        spec1.addFilter(new FilterSpec(Arrays.asList("filterAttr"), FilterOperator.EQ, "test"));
        spec1.addSort(new SortSpec(Arrays.asList("sortAttr"), Direction.ASC));
        spec1.includeField(Arrays.asList("includedField"));
        spec1.includeRelation(Arrays.asList("includedRelation"));
        Assertions.assertEquals(spec1, spec1);

        QuerySpec spec2 = new QuerySpec(Task.class);
        spec2.addFilter(new FilterSpec(Arrays.asList("filterAttr"), FilterOperator.EQ, "test"));
        spec2.addSort(new SortSpec(Arrays.asList("sortAttr"), Direction.ASC));
        spec2.includeField(Arrays.asList("includedField"));
        spec2.includeRelation(Arrays.asList("includedRelation"));
        Assertions.assertEquals(spec2, spec2);
        Assertions.assertEquals(spec1, spec2);

        spec2.getIncludedRelations().clear();
        Assertions.assertNotEquals(spec1, spec2);
        spec2.includeRelation(Arrays.asList("includedRelation"));
        Assertions.assertEquals(spec1, spec2);

        spec2.getIncludedFields().clear();
        Assertions.assertNotEquals(spec1, spec2);
        Assertions.assertNotEquals(spec1.hashCode(), spec2.hashCode());
        spec2.includeField(Arrays.asList("includedField"));
        Assertions.assertEquals(spec1, spec2);

        spec2.getFilters().clear();
        Assertions.assertNotEquals(spec1, spec2);
        spec2.addFilter(new FilterSpec(Arrays.asList("filterAttr"), FilterOperator.EQ, "test"));
        Assertions.assertEquals(spec1, spec2);

        spec2.getSort().clear();
        Assertions.assertNotEquals(spec1, spec2);
        spec2.addSort(new SortSpec(Arrays.asList("sortAttr"), Direction.ASC));
        Assertions.assertEquals(spec1, spec2);

        spec2.setOffset(2);
        Assertions.assertNotEquals(spec1, spec2);
        spec2.setOffset(0);
        Assertions.assertEquals(spec1, spec2);

        spec2.setLimit(2L);
        Assertions.assertNotEquals(spec1, spec2);
        spec2.setLimit(null);
        Assertions.assertEquals(spec1, spec2);

        Assertions.assertNotEquals(spec1, "someOtherType");
    }

    private static QuerySpec createTestQueySpec() {
        FilterSpec filterSpec = new FilterSpec(Arrays.asList("filterAttr"), FilterOperator.EQ, "test");
        SortSpec sortSpec = new SortSpec(Arrays.asList("sortAttr"), Direction.ASC);
        IncludeFieldSpec fieldSpec = new IncludeFieldSpec(PathSpec.of("includedField"));
        IncludeRelationSpec relationSpec = new IncludeRelationSpec(PathSpec.of("includedRelation"));
        QuerySpec spec = new QuerySpec(Task.class);
        spec.addFilter(filterSpec);
        spec.addSort(sortSpec);
        spec.getIncludedFields().add(fieldSpec);
        spec.getIncludedRelations().add(relationSpec);
        spec.setOffset(1);
        return spec;
    }

    @Test
    public void testVisitor() {
        QuerySpec spec = createTestQueySpec();

        QuerySpecVisitorBase visitor = Mockito.spy(QuerySpecVisitorBase.class);
        spec.accept(visitor);

        Mockito.verify(visitor, Mockito.times(1)).visitStart(Mockito.eq(spec));
        Mockito.verify(visitor, Mockito.times(1)).visitEnd(Mockito.eq(spec));
        Mockito.verify(visitor, Mockito.times(1)).visitField(Mockito.eq(spec.getIncludedFields().get(0)));
        Mockito.verify(visitor, Mockito.times(1)).visitFilterStart(Mockito.eq(spec.getFilters().get(0)));
        Mockito.verify(visitor, Mockito.times(1)).visitFilterEnd(Mockito.eq(spec.getFilters().get(0)));
        Mockito.verify(visitor, Mockito.times(1)).visitInclude(Mockito.eq(spec.getIncludedRelations().get(0)));
        Mockito.verify(visitor, Mockito.times(1)).visitSort(Mockito.eq(spec.getSort().get(0)));
        Mockito.verify(visitor, Mockito.times(1)).visitPaging(Mockito.eq(spec.getPaging()));
        Mockito.verify(visitor, Mockito.times(4)).visitPath(Mockito.any(PathSpec.class));
    }

    @Test
    public void testVisitorWithFilterAbort() {
        QuerySpec spec = createTestQueySpec();

        QuerySpecVisitorBase visitor = Mockito.mock(QuerySpecVisitorBase.class);
        Mockito.when(visitor.visitStart(Mockito.any(QuerySpec.class))).thenReturn(true);
        Mockito.when(visitor.visitFilterStart(Mockito.any(FilterSpec.class))).thenReturn(false);
        Mockito.when(visitor.visitSort(Mockito.any(SortSpec.class))).thenReturn(true);
        Mockito.when(visitor.visitField(Mockito.any(IncludeFieldSpec.class))).thenReturn(true);
        Mockito.when(visitor.visitInclude(Mockito.any(IncludeRelationSpec.class))).thenReturn(true);
        spec.accept(visitor);

        Mockito.verify(visitor, Mockito.times(1)).visitStart(Mockito.eq(spec));
        Mockito.verify(visitor, Mockito.times(1)).visitEnd(Mockito.eq(spec));
        Mockito.verify(visitor, Mockito.times(1)).visitField(Mockito.any(IncludeFieldSpec.class));
        Mockito.verify(visitor, Mockito.times(1)).visitFilterStart(Mockito.any(FilterSpec.class));
        Mockito.verify(visitor, Mockito.times(0)).visitFilterEnd(Mockito.any(FilterSpec.class));
        Mockito.verify(visitor, Mockito.times(1)).visitInclude(Mockito.any(IncludeRelationSpec.class));
        Mockito.verify(visitor, Mockito.times(1)).visitSort(Mockito.any(SortSpec.class));
        Mockito.verify(visitor, Mockito.times(1)).visitPaging(Mockito.any(PagingSpec.class));

        // filter path will not be visited
        Mockito.verify(visitor, Mockito.times(3)).visitPath(Mockito.any(PathSpec.class));
    }

    @Test
    public void testVisitorWithSortAbort() {
        QuerySpec spec = createTestQueySpec();

        QuerySpecVisitorBase visitor = Mockito.mock(QuerySpecVisitorBase.class);
        Mockito.when(visitor.visitStart(Mockito.any(QuerySpec.class))).thenReturn(true);
        Mockito.when(visitor.visitFilterStart(Mockito.any(FilterSpec.class))).thenReturn(true);
        Mockito.when(visitor.visitSort(Mockito.any(SortSpec.class))).thenReturn(false);
        Mockito.when(visitor.visitField(Mockito.any(IncludeFieldSpec.class))).thenReturn(true);
        Mockito.when(visitor.visitInclude(Mockito.any(IncludeRelationSpec.class))).thenReturn(true);
        spec.accept(visitor);

        // sort path will not be visited
        Mockito.verify(visitor, Mockito.times(3)).visitPath(Mockito.any(PathSpec.class));
    }

    @Test
    public void testVisitorWithMultipleAbort() {
        QuerySpec spec = createTestQueySpec();

        QuerySpecVisitorBase visitor = Mockito.mock(QuerySpecVisitorBase.class);
        Mockito.when(visitor.visitStart(Mockito.any(QuerySpec.class))).thenReturn(true);
        Mockito.when(visitor.visitFilterStart(Mockito.any(FilterSpec.class))).thenReturn(true);
        Mockito.when(visitor.visitSort(Mockito.any(SortSpec.class))).thenReturn(false);
        Mockito.when(visitor.visitField(Mockito.any(IncludeFieldSpec.class))).thenReturn(false);
        Mockito.when(visitor.visitInclude(Mockito.any(IncludeRelationSpec.class))).thenReturn(false);
        spec.accept(visitor);

        // sort path will not be visited
        Mockito.verify(visitor, Mockito.times(1)).visitPath(Mockito.any(PathSpec.class));
    }


    @Test
    public void testVisitorWithAbort() {
        QuerySpec spec = createTestQueySpec();

        QuerySpecVisitorBase visitor = Mockito.mock(QuerySpecVisitorBase.class);
        Mockito.when(visitor.visitStart(Mockito.any(QuerySpec.class))).thenReturn(false);
        spec.accept(visitor);

        Mockito.verify(visitor, Mockito.times(1)).visitStart(Mockito.eq(spec));
        Mockito.verify(visitor, Mockito.times(0)).visitEnd(Mockito.eq(spec));
        Mockito.verify(visitor, Mockito.times(0)).visitField(Mockito.any(IncludeFieldSpec.class));
        Mockito.verify(visitor, Mockito.times(0)).visitFilterStart(Mockito.any(FilterSpec.class));
        Mockito.verify(visitor, Mockito.times(0)).visitFilterEnd(Mockito.any(FilterSpec.class));
        Mockito.verify(visitor, Mockito.times(0)).visitInclude(Mockito.any(IncludeRelationSpec.class));
        Mockito.verify(visitor, Mockito.times(0)).visitSort(Mockito.any(SortSpec.class));
        Mockito.verify(visitor, Mockito.times(0)).visitPaging(Mockito.any(PagingSpec.class));
        Mockito.verify(visitor, Mockito.times(0)).visitPath(Mockito.any(PathSpec.class));
    }
}
