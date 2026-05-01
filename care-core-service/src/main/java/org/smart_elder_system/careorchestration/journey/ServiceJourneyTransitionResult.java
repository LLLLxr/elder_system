package org.smart_elder_system.careorchestration.journey;

public record ServiceJourneyTransitionResult(
        ServiceJourneyState fromState,
        ServiceJourneyEvent event,
        ServiceJourneyState toState,
        String requiredAuthority
) {
}
