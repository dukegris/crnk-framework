package io.crnk.data.activiti.repository;

import io.crnk.data.activiti.example.model.ImmediateTerminatationProcessInstance;
import io.crnk.data.activiti.internal.repository.ProcessInstanceResourceRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProcessInstanceImmediateTerminatationTest extends ActivitiTestBase {

    private ProcessInstanceResourceRepository<ImmediateTerminatationProcessInstance> processRepository;

    @BeforeEach
    public void setup() {
        super.setup();

        processRepository =
                (ProcessInstanceResourceRepository<ImmediateTerminatationProcessInstance>) boot.getResourceRegistry().getEntry
                        (ImmediateTerminatationProcessInstance.class).getResourceRepository().getImplementation();

    }

    @Test
    public void test() {
        ImmediateTerminatationProcessInstance processInstance = new ImmediateTerminatationProcessInstance();
        processInstance.setValue("test");

        ImmediateTerminatationProcessInstance createdProcessInstance = processRepository.create(processInstance);
        Assertions.assertNotNull(createdProcessInstance);
        Assertions.assertTrue(createdProcessInstance.isEnded());
        Assertions.assertFalse(createdProcessInstance.isSuspended());
        Assertions.assertNull(createdProcessInstance.getDescription());
        Assertions.assertEquals("quickStartEvent", createdProcessInstance.getActivityId());
        Assertions.assertEquals("test", createdProcessInstance.getValue());

        Assertions.assertNotNull(createdProcessInstance.getId());
    }
}
