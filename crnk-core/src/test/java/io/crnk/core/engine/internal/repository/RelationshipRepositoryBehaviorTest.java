package io.crnk.core.engine.internal.repository;

import io.crnk.core.CoreTestContainer;
import io.crnk.core.CoreTestModule;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.mock.models.RelationshipBehaviorTestResource;
import io.crnk.core.repository.foward.ForwardingRelationshipRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RelationshipRepositoryBehaviorTest {

    private ResourceRegistry resourceRegistry;

    @BeforeEach
    public void setup() {
        CoreTestContainer container = new CoreTestContainer();
        container.addModule(new CoreTestModule());
        container.boot();
        resourceRegistry = container.getResourceRegistry();
    }

    @Test
    public void checkRelationIdTriggersImplicitOwnerRepo() {
        RegistryEntry entry = resourceRegistry.getEntry(RelationshipBehaviorTestResource.class);
        Object relRepository = entry.getRelationshipRepository("testRelationId")
                .getImplementation();
        Assertions.assertEquals(ForwardingRelationshipRepository.class, relRepository.getClass());
    }

    @Test
    public void checkNoLookupTriggersImplicitOwnerRepo() {
        RegistryEntry entry = resourceRegistry.getEntry(RelationshipBehaviorTestResource.class);
        Object relRepository = entry.getRelationshipRepository("testNoLookup")
                .getImplementation();
        Assertions.assertEquals(ForwardingRelationshipRepository.class, relRepository.getClass());
    }

    @Test
    public void checkImplicitOwnerRepo() {
        RegistryEntry entry = resourceRegistry.getEntry(RelationshipBehaviorTestResource.class);
        Object relRepository = entry.getRelationshipRepository("testImplicityFromOwner")
                .getImplementation();
        Assertions.assertEquals(ForwardingRelationshipRepository.class, relRepository.getClass());
    }
}
