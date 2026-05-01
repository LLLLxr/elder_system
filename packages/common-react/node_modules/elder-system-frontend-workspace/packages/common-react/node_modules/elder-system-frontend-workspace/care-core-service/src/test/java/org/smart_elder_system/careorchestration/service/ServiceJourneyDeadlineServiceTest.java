package org.smart_elder_system.careorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceJourneyDeadlineServiceTest {

    @Mock
    private ServiceJourneyTaskService serviceJourneyTaskService;

    @InjectMocks
    private ServiceJourneyDeadlineService serviceJourneyDeadlineService;

    @Test
    void shouldInvokeOverdueMarking() {
        when(serviceJourneyTaskService.markOverdueTasks(org.mockito.ArgumentMatchers.any())).thenReturn(1);

        serviceJourneyDeadlineService.markOverdueTasks();

        verify(serviceJourneyTaskService).markOverdueTasks(org.mockito.ArgumentMatchers.any());
    }
}
