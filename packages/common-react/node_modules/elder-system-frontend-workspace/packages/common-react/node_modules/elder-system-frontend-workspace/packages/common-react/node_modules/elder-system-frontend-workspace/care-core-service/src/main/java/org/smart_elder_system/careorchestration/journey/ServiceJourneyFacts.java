package org.smart_elder_system.careorchestration.journey;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ServiceJourneyFacts {
    String applicationStatus;
    String healthAssessmentStatus;
    String agreementStatus;
    String reviewConclusion;

    public String getApplicationStatus() {
        return applicationStatus;
    }

    public String getHealthAssessmentStatus() {
        return healthAssessmentStatus;
    }

    public String getAgreementStatus() {
        return agreementStatus;
    }

    public String getReviewConclusion() {
        return reviewConclusion;
    }
}
