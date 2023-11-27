package io.crnk.validation.internal;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.document.ErrorDataBuilder;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.SimpleModule;
import io.crnk.validation.ValidationModule;
import io.crnk.validation.mock.models.Task;
import io.crnk.validation.mock.repository.ProjectRepository;
import io.crnk.validation.mock.repository.ScheduleRepository;
import io.crnk.validation.mock.repository.TaskRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.validation.ElementKind;
import javax.validation.Path;
import java.util.Iterator;

public class ConstraintViolationImplTest {


    private ErrorData errorData;

    private CrnkBoot boot;

    private ConstraintViolationImpl violation;
    private QueryContext queryContext = new QueryContext().setRequestVersion(0);

    @BeforeEach
    public void setup() {
        SimpleModule testModule = new SimpleModule("test");
        testModule.addRepository(new ProjectRepository());
        testModule.addRepository(new ScheduleRepository());
        testModule.addRepository(new TaskRepository());

        boot = new CrnkBoot();
        boot.addModule(ValidationModule.create());
        boot.addModule(testModule);
        boot.boot();

        errorData =
                Mockito.spy(new ErrorDataBuilder().setDetail("testMessage").addMetaField(ConstraintViolationExceptionMapper
                                .META_RESOURCE_TYPE,
                        "tasks")
                        .setSourcePointer("name").build());

        ResourceRegistry resourceRegistry = boot.getResourceRegistry();

        violation = ConstraintViolationImpl.fromError(resourceRegistry, errorData, queryContext);
    }

    @Test
    public void testDetailMappedToMessage() {
        Assertions.assertEquals(errorData.getDetail(), violation.getMessage());
        Assertions.assertNotNull(errorData.getDetail());
    }

    @Test
    public void path() {
        Path path = violation.getPropertyPath();
        Assertions.assertEquals("name", path.toString());
        Assertions.assertEquals(path.toString().hashCode(), path.hashCode());

        Assertions.assertNotEquals(path, null);
        Assertions.assertNotEquals(path, "not a path");
        Assertions.assertEquals(path, path);

        Iterator<Path.Node> iterator = path.iterator();
        Assertions.assertTrue(iterator.hasNext());
        Path.Node node = iterator.next();
        Assertions.assertEquals("name", node.getName());
        Assertions.assertEquals("name", node.toString());
        Assertions.assertEquals(null, node.getKey());
        Assertions.assertEquals(ElementKind.PROPERTY, node.getKind());
        try {
            node.isInIterable();
            Assertions.fail();
        } catch (UnsupportedOperationException e) {
            //
        }
        try {
            node.as((Class) String.class);
            Assertions.fail();
        } catch (UnsupportedOperationException e) {
            //
        }
        Assertions.assertFalse(iterator.hasNext());
    }

    @Test
    public void getRootBeanClass() {
        Assertions.assertEquals(Task.class, violation.getRootBeanClass());
    }


    @Test
    public void getRootBean() {
    	Assertions.assertThrows(UnsupportedOperationException.class, () -> {
        violation.getRootBean();
    	});
    }

    @Test
    public void getLeafBean() {
    	Assertions.assertThrows(UnsupportedOperationException.class, () -> {
        violation.getLeafBean();
    	});
    }

    @Test
    public void getInvalidValue() {
    	Assertions.assertThrows(UnsupportedOperationException.class, () -> {
        violation.getInvalidValue();
    	});
    }

    @Test
    public void getExecutableParameters() {
    	Assertions.assertThrows(UnsupportedOperationException.class, () -> {
        violation.getExecutableParameters();
    	});
    }

    @Test
    public void getExecutableReturnValue() {
    	Assertions.assertThrows(UnsupportedOperationException.class, () -> {
        violation.getExecutableReturnValue();
    	});
    }

    @Test
    public void unwrap() {
        Assertions.assertNull(violation.unwrap(String.class));
    }

    @Test
    public void getMessage() {
        violation.getMessage();
        Mockito.verify(errorData, Mockito.times(1)).getDetail();
    }

    @Test
    public void getConstraintDescriptor() {
    	Assertions.assertThrows(UnsupportedOperationException.class, () -> {
        violation.getConstraintDescriptor();
    	});
    }

}
