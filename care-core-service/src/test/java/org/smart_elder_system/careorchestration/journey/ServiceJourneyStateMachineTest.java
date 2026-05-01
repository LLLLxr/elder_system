package org.smart_elder_system.careorchestration.journey;

import org.junit.jupiter.api.Test;
import org.smart_elder_system.admission.model.ServiceApplication;
import org.smart_elder_system.contract.model.ServiceAgreement;
import org.smart_elder_system.quality.model.ServiceReview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceJourneyStateMachineTest {

    private final ServiceJourneyStateMachine stateMachine = new ServiceJourneyStateMachine();

    @Test
    void shouldInitializeJourneyAsPendingAssessment() {
        assertEquals(
                ServiceJourneyState.PENDING_ASSESSMENT,
                stateMachine.initialState(ServiceJourneyEvent.APPLICATION_SUBMITTED)
        );
    }

    @Test
    void shouldRejectInvalidInitialEvent() {
        assertThrows(
                IllegalArgumentException.class,
                () -> stateMachine.initialState(ServiceJourneyEvent.HEALTH_APPROVED)
        );
    }

    @Test
    void shouldDerivePendingAssessmentFromSubmittedApplication() {
        ServiceJourneyState state = stateMachine.deriveCurrentState(ServiceJourneyFacts.builder()
                .applicationStatus(ServiceApplication.STATUS_SUBMITTED)
                .build());

        assertEquals(ServiceJourneyState.PENDING_ASSESSMENT, state);
    }

    @Test
    void shouldDerivePendingAssessmentFromAssessedApplication() {
        ServiceJourneyState state = stateMachine.deriveCurrentState(ServiceJourneyFacts.builder()
                .applicationStatus(ServiceApplication.STATUS_ASSESSED)
                .build());

        assertEquals(ServiceJourneyState.PENDING_ASSESSMENT, state);
    }

    @Test
    void shouldDeriveTerminatedFromFailedAdmission() {
        ServiceJourneyState state = stateMachine.deriveCurrentState(ServiceJourneyFacts.builder()
                .applicationStatus(ServiceApplication.STATUS_FAILED)
                .build());

        assertEquals(ServiceJourneyState.TERMINATED, state);
    }

    @Test
    void shouldDeriveTerminatedFromWithdrawnAdmission() {
        ServiceJourneyState state = stateMachine.deriveCurrentState(ServiceJourneyFacts.builder()
                .applicationStatus(ServiceApplication.STATUS_WITHDRAWN)
                .build());

        assertEquals(ServiceJourneyState.TERMINATED, state);
    }

    @Test
    void shouldDerivePendingHealthAssessmentWhenAdmissionPassedButHealthNotSubmitted() {
        ServiceJourneyState state = stateMachine.deriveCurrentState(ServiceJourneyFacts.builder()
                .applicationStatus(ServiceApplication.STATUS_PASSED)
                .healthAssessmentStatus(ServiceJourneyStateMachine.HEALTH_ASSESSMENT_PENDING)
                .build());

        assertEquals(ServiceJourneyState.PENDING_HEALTH_ASSESSMENT, state);
    }

    @Test
    void shouldDerivePendingAgreementWhenHealthAssessmentPassed() {
        ServiceJourneyState state = stateMachine.deriveCurrentState(ServiceJourneyFacts.builder()
                .applicationStatus(ServiceApplication.STATUS_PASSED)
                .healthAssessmentStatus(ServiceJourneyStateMachine.HEALTH_ASSESSMENT_PASSED)
                .build());

        assertEquals(ServiceJourneyState.PENDING_AGREEMENT, state);
    }

    @Test
    void shouldDeriveTerminatedWhenHealthAssessmentFailed() {
        ServiceJourneyState state = stateMachine.deriveCurrentState(ServiceJourneyFacts.builder()
                .applicationStatus(ServiceApplication.STATUS_PASSED)
                .healthAssessmentStatus(ServiceJourneyStateMachine.HEALTH_ASSESSMENT_FAILED)
                .build());

        assertEquals(ServiceJourneyState.TERMINATED, state);
    }

    @Test
    void shouldDeriveInServiceFromActiveAgreement() {
        ServiceJourneyState state = stateMachine.deriveCurrentState(ServiceJourneyFacts.builder()
                .applicationStatus(ServiceApplication.STATUS_PASSED)
                .agreementStatus(ServiceAgreement.STATUS_ACTIVE)
                .build());

        assertEquals(ServiceJourneyState.IN_SERVICE, state);
    }

    @Test
    void shouldDeriveRenewedFromRenewedAgreement() {
        ServiceJourneyState state = stateMachine.deriveCurrentState(ServiceJourneyFacts.builder()
                .applicationStatus(ServiceApplication.STATUS_PASSED)
                .agreementStatus(ServiceAgreement.STATUS_RENEWED)
                .build());

        assertEquals(ServiceJourneyState.RENEWED, state);
    }

    @Test
    void shouldDeriveImprovementRequiredFromReviewConclusion() {
        ServiceJourneyState state = stateMachine.deriveCurrentState(ServiceJourneyFacts.builder()
                .applicationStatus(ServiceApplication.STATUS_PASSED)
                .agreementStatus(ServiceAgreement.STATUS_ACTIVE)
                .reviewConclusion(ServiceReview.REVIEW_CONCLUSION_IMPROVE)
                .build());

        assertEquals(ServiceJourneyState.IMPROVEMENT_REQUIRED, state);
    }

    @Test
    void shouldDeriveTerminatedFromReviewTerminate() {
        ServiceJourneyState state = stateMachine.deriveCurrentState(ServiceJourneyFacts.builder()
                .applicationStatus(ServiceApplication.STATUS_PASSED)
                .agreementStatus(ServiceAgreement.STATUS_ACTIVE)
                .reviewConclusion(ServiceReview.REVIEW_CONCLUSION_TERMINATE)
                .build());

        assertEquals(ServiceJourneyState.TERMINATED, state);
    }

    @Test
    void shouldTransitBetweenDefinedStates() {
        assertEquals(
                ServiceJourneyState.PENDING_HEALTH_ASSESSMENT,
                stateMachine.transit(ServiceJourneyState.PENDING_ASSESSMENT, ServiceJourneyEvent.ADMISSION_APPROVED)
        );
        assertEquals(
                ServiceJourneyState.PENDING_ASSESSMENT,
                stateMachine.transit(ServiceJourneyState.PENDING_HEALTH_ASSESSMENT, ServiceJourneyEvent.RETURN_TO_ASSESSMENT)
        );
        assertEquals(
                ServiceJourneyState.PENDING_AGREEMENT,
                stateMachine.transit(ServiceJourneyState.PENDING_HEALTH_ASSESSMENT, ServiceJourneyEvent.HEALTH_APPROVED)
        );
        assertEquals(
                ServiceJourneyState.PENDING_HEALTH_ASSESSMENT,
                stateMachine.transit(ServiceJourneyState.PENDING_AGREEMENT, ServiceJourneyEvent.RETURN_TO_HEALTH_ASSESSMENT)
        );
        assertEquals(
                ServiceJourneyState.IN_SERVICE,
                stateMachine.transit(ServiceJourneyState.PENDING_AGREEMENT, ServiceJourneyEvent.AGREEMENT_SIGNED)
        );
    }

    @Test
    void shouldRejectInvalidTransition() {
        assertFalse(stateMachine.canTransit(ServiceJourneyState.PENDING_ASSESSMENT, ServiceJourneyEvent.REVIEW_RENEW));
        assertThrows(
                IllegalArgumentException.class,
                () -> stateMachine.transit(ServiceJourneyState.PENDING_ASSESSMENT, ServiceJourneyEvent.REVIEW_RENEW)
        );
    }

    @Test
    void shouldMarkOngoingStatesCorrectly() {
        assertTrue(stateMachine.isOngoing(ServiceJourneyState.PENDING_ASSESSMENT));
        assertTrue(stateMachine.isOngoing(ServiceJourneyState.PENDING_HEALTH_ASSESSMENT));
        assertTrue(stateMachine.isOngoing(ServiceJourneyState.PENDING_AGREEMENT));
        assertTrue(stateMachine.isOngoing(ServiceJourneyState.IN_SERVICE));
        assertFalse(stateMachine.isOngoing(ServiceJourneyState.RENEWED));
        assertFalse(stateMachine.isOngoing(ServiceJourneyState.TERMINATED));
    }

    @Test
    void shouldMapPendingAgreementToExternalPendingHealthAssessment() {
        assertEquals(
                ServiceJourneyState.PENDING_HEALTH_ASSESSMENT,
                stateMachine.toExternalState(ServiceJourneyState.PENDING_AGREEMENT)
        );
    }

    @Test
    void shouldRequireReasonForWithdrawTransition() {
        assertThrows(
                IllegalArgumentException.class,
                () -> stateMachine.transition(
                        ServiceJourneyState.PENDING_HEALTH_ASSESSMENT,
                        ServiceJourneyEvent.JOURNEY_WITHDRAWN,
                        new ServiceJourneyTransitionContext(null))
        );
    }

    @Test
    void shouldRequireReasonForReturnTransition() {
        assertThrows(
                IllegalArgumentException.class,
                () -> stateMachine.transition(
                        ServiceJourneyState.PENDING_AGREEMENT,
                        ServiceJourneyEvent.RETURN_TO_HEALTH_ASSESSMENT,
                        new ServiceJourneyTransitionContext("  "))
        );
    }

    @Test
    void shouldResolveReviewTargetState() {
        assertEquals(ServiceJourneyState.TERMINATED,
                stateMachine.resolveReviewTargetState(ServiceReview.REVIEW_CONCLUSION_TERMINATE));
        assertEquals(ServiceJourneyState.RENEWED,
                stateMachine.resolveReviewTargetState(ServiceReview.REVIEW_CONCLUSION_RENEW));
        assertEquals(ServiceJourneyState.IMPROVEMENT_REQUIRED,
                stateMachine.resolveReviewTargetState(ServiceReview.REVIEW_CONCLUSION_IMPROVE));
    }

    @Test
    void shouldRejectUnknownReviewTargetState() {
        assertThrows(IllegalArgumentException.class,
                () -> stateMachine.resolveReviewTargetState("UNKNOWN"));
    }

    @Test
    void shouldResolveReturnEvent() {
        assertEquals(ServiceJourneyEvent.RETURN_TO_ASSESSMENT,
                stateMachine.resolveReturnEvent(
                        ServiceJourneyState.PENDING_HEALTH_ASSESSMENT,
                        ServiceJourneyState.PENDING_ASSESSMENT));
        assertEquals(ServiceJourneyEvent.RETURN_TO_HEALTH_ASSESSMENT,
                stateMachine.resolveReturnEvent(
                        ServiceJourneyState.PENDING_AGREEMENT,
                        ServiceJourneyState.PENDING_HEALTH_ASSESSMENT));
    }

    @Test
    void shouldRejectInvalidReturnEventResolution() {
        assertThrows(IllegalStateException.class,
                () -> stateMachine.resolveReturnEvent(
                        ServiceJourneyState.IN_SERVICE,
                        ServiceJourneyState.PENDING_HEALTH_ASSESSMENT));
    }

    @Test
    void shouldResolveJourneyMessage() {
        assertEquals("需求评估处理中",
                stateMachine.resolveJourneyMessage(
                        ServiceJourneyState.PENDING_ASSESSMENT,
                        ServiceApplication.STATUS_ASSESSED,
                        null));
        assertEquals("申请已撤回",
                stateMachine.resolveJourneyMessage(
                        ServiceJourneyState.TERMINATED,
                        ServiceApplication.STATUS_WITHDRAWN,
                        null));
        assertEquals("协议已终止",
                stateMachine.resolveJourneyMessage(
                        ServiceJourneyState.TERMINATED,
                        ServiceApplication.STATUS_PASSED,
                        ServiceAgreement.STATUS_TERMINATED));
    }

    @Test
    void shouldResolveIntakeRecordMessage() {
        assertEquals("需求评估处理中，暂不可重复发起申请",
                stateMachine.resolveIntakeRecordMessage(
                        ServiceJourneyState.PENDING_ASSESSMENT,
                        ServiceApplication.STATUS_ASSESSED,
                        null));
        assertEquals("申请已撤回，可重新发起申请",
                stateMachine.resolveIntakeRecordMessage(
                        ServiceJourneyState.TERMINATED,
                        ServiceApplication.STATUS_WITHDRAWN,
                        null));
        assertEquals("需求评估已通过，待完成健康评估与签约",
                stateMachine.resolveIntakeRecordMessage(
                        ServiceJourneyState.PENDING_HEALTH_ASSESSMENT,
                        ServiceApplication.STATUS_PASSED,
                        null));
    }

    @Test
    void shouldResolveReviewMessage() {
        assertEquals("服务评价结果为终止，协议已终止",
                stateMachine.resolveReviewMessage(ServiceJourneyState.TERMINATED, null));
        assertEquals("服务评价结果为续约，协议已续约",
                stateMachine.resolveReviewMessage(ServiceJourneyState.RENEWED, null));
        assertEquals("服务评价完成，建议结果：IMPROVE",
                stateMachine.resolveReviewMessage(
                        ServiceJourneyState.IMPROVEMENT_REQUIRED,
                        ServiceReview.REVIEW_CONCLUSION_IMPROVE));
    }
}
