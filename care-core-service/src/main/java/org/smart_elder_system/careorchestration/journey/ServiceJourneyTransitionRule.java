package org.smart_elder_system.careorchestration.journey;

public record ServiceJourneyTransitionRule(
        ServiceJourneyState fromState,
        ServiceJourneyEvent event,
        ServiceJourneyState toState,
        String requiredAuthority,
        boolean reasonRequired
) {

    public ServiceJourneyTransitionRule(
            ServiceJourneyState fromState,
            ServiceJourneyEvent event,
            ServiceJourneyState toState,
            String requiredAuthority) {
        this(fromState, event, toState, requiredAuthority, false);
    }
}
