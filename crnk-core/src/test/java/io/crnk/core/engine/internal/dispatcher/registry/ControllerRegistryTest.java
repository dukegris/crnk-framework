package io.crnk.core.engine.internal.dispatcher.registry;

import io.crnk.core.CoreTestContainer;
import io.crnk.core.CoreTestModule;
import io.crnk.core.engine.internal.dispatcher.ControllerRegistry;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.PathBuilder;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.BadRequestException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
// import org.junit.Rule;
import org.junit.jupiter.api.Test;
// import org.junit.rules.ExpectedException;

public class ControllerRegistryTest {

    // RCS innecesario
    // @Rule
    //  ExpectedException expectedException = ExpectedException.none();
    private ResourceRegistry resourceRegistry;
    private PathBuilder pathBuilder;
    private QueryContext queryContext = new QueryContext().setRequestVersion(0);

    @BeforeEach
    public void prepare() {
        CoreTestContainer container = new CoreTestContainer();
        container.addModule(new CoreTestModule());
        container.boot();
        resourceRegistry = container.getResourceRegistry();
        pathBuilder = new PathBuilder(resourceRegistry, container.getModuleRegistry().getTypeParser());
    }

    @Test
    public void onUnsupportedRequestRegisterShouldThrowError() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.build("/tasks/", queryContext);
        String requestType = "PATCH";
        ControllerRegistry sut = new ControllerRegistry(null);

        // THEN
        // RCS deprecated
        // expectedException.expect(BadRequestException.class);

        // WHEN
        Assertions.assertThrows(BadRequestException.class, () -> {
            sut.getController(jsonPath, requestType);
        });
    }
}
