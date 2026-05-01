package org.smart_elder_system.careorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.smart_elder_system.careorchestration.journey.ServiceJourneyEvent;
import org.smart_elder_system.careorchestration.journey.ServiceJourneyState;
import org.smart_elder_system.careorchestration.po.ServiceJourneyTransitionLogPo;
import org.smart_elder_system.careorchestration.repository.ServiceJourneyTransitionLogRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceJourneyTransitionLogServiceTest {

    @Mock
    private ServiceJourneyTransitionLogRepository transitionLogRepository;

    @InjectMocks
    private ServiceJourneyTransitionLogService transitionLogService =
            new ServiceJourneyTransitionLogService(null, new ObjectMapper());

    @Test
    void shouldPersistTransitionLogWithSnapshot() {
        transitionLogService = new ServiceJourneyTransitionLogService(transitionLogRepository, new ObjectMapper());

        transitionLogService.logTransition(
                1001L,
                2002L,
                3003L,
                ServiceJourneyState.PENDING_ASSESSMENT,
                ServiceJourneyEvent.ADMISSION_APPROVED,
                ServiceJourneyState.PENDING_HEALTH_ASSESSMENT,
                "需求评估通过",
                Map.of("applicationId", 1001L, "operator", "tester")
        );

        ArgumentCaptor<ServiceJourneyTransitionLogPo> captor = ArgumentCaptor.forClass(ServiceJourneyTransitionLogPo.class);
        verify(transitionLogRepository).save(captor.capture());

        ServiceJourneyTransitionLogPo saved = captor.getValue();
        assertEquals(1001L, saved.getApplicationId());
        assertEquals(2002L, saved.getAgreementId());
        assertEquals(3003L, saved.getElderId());
        assertEquals(ServiceJourneyState.PENDING_ASSESSMENT.name(), saved.getFromState());
        assertEquals(ServiceJourneyEvent.ADMISSION_APPROVED.name(), saved.getJourneyEvent());
        assertEquals(ServiceJourneyState.PENDING_HEALTH_ASSESSMENT.name(), saved.getToState());
        assertEquals("需求评估通过", saved.getReason());
        assertNotNull(saved.getRequestSnapshot());
        assertNotNull(saved.getTransitionTime());
    }

    @Test
    void shouldAllowNullFromStateAndSnapshot() {
        transitionLogService = new ServiceJourneyTransitionLogService(transitionLogRepository, new ObjectMapper());

        transitionLogService.logTransition(
                1001L,
                null,
                3003L,
                null,
                ServiceJourneyEvent.APPLICATION_SUBMITTED,
                ServiceJourneyState.PENDING_ASSESSMENT,
                "申请提交",
                null
        );

        ArgumentCaptor<ServiceJourneyTransitionLogPo> captor = ArgumentCaptor.forClass(ServiceJourneyTransitionLogPo.class);
        verify(transitionLogRepository).save(captor.capture());

        ServiceJourneyTransitionLogPo saved = captor.getValue();
        assertNull(saved.getFromState());
        assertNull(saved.getRequestSnapshot());
        assertEquals(ServiceJourneyEvent.APPLICATION_SUBMITTED.name(), saved.getJourneyEvent());
        assertEquals(ServiceJourneyState.PENDING_ASSESSMENT.name(), saved.getToState());
    }

    @Test
    void shouldListTransitionLogsByApplicationId() {
        transitionLogService = new ServiceJourneyTransitionLogService(transitionLogRepository, new ObjectMapper());

        ServiceJourneyTransitionLogPo log = new ServiceJourneyTransitionLogPo();
        log.setId(1L);
        log.setApplicationId(1001L);
        log.setAgreementId(2002L);
        log.setElderId(3003L);
        log.setFromState(ServiceJourneyState.PENDING_HEALTH_ASSESSMENT.name());
        log.setJourneyEvent(ServiceJourneyEvent.RETURN_TO_ASSESSMENT.name());
        log.setToState(ServiceJourneyState.PENDING_ASSESSMENT.name());
        log.setReason("资料补充");
        log.setRequestSnapshot("{\"reason\":\"资料补充\"}");
        log.setTransitionTime(LocalDateTime.now());
        log.setCreatedBy("tester");

        when(transitionLogRepository.findByApplicationIdOrderByTransitionTimeAscIdAsc(1001L)).thenReturn(List.of(log));

        var result = transitionLogService.listByApplicationId(1001L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getLogId());
        assertEquals("tester", result.get(0).getCreatedBy());
        assertEquals(ServiceJourneyEvent.RETURN_TO_ASSESSMENT.name(), result.get(0).getJourneyEvent());
    }

    @Test
    void shouldDetectReturnToHealthAfterAssessmentTime() {
        transitionLogService = new ServiceJourneyTransitionLogService(transitionLogRepository, new ObjectMapper());

        ServiceJourneyTransitionLogPo log = new ServiceJourneyTransitionLogPo();
        log.setTransitionTime(LocalDateTime.now());

        when(transitionLogRepository.findTopByApplicationIdAndJourneyEventOrderByTransitionTimeDescIdDesc(
                1001L,
                ServiceJourneyEvent.RETURN_TO_HEALTH_ASSESSMENT.name())).thenReturn(java.util.Optional.of(log));

        assertEquals(true, transitionLogService.hasReturnToHealthAfter(1001L, LocalDateTime.now().minusMinutes(1)));
        assertFalse(transitionLogService.hasReturnToHealthAfter(1001L, LocalDateTime.now().plusMinutes(1)));
    }
}
