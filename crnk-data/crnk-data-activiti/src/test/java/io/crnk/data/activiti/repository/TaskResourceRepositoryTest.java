package io.crnk.data.activiti.repository;

import com.google.common.collect.Sets;
import io.crnk.data.activiti.ActivitiModule;
import io.crnk.data.activiti.ActivitiModuleConfig;
import io.crnk.data.activiti.ProcessInstanceConfig;
import io.crnk.data.activiti.TaskRepositoryConfig;
import io.crnk.data.activiti.example.model.ApproveForm;
import io.crnk.data.activiti.example.model.ApproveTask;
import io.crnk.data.activiti.example.model.HistoricApproveTask;
import io.crnk.data.activiti.example.model.HistoricScheduleApprovalProcessInstance;
import io.crnk.data.activiti.example.model.ScheduleApprovalProcessInstance;
import io.crnk.data.activiti.internal.repository.ProcessInstanceResourceRepository;
import io.crnk.data.activiti.resource.TaskResource;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.module.Module;
import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.SortSpec;
import io.crnk.core.resource.list.ResourceList;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;

public class TaskResourceRepositoryTest extends ActivitiTestBase {


    private static final String ENFORCED_DESCRIPTION = "testDescription";

    private Task task;

    private Task isolatedTask;

    protected ProcessInstanceResourceRepository<ScheduleApprovalProcessInstance> processInstanceRepository;

    @BeforeEach
    public void setup() {
        super.setup();

        task = addTask("testTask", 12);

        isolatedTask = addTask("isolatedTask", 12);
        isolatedTask.setDescription("doesNotMatchRepositoryFilter");
        processEngine.getTaskService().saveTask(isolatedTask);

        processInstanceRepository = (ProcessInstanceResourceRepository<ScheduleApprovalProcessInstance>)
                boot.getResourceRegistry().getEntry(ScheduleApprovalProcessInstance.class)
                        .getResourceRepository().getImplementation();
    }


    @Override
    protected Module createActivitiModule() {
        ActivitiModuleConfig config = new ActivitiModuleConfig();

        ProcessInstanceConfig processConfig = config.addProcessInstance(ScheduleApprovalProcessInstance.class);
        processConfig.historic(HistoricScheduleApprovalProcessInstance.class);
        processConfig.filterByProcessDefinitionKey("scheduleChange");
        processConfig.addTaskRelationship(
                "approveTask", ApproveTask.class, "approveScheduleTask"
        );
        TaskRepositoryConfig taskConfig = config.addTask(ApproveTask.class);
        taskConfig.filterBy("description", ENFORCED_DESCRIPTION);
        taskConfig.historic(HistoricApproveTask.class);
        taskConfig.setForm(ApproveForm.class);
        return ActivitiModule.create(processEngine, config);
    }


    @Test
    public void checkResourceMapping() {
        QuerySpec querySpec = new QuerySpec(TaskResource.class);

        ApproveTask resource = taskRepository.findOne(task.getId(), querySpec);
        Assertions.assertEquals(task.getPriority(), resource.getPriority());
        Assertions.assertEquals(task.getAssignee(), resource.getAssignee());
        Assertions.assertEquals(task.getCategory(), resource.getCategory());
        Assertions.assertEquals(task.getName(), resource.getName());
        Assertions.assertEquals(task.getOwner(), resource.getOwner());
        Assertions.assertEquals(task.getDescription(), resource.getDescription());
        Assertions.assertEquals(task.getTenantId(), resource.getTenantId());
        Assertions.assertFalse(resource.isCompleted());
        Assertions.assertEquals(task.getDueDate().toInstant(), resource.getDueDate().toInstant());
    }

    @Test
    public void updateTask() {
        QuerySpec querySpec = new QuerySpec(TaskResource.class);

        OffsetDateTime updatedDueDate = OffsetDateTime.now().plusHours(12);
        ApproveTask resource = taskRepository.findOne(task.getId(), querySpec);
        resource.setName("updatedName");
        resource.setPriority(101);
        resource.setDueDate(updatedDueDate);
        ApproveTask updatedResource = taskRepository.save(resource);
        Assertions.assertEquals("updatedName", updatedResource.getName());
        Assertions.assertEquals(101, updatedResource.getPriority());
        // RCS <2023-11-07T01:06:26.061534500Z> but was:<2023-11-07T01:06:26.061Z>
        Assertions.assertEquals(updatedDueDate.toInstant().truncatedTo(ChronoUnit.SECONDS), updatedResource.getDueDate().toInstant().truncatedTo(ChronoUnit.SECONDS));

        updatedResource = taskRepository.findOne(task.getId(), querySpec);
        Assertions.assertEquals("updatedName", updatedResource.getName());
        Assertions.assertEquals(101, updatedResource.getPriority());
        // RCS <2023-11-07T01:06:26.061534500Z> but was:<2023-11-07T01:06:26.061Z>
        Assertions.assertEquals(updatedDueDate.toInstant().truncatedTo(ChronoUnit.SECONDS), updatedResource.getDueDate().toInstant().truncatedTo(ChronoUnit.SECONDS));
    }

    @Test
    public void createTask() {
        QuerySpec querySpec = new QuerySpec(ApproveTask.class);

        ApproveTask resource = new ApproveTask();
        resource.setName("testTask");
        resource.setDescription(ENFORCED_DESCRIPTION);
        ApproveTask createdTask = taskRepository.create(resource);
        Assertions.assertEquals("testTask", createdTask.getName());

        createdTask = taskRepository.findOne(task.getId(), querySpec);
        Assertions.assertNotNull(createdTask);
    }

    @Test
    public void checkFilterEnforcementOnCreate() {
		Assertions.assertThrows(BadRequestException.class, () -> {
	        ApproveTask resource = new ApproveTask();
	        resource.setName("testTask");
	        resource.setDescription("invalid"); // must be set => due to chosen setup
	        taskRepository.create(resource);
		});
    }

    @Test
    public void checkFilterEnforcementOnSave() {
		Assertions.assertThrows(BadRequestException.class, () -> {
	        ApproveTask resource = new ApproveTask();
	        resource.setId(isolatedTask.getId()); // => will not be able to hijack isolated task
	        resource.setName("testTask");
	        resource.setDescription(ENFORCED_DESCRIPTION);
	        taskRepository.save(resource);
		});
    }

    @Test
    public void checkDefaultsOnEnforcedAttributeOnCreate() {
        ApproveTask resource = new ApproveTask();
        resource.setName("newTask");
        resource.setDescription(null); // default will be set
        ApproveTask savedTask = taskRepository.create(resource);
        Assertions.assertEquals(ENFORCED_DESCRIPTION, savedTask.getDescription());
    }

    @Test
    public void completeTask() {
        QuerySpec querySpec = new QuerySpec(ApproveTask.class);

        ApproveTask resource = taskRepository.findOne(task.getId(), querySpec);
        Assertions.assertFalse(resource.isCompleted());
        resource.setCompleted(true);
        ApproveTask updatedResource = taskRepository.save(resource);
        Assertions.assertTrue(updatedResource.isCompleted());
        try {
            taskRepository.findOne(task.getId(), querySpec);
            Assertions.fail();
        } catch (ResourceNotFoundException e) {
            // ok
        }
    }

    @Test
    public void deleteTask() {
        QuerySpec querySpec = new QuerySpec(ApproveTask.class);

        ApproveTask resource = taskRepository.findOne(task.getId(), querySpec);
        taskRepository.delete(resource.getId());
        try {
            taskRepository.findOne(task.getId(), querySpec);
            Assertions.fail();
        } catch (ResourceNotFoundException e) {
            // ok
        }
    }

    @Test
    public void checkEqualsName() {
        QuerySpec querySpec = new QuerySpec(ApproveTask.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.EQ, task.getId()));
        Assertions.assertEquals(1, taskRepository.findAll(querySpec).size());

        querySpec = new QuerySpec(TaskResource.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.EQ, "doesNotExists"));
        Assertions.assertEquals(0, taskRepository.findAll(querySpec).size());
    }

    @Test
    public void checkFindAllByIds() {
        ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByKey("scheduleChange");
        String id = processInstance.getId();

        // note that this is not supported for Tasks, activiti lacks API to query multiple tasks
        QuerySpec querySpec = new QuerySpec(ScheduleApprovalProcessInstance.class);
        Assertions.assertEquals(1, processInstanceRepository.findAll(Arrays.asList(id), querySpec).size());
        Assertions.assertEquals(1, processInstanceRepository.findAll(Sets.newHashSet(id), querySpec).size());
    }

    @Test
    public void checkNotEqualsNotSupported() {
		Assertions.assertThrows(BadRequestException.class, () -> {
            QuerySpec querySpec = new QuerySpec(ApproveTask.class);
            querySpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.NEQ, task.getId()));
            taskRepository.findAll(querySpec).size();
        });
    }

    @Test
    public void checkEqualsAssignee() {
        QuerySpec querySpec = new QuerySpec(ApproveTask.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList("assignee"), FilterOperator.EQ, "john"));
        Assertions.assertEquals(1, taskRepository.findAll(querySpec).size());

        querySpec = new QuerySpec(TaskResource.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList("assignee"), FilterOperator.EQ, "doesNotExists"));
        Assertions.assertEquals(0, taskRepository.findAll(querySpec).size());
    }

    @Test
    public void checkLikeAssignee() {
        QuerySpec querySpec = new QuerySpec(ApproveTask.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList("assignee"), FilterOperator.LIKE, "%oh%"));
        Assertions.assertEquals(1, taskRepository.findAll(querySpec).size());

        querySpec = new QuerySpec(TaskResource.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList("assignee"), FilterOperator.LIKE, "%doesNotExists%"));
        Assertions.assertEquals(0, taskRepository.findAll(querySpec).size());
    }

    @Test
    public void checkEqualsAssigneeList() {
        QuerySpec querySpec = new QuerySpec(ApproveTask.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList("assignee"), FilterOperator.EQ, Arrays.asList("john", "jane")));
        Assertions.assertEquals(1, taskRepository.findAll(querySpec).size());

        querySpec = new QuerySpec(TaskResource.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList("assignee"), FilterOperator.EQ, Arrays.asList("jane", "other")));
        Assertions.assertEquals(0, taskRepository.findAll(querySpec).size());
    }

    @Test
    public void checkLEPriority() {
        QuerySpec querySpec = new QuerySpec(ApproveTask.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList("priority"), FilterOperator.LE, task.getPriority() + 1));
        Assertions.assertEquals(1, taskRepository.findAll(querySpec).size());

        querySpec = new QuerySpec(TaskResource.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList("priority"), FilterOperator.LE, task.getPriority() - 1));
        Assertions.assertEquals(0, taskRepository.findAll(querySpec).size());
    }

    @Test
    public void checkLEDueDate() {
        QuerySpec querySpec = new QuerySpec(ApproveTask.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList("dueDate"), FilterOperator.LT, OffsetDateTime.now().plusHours(1)));
        Assertions.assertEquals(1, taskRepository.findAll(querySpec).size());

        querySpec = new QuerySpec(TaskResource.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList("dueDate"), FilterOperator.LT, OffsetDateTime.now().minusHours(1)));
        Assertions.assertEquals(0, taskRepository.findAll(querySpec).size());
    }


    @Test
    public void checkGTDueDate() {
        QuerySpec querySpec = new QuerySpec(ApproveTask.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList("dueDate"), FilterOperator.GT, OffsetDateTime.now().minusHours(1)));
        Assertions.assertEquals(1, taskRepository.findAll(querySpec).size());

        querySpec = new QuerySpec(TaskResource.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList("dueDate"), FilterOperator.GT, OffsetDateTime.now().plusHours(1)));
        Assertions.assertEquals(0, taskRepository.findAll(querySpec).size());
    }

    @Test
    public void checkGTPriority() {
        QuerySpec querySpec = new QuerySpec(ApproveTask.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList("priority"), FilterOperator.GE, task.getPriority() - 1));
        Assertions.assertEquals(1, taskRepository.findAll(querySpec).size());

        querySpec = new QuerySpec(TaskResource.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList("priority"), FilterOperator.GE, task.getPriority() + 1));
        Assertions.assertEquals(0, taskRepository.findAll(querySpec).size());
    }

    @Test
    public void checkOrderByPriorityAsc() {
        addTask("otherTask", 14);

        QuerySpec querySpec = new QuerySpec(ApproveTask.class);
        querySpec.addSort(new SortSpec(Arrays.asList("priority"), Direction.ASC));
        ResourceList<ApproveTask> resources = taskRepository.findAll(querySpec);
        Assertions.assertEquals(2, resources.size());
        Assertions.assertEquals("testTask", resources.get(0).getName());
        Assertions.assertEquals("otherTask", resources.get(1).getName());
    }

    @Test
    public void checkOrderByPriorityDesc() {
        addTask("otherTask", 14);

        QuerySpec querySpec = new QuerySpec(ApproveTask.class);
        querySpec.addSort(new SortSpec(Arrays.asList("priority"), Direction.DESC));
        ResourceList<ApproveTask> resources = taskRepository.findAll(querySpec);
        Assertions.assertEquals(2, resources.size());
        Assertions.assertEquals("otherTask", resources.get(0).getName());
        Assertions.assertEquals("testTask", resources.get(1).getName());

    }


    @Test
    public void checkPaging() {
        addTask("otherTask1", 14);
        addTask("otherTask2", 15);
        addTask("otherTask3", 16);

        QuerySpec querySpec = new QuerySpec(ApproveTask.class);
        querySpec.addSort(new SortSpec(Arrays.asList("priority"), Direction.ASC));
        querySpec.setOffset(1);
        querySpec.setLimit(2L);
        ResourceList<ApproveTask> resources = taskRepository.findAll(querySpec);
        Assertions.assertEquals(2, resources.size());
        Assertions.assertEquals("otherTask1", resources.get(0).getName());
        Assertions.assertEquals("otherTask2", resources.get(1).getName());
    }

    @Test
    public void checkIsolation() {
        addTask("otherTask1", 14);
        addTask("otherTask2", 15);
        addTask("otherTask3", 16);

        QuerySpec querySpec = new QuerySpec(ApproveTask.class);
        querySpec.addSort(new SortSpec(Arrays.asList("priority"), Direction.ASC));
        querySpec.setOffset(1);
        querySpec.setLimit(2L);
        ResourceList<ApproveTask> resources = taskRepository.findAll(querySpec);
        Assertions.assertEquals(2, resources.size());
        Assertions.assertEquals("otherTask1", resources.get(0).getName());
        Assertions.assertEquals("otherTask2", resources.get(1).getName());
    }
}
