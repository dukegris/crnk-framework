package io.crnk.data.activiti.repository;

import io.crnk.data.activiti.ActivitiModule;
import io.crnk.data.activiti.example.model.ApproveForm;
import io.crnk.data.activiti.example.model.ApproveTask;
import io.crnk.data.activiti.example.model.ScheduleApprovalProcessInstance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ActivitiModuleTest extends ActivitiTestBase {

    @Test
    public void checkGetRepositories() {
        ActivitiModule module = boot.getModuleRegistry().getModule(ActivitiModule.class).get();

        Assertions.assertNotNull(module.getTaskRepository(ApproveTask.class));
        Assertions.assertNotNull(module.getFormRepository(ApproveForm.class));
        Assertions.assertNotNull(module.getProcessInstanceRepository(ScheduleApprovalProcessInstance.class));
    }

}
