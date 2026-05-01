package org.smart_elder_system.careorchestration.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ServiceJourneyTaskItemDTO {

    private Long taskId;
    private Long applicationId;
    private Long agreementId;
    private Long elderId;
    private String taskType;
    private String currentState;
    private String assigneeRole;
    private String status;
    private LocalDateTime dueAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}
