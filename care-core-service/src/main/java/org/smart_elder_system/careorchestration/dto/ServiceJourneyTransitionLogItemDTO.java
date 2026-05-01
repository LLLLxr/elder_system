package org.smart_elder_system.careorchestration.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ServiceJourneyTransitionLogItemDTO {

    private Long logId;
    private Long applicationId;
    private Long agreementId;
    private Long elderId;
    private String fromState;
    private String journeyEvent;
    private String toState;
    private String reason;
    private String requestSnapshot;
    private LocalDateTime transitionTime;
    private String createdBy;
}
