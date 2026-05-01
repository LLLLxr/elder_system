package org.smart_elder_system.careorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.smart_elder_system.careorchestration.po.ServiceJourneyTaskPo;
import org.smart_elder_system.careorchestration.repository.ServiceJourneyTaskRepository;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceJourneyTaskServiceTest {

    @Mock
    private ServiceJourneyTaskRepository serviceJourneyTaskRepository;

    @InjectMocks
    private ServiceJourneyTaskService serviceJourneyTaskService;

    @Test
    void shouldCreateAdmissionAssessmentTask() {
        serviceJourneyTaskService.createAdmissionAssessmentTask(1001L, 3003L);

        ArgumentCaptor<ServiceJourneyTaskPo> captor = ArgumentCaptor.forClass(ServiceJourneyTaskPo.class);
        verify(serviceJourneyTaskRepository).save(captor.capture());
        ServiceJourneyTaskPo saved = captor.getValue();
        assertEquals(1001L, saved.getApplicationId());
        assertEquals(3003L, saved.getElderId());
        assertEquals(ServiceJourneyTaskService.TASK_TYPE_ADMISSION_ASSESSMENT, saved.getTaskType());
        assertEquals(ServiceJourneyTaskPo.STATUS_PENDING, saved.getStatus());
        assertEquals("PENDING_ASSESSMENT", saved.getCurrentState());
        assertNotNull(saved.getDueAt());
    }

    @Test
    void shouldCompleteOpenTask() {
        ServiceJourneyTaskPo task = new ServiceJourneyTaskPo();
        task.setApplicationId(1001L);
        task.setTaskType(ServiceJourneyTaskService.TASK_TYPE_ADMISSION_ASSESSMENT);
        task.setStatus(ServiceJourneyTaskPo.STATUS_PENDING);

        when(serviceJourneyTaskRepository.findLatestOpenTaskForUpdate(
                1001L,
                ServiceJourneyTaskService.TASK_TYPE_ADMISSION_ASSESSMENT,
                Set.of(ServiceJourneyTaskPo.STATUS_PENDING, ServiceJourneyTaskPo.STATUS_OVERDUE))).thenReturn(Optional.of(task));

        serviceJourneyTaskService.completeOpenTask(1001L, ServiceJourneyTaskService.TASK_TYPE_ADMISSION_ASSESSMENT);

        assertEquals(ServiceJourneyTaskPo.STATUS_COMPLETED, task.getStatus());
        assertNotNull(task.getCompletedAt());
        verify(serviceJourneyTaskRepository).save(task);
    }

    @Test
    void shouldCancelOverdueTask() {
        ServiceJourneyTaskPo task = new ServiceJourneyTaskPo();
        task.setApplicationId(1001L);
        task.setTaskType(ServiceJourneyTaskService.TASK_TYPE_HEALTH_ASSESSMENT);
        task.setStatus(ServiceJourneyTaskPo.STATUS_OVERDUE);

        when(serviceJourneyTaskRepository.findLatestOpenTaskForUpdate(
                1001L,
                ServiceJourneyTaskService.TASK_TYPE_HEALTH_ASSESSMENT,
                Set.of(ServiceJourneyTaskPo.STATUS_PENDING, ServiceJourneyTaskPo.STATUS_OVERDUE))).thenReturn(Optional.of(task));

        serviceJourneyTaskService.cancelOpenTask(1001L, ServiceJourneyTaskService.TASK_TYPE_HEALTH_ASSESSMENT);

        assertEquals(ServiceJourneyTaskPo.STATUS_CANCELLED, task.getStatus());
        assertNotNull(task.getCompletedAt());
        verify(serviceJourneyTaskRepository).save(task);
    }

    @Test
    void shouldListTasks() {
        ServiceJourneyTaskPo task = new ServiceJourneyTaskPo();
        task.setId(9L);
        task.setApplicationId(1001L);
        task.setElderId(3003L);
        task.setTaskType(ServiceJourneyTaskService.TASK_TYPE_ADMISSION_ASSESSMENT);
        task.setCurrentState("PENDING_ASSESSMENT");
        task.setAssigneeRole("CUSTOMER_SERVICE");
        task.setStatus(ServiceJourneyTaskPo.STATUS_PENDING);
        task.setDueAt(LocalDateTime.now().plusHours(2));
        task.setCreatedDateTimeUtc(LocalDateTime.now());

        when(serviceJourneyTaskRepository.searchTasks(1001L, null, null, null, null,
                List.of(ServiceJourneyTaskPo.STATUS_PENDING),
                PageRequest.of(0, 20, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "dueAt").and(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id")))))
                .thenReturn(new PageImpl<>(List.of(task)));

        var result = serviceJourneyTaskService.listTasks(
                1001L,
                null,
                null,
                null,
                List.of(ServiceJourneyTaskPo.STATUS_PENDING),
                null,
                0,
                20,
                "dueAt",
                "asc");

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(9L, result.getContent().get(0).getTaskId());
        assertEquals("PENDING_ASSESSMENT", result.getContent().get(0).getCurrentState());
    }

    @Test
    void shouldBuildTaskOverview() {
        List<Object[]> taskTypeRows = new java.util.ArrayList<>();
        taskTypeRows.add(new Object[]{ServiceJourneyTaskService.TASK_TYPE_ADMISSION_ASSESSMENT, 2L});
        List<Object[]> statusRows = new java.util.ArrayList<>();
        statusRows.add(new Object[]{ServiceJourneyTaskPo.STATUS_PENDING, 1L});
        statusRows.add(new Object[]{ServiceJourneyTaskPo.STATUS_OVERDUE, 2L});
        statusRows.add(new Object[]{ServiceJourneyTaskPo.STATUS_COMPLETED, 1L});
        when(serviceJourneyTaskRepository.countByTaskType(null, null, null, null, null, null)).thenReturn(taskTypeRows);
        when(serviceJourneyTaskRepository.countByStatus(null, null, null, null, null, null)).thenReturn(statusRows);

        var overview = serviceJourneyTaskService.getTaskOverview(null, null, null, null, null, null);

        assertEquals(1, overview.getPendingCount());
        assertEquals(2, overview.getOverdueCount());
        assertEquals(1, overview.getCompletedCount());
        assertEquals(0, overview.getCancelledCount());
        assertEquals(1, overview.getTaskTypeDistribution().size());
        assertEquals(ServiceJourneyTaskService.TASK_TYPE_ADMISSION_ASSESSMENT, overview.getTaskTypeDistribution().get(0).getName());
        assertTrue(overview.getStatusDistribution().stream().anyMatch(item -> ServiceJourneyTaskPo.STATUS_OVERDUE.equals(item.getName())));
    }

    @Test
    void shouldMarkOverdueTasks() {
        ServiceJourneyTaskPo task = new ServiceJourneyTaskPo();
        task.setStatus(ServiceJourneyTaskPo.STATUS_PENDING);
        task.setDueAt(LocalDateTime.now().minusHours(1));

        when(serviceJourneyTaskRepository.findByStatusAndDueAtBefore(ServiceJourneyTaskPo.STATUS_PENDING, task.getDueAt().plusHours(1)))
                .thenReturn(List.of(task));

        int updated = serviceJourneyTaskService.markOverdueTasks(task.getDueAt().plusHours(1));

        assertEquals(1, updated);
        assertEquals(ServiceJourneyTaskPo.STATUS_OVERDUE, task.getStatus());
        verify(serviceJourneyTaskRepository).saveAll(List.of(task));
    }
}
