package org.smart_elder_system.careorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.smart_elder_system.careorchestration.dto.ServiceJourneyTransitionLogItemDTO;
import org.smart_elder_system.careorchestration.journey.ServiceJourneyEvent;
import org.smart_elder_system.careorchestration.journey.ServiceJourneyState;
import org.smart_elder_system.careorchestration.po.ServiceJourneyTransitionLogPo;
import org.smart_elder_system.careorchestration.repository.ServiceJourneyTransitionLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceJourneyTransitionLogService {

    private static final int MAX_SNAPSHOT_LENGTH = 2000;

    private final ServiceJourneyTransitionLogRepository transitionLogRepository;
    private final ObjectMapper objectMapper;

    public void logTransition(
            Long applicationId,
            Long agreementId,
            Long elderId,
            ServiceJourneyState fromState,
            ServiceJourneyEvent journeyEvent,
            ServiceJourneyState toState,
            String reason,
            Object requestSnapshot) {

        ServiceJourneyTransitionLogPo log = new ServiceJourneyTransitionLogPo();
        log.setApplicationId(applicationId);
        log.setAgreementId(agreementId);
        log.setElderId(elderId);
        log.setFromState(fromState == null ? null : fromState.name());
        log.setJourneyEvent(journeyEvent.name());
        log.setToState(toState.name());
        log.setReason(reason);
        log.setRequestSnapshot(serializeSnapshot(requestSnapshot));
        log.setTransitionTime(LocalDateTime.now());
        transitionLogRepository.save(log);
    }

    public boolean hasReturnToHealthAfter(Long applicationId, LocalDateTime assessedAt) {
        if (applicationId == null || assessedAt == null) {
            return false;
        }

        return transitionLogRepository
                .findTopByApplicationIdAndJourneyEventOrderByTransitionTimeDescIdDesc(
                        applicationId,
                        ServiceJourneyEvent.RETURN_TO_HEALTH_ASSESSMENT.name())
                .map(log -> log.getTransitionTime() != null && !log.getTransitionTime().isBefore(assessedAt))
                .orElse(false);
    }

    public boolean hasTransition(Long applicationId, ServiceJourneyEvent journeyEvent, ServiceJourneyState toState) {
        if (applicationId == null || journeyEvent == null || toState == null) {
            return false;
        }

        return transitionLogRepository
                .findTopByApplicationIdAndJourneyEventOrderByTransitionTimeDescIdDesc(applicationId, journeyEvent.name())
                .map(log -> toState.name().equals(log.getToState()))
                .orElse(false);
    }

    public List<ServiceJourneyTransitionLogItemDTO> listByApplicationId(Long applicationId) {
        return transitionLogRepository.findByApplicationIdOrderByTransitionTimeAscIdAsc(applicationId)
                .stream()
                .map(this::toItemDTO)
                .toList();
    }

    public List<ServiceJourneyTransitionLogItemDTO> listByAgreementId(Long agreementId) {
        return transitionLogRepository.findByAgreementIdOrderByTransitionTimeAscIdAsc(agreementId)
                .stream()
                .map(this::toItemDTO)
                .toList();
    }

    private ServiceJourneyTransitionLogItemDTO toItemDTO(ServiceJourneyTransitionLogPo po) {
        ServiceJourneyTransitionLogItemDTO dto = new ServiceJourneyTransitionLogItemDTO();
        dto.setLogId(po.getId());
        dto.setApplicationId(po.getApplicationId());
        dto.setAgreementId(po.getAgreementId());
        dto.setElderId(po.getElderId());
        dto.setFromState(po.getFromState());
        dto.setJourneyEvent(po.getJourneyEvent());
        dto.setToState(po.getToState());
        dto.setReason(po.getReason());
        dto.setRequestSnapshot(po.getRequestSnapshot());
        dto.setTransitionTime(po.getTransitionTime());
        dto.setCreatedBy(po.getCreatedBy());
        return dto;
    }

    private String serializeSnapshot(Object requestSnapshot) {
        if (requestSnapshot == null) {
            return null;
        }

        try {
            return truncate(objectMapper.writeValueAsString(requestSnapshot));
        } catch (JsonProcessingException ex) {
            return truncate(String.valueOf(requestSnapshot));
        }
    }

    private String truncate(String value) {
        if (value == null || value.length() <= MAX_SNAPSHOT_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_SNAPSHOT_LENGTH);
    }
}
