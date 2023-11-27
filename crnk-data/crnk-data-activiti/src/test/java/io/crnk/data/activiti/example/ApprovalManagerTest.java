package io.crnk.data.activiti.example;

import java.util.HashMap;
import java.util.Map;

import io.crnk.data.activiti.example.approval.ApprovalManager;
import io.crnk.data.activiti.example.approval.ApprovalMapper;
import io.crnk.data.activiti.mapper.ActivitiResourceMapper;
import io.crnk.data.activiti.mapper.DefaultDateTimeMapper;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.test.mock.models.Schedule;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.Execution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class ApprovalManagerTest {

	private Long mockId = 13L;

	private ApprovalManager manager;

	private RuntimeService runtimeService;

	private ResourceRepository repositoryFacade;

	private RegistryEntry registryEntry;

	private Schedule originalResource;

	@BeforeEach
	public void setup() {
		runtimeService = Mockito.mock(RuntimeService.class);
		TaskService taskService = Mockito.mock(TaskService.class);
		repositoryFacade = Mockito.mock(ResourceRepository.class);
		ApprovalMapper approvalMapper = new ApprovalMapper();
		ActivitiResourceMapper resourceMapper = new ActivitiResourceMapper(new TypeParser(), new DefaultDateTimeMapper());

		ResourceInformation information = Mockito.mock(ResourceInformation.class);
		registryEntry = Mockito.mock(RegistryEntry.class);
		ResourceRegistry resourceRegistry = Mockito.mock(ResourceRegistry.class);
		Mockito.when(registryEntry.getResourceInformation()).thenReturn(information);
		Mockito.when(registryEntry.getResourceRepositoryFacade()).thenReturn(repositoryFacade);
		Mockito.when(information.getResourceType()).thenReturn("schedule");
		Mockito.when(information.getId(Mockito.any())).thenReturn(mockId);
		Mockito.when(resourceRegistry.getEntry(Mockito.any(Class.class))).thenReturn(registryEntry);
		Mockito.when(resourceRegistry.getEntry(Mockito.any(String.class))).thenReturn(registryEntry);
		// RCS Devolver la registryEntry a través del módulo tambien para null
		// Since Mockito 2.1.0, only allow non-null instance of 
		Mockito.when(resourceRegistry.getEntry(Mockito.nullable(String.class))).thenReturn(registryEntry);
		ModuleRegistry moduleRegistry = Mockito.mock(ModuleRegistry.class);
		Mockito.when(moduleRegistry.getResourceRegistry()).thenReturn(resourceRegistry);


		originalResource = new Schedule();
		originalResource.setId(mockId);
		originalResource.setName("Jane");
		Mockito.when(repositoryFacade.findOne(Mockito.any(Long.class), Mockito.any(QuerySpec.class)))
				.thenReturn(originalResource);

		manager = new ApprovalManager();
		manager.init(runtimeService, taskService, resourceMapper, approvalMapper, moduleRegistry);
	}

	@Test
	public void checkRequestApproval() {
		Schedule changedEntity = new Schedule();
		changedEntity.setId(mockId);
		changedEntity.setName("John");

		Assertions.assertFalse(manager.needsApproval(changedEntity, HttpMethod.POST));
		Assertions.assertTrue(manager.needsApproval(changedEntity, HttpMethod.PATCH));
		manager.requestApproval(changedEntity, HttpMethod.PATCH);
		ArgumentCaptor<Map> processVariablesCaptor = ArgumentCaptor.forClass(Map.class);
		Mockito.verify(runtimeService, Mockito.times(1))
				.startProcessInstanceByKey(Mockito.eq("scheduleChange"), processVariablesCaptor.capture());
		Map processVariables = processVariablesCaptor.getValue();

		Assertions.assertEquals(7, processVariables.size());
		Assertions.assertEquals(mockId.toString(), processVariables.get("resourceId"));
		Assertions.assertEquals("schedule", processVariables.get("resourceType"));
		Assertions.assertEquals("John", processVariables.get("newValues.name"));
		Assertions.assertEquals("Jane", processVariables.get("previousValues.name"));
		Assertions.assertEquals("SHIPPED", processVariables.get("status"));
	}

	@Test
	public void checkApprovedForwardsToRepository() {
		Map processVariable = new HashMap();
		processVariable.put("resourceId", mockId.toString());
		processVariable.put("resourceType", "schedule");
		processVariable.put("newValues.name", "John");
		processVariable.put("previousValues.name", "Jane");
		Mockito.when(runtimeService.getVariables(Mockito.anyString())).thenReturn(processVariable);

		Execution execution = Mockito.mock(Execution.class);
		manager.approved(execution);

		ArgumentCaptor<Object> savedEntityCaptor = ArgumentCaptor.forClass(Object.class);
		Mockito.verify(repositoryFacade, Mockito.times(1)).save(savedEntityCaptor.capture());

		// check value updated on original resource
		Schedule savedEntity = (Schedule) savedEntityCaptor.getValue();
		Assertions.assertSame(originalResource, savedEntity);
		Assertions.assertEquals("John", savedEntity.getName());
	}
}
