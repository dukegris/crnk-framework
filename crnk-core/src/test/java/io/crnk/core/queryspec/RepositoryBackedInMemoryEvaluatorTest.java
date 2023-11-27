package io.crnk.core.queryspec;

import io.crnk.core.CoreTestModule;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.mock.models.RelationIdTestResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class RepositoryBackedInMemoryEvaluatorTest extends InMemoryEvaluatorTestBase {


    private CrnkBoot boot;

    @Override
    protected InMemoryEvaluator getEvaluator() {
        boot = new CrnkBoot();
        boot.setPropertiesProvider(key -> {
            if (key.equals(CrnkProperties.ENFORCE_ID_NAME)) {
                return "true";
            }
            return null;
        });
        boot.addModule(new CoreTestModule());
        boot.boot();

        ResourceRegistry resourceRegistry = boot.getResourceRegistry();
        return new InMemoryEvaluator(resourceRegistry);
    }

    @Test
    public void testRelationId() {
        RelationIdTestResource resource = new RelationIdTestResource();
        resource.setTestRenamedDifferent(12L);

        List<RelationIdTestResource> resources = Arrays.asList(resource);

        QuerySpec matchQuerySpec = new QuerySpec(RelationIdTestResource.class);
        matchQuerySpec.addFilter(new FilterSpec(PathSpec.of("testRenamed.id"), FilterOperator.EQ, 12L));

        QuerySpec mismatchQuerySpec = new QuerySpec(RelationIdTestResource.class);
        mismatchQuerySpec.addFilter(new FilterSpec(PathSpec.of("testRenamed.id"), FilterOperator.EQ, 99999L));

        Assertions.assertEquals(1, evaluator.eval(resources, matchQuerySpec).size());
        Assertions.assertEquals(0, evaluator.eval(resources, mismatchQuerySpec).size());
    }
}
