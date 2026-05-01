package org.smart_elder_system.common.dto.care;

import lombok.Data;

@Data
public class ServiceJourneyResultDTO {

    private Long applicationId;

    private Long elderId;

    private Long agreementId;

    private Long carePlanId;

    private Long healthProfileId;

    private Long healthAssessmentId;

    private String finalStatus;

    private String message;
}
