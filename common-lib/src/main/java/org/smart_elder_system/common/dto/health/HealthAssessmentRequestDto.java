package org.smart_elder_system.common.dto.health;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HealthAssessmentRequestDto {

    private Long applicationId;

    private Long elderId;

    private Long agreementId;

    private String applicantName;

    private String serviceScene;

    private String assessmentStatus;

    private String assessmentConclusion;

    private Integer score;

    private LocalDateTime submittedAt;

    private LocalDateTime needsAssessedAt;

    private LocalDateTime healthAssessedAt;
}
