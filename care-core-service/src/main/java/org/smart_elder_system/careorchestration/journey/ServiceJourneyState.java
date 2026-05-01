package org.smart_elder_system.careorchestration.journey;

public enum ServiceJourneyState {
    PENDING_ASSESSMENT,
    PENDING_HEALTH_ASSESSMENT,
    PENDING_AGREEMENT,
    IN_SERVICE,
    IMPROVEMENT_REQUIRED,
    RENEWED,
    TERMINATED;

    public boolean isOngoing() {
        return this == PENDING_ASSESSMENT
                || this == PENDING_HEALTH_ASSESSMENT
                || this == PENDING_AGREEMENT
                || this == IN_SERVICE;
    }
}
