package org.smart_elder_system.careorchestration.journey;

import org.smart_elder_system.admission.model.ServiceApplication;
import org.smart_elder_system.contract.model.ServiceAgreement;
import org.smart_elder_system.quality.model.ServiceReview;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ServiceJourneyStateMachine {

    public static final String HEALTH_ASSESSMENT_PENDING = "PENDING";
    public static final String HEALTH_ASSESSMENT_PASSED = "PASSED";
    public static final String HEALTH_ASSESSMENT_FAILED = "FAILED";

    private final Map<ServiceJourneyState, Map<ServiceJourneyEvent, ServiceJourneyTransitionRule>> transitions;

    public ServiceJourneyStateMachine() {
        this.transitions = new EnumMap<>(ServiceJourneyState.class);
        registerTransitions(List.of(
                new ServiceJourneyTransitionRule(
                        ServiceJourneyState.PENDING_ASSESSMENT,
                        ServiceJourneyEvent.ADMISSION_APPROVED,
                        ServiceJourneyState.PENDING_HEALTH_ASSESSMENT,
                        "journey:assessment:approve"),
                new ServiceJourneyTransitionRule(
                        ServiceJourneyState.PENDING_ASSESSMENT,
                        ServiceJourneyEvent.ADMISSION_REJECTED,
                        ServiceJourneyState.TERMINATED,
                        "journey:assessment:reject"),
                new ServiceJourneyTransitionRule(
                        ServiceJourneyState.PENDING_ASSESSMENT,
                        ServiceJourneyEvent.JOURNEY_WITHDRAWN,
                        ServiceJourneyState.TERMINATED,
                        "journey:withdraw",
                        true),
                new ServiceJourneyTransitionRule(
                        ServiceJourneyState.PENDING_HEALTH_ASSESSMENT,
                        ServiceJourneyEvent.RETURN_TO_ASSESSMENT,
                        ServiceJourneyState.PENDING_ASSESSMENT,
                        "journey:return:assessment",
                        true),
                new ServiceJourneyTransitionRule(
                        ServiceJourneyState.PENDING_HEALTH_ASSESSMENT,
                        ServiceJourneyEvent.HEALTH_APPROVED,
                        ServiceJourneyState.PENDING_AGREEMENT,
                        "journey:health:approve"),
                new ServiceJourneyTransitionRule(
                        ServiceJourneyState.PENDING_HEALTH_ASSESSMENT,
                        ServiceJourneyEvent.JOURNEY_WITHDRAWN,
                        ServiceJourneyState.TERMINATED,
                        "journey:withdraw",
                        true),
                new ServiceJourneyTransitionRule(
                        ServiceJourneyState.PENDING_HEALTH_ASSESSMENT,
                        ServiceJourneyEvent.HEALTH_REJECTED,
                        ServiceJourneyState.TERMINATED,
                        "journey:health:reject"),
                new ServiceJourneyTransitionRule(
                        ServiceJourneyState.PENDING_AGREEMENT,
                        ServiceJourneyEvent.RETURN_TO_HEALTH_ASSESSMENT,
                        ServiceJourneyState.PENDING_HEALTH_ASSESSMENT,
                        "journey:return:health",
                        true),
                new ServiceJourneyTransitionRule(
                        ServiceJourneyState.PENDING_AGREEMENT,
                        ServiceJourneyEvent.AGREEMENT_SIGNED,
                        ServiceJourneyState.IN_SERVICE,
                        "journey:agreement:sign"),
                new ServiceJourneyTransitionRule(
                        ServiceJourneyState.IN_SERVICE,
                        ServiceJourneyEvent.REVIEW_IMPROVE,
                        ServiceJourneyState.IMPROVEMENT_REQUIRED,
                        "journey:review:improve"),
                new ServiceJourneyTransitionRule(
                        ServiceJourneyState.IN_SERVICE,
                        ServiceJourneyEvent.REVIEW_RENEW,
                        ServiceJourneyState.RENEWED,
                        "journey:review:renew"),
                new ServiceJourneyTransitionRule(
                        ServiceJourneyState.IN_SERVICE,
                        ServiceJourneyEvent.REVIEW_TERMINATE,
                        ServiceJourneyState.TERMINATED,
                        "journey:review:terminate"),
                new ServiceJourneyTransitionRule(
                        ServiceJourneyState.IMPROVEMENT_REQUIRED,
                        ServiceJourneyEvent.REVIEW_TERMINATE,
                        ServiceJourneyState.TERMINATED,
                        "journey:review:terminate"),
                new ServiceJourneyTransitionRule(
                        ServiceJourneyState.IMPROVEMENT_REQUIRED,
                        ServiceJourneyEvent.REVIEW_RENEW,
                        ServiceJourneyState.RENEWED,
                        "journey:review:renew")
        ));
    }

    public ServiceJourneyState initialState(ServiceJourneyEvent event) {
        if (event != ServiceJourneyEvent.APPLICATION_SUBMITTED) {
            throw new IllegalArgumentException("仅支持通过 APPLICATION_SUBMITTED 初始化旅程状态");
        }
        return ServiceJourneyState.PENDING_ASSESSMENT;
    }

    public boolean canTransit(ServiceJourneyState fromState, ServiceJourneyEvent event) {
        return transitions.getOrDefault(fromState, Map.of()).containsKey(event);
    }

    public ServiceJourneyState transit(ServiceJourneyState fromState, ServiceJourneyEvent event) {
        return getRule(fromState, event).toState();
    }

    public ServiceJourneyTransitionResult transition(
            ServiceJourneyState fromState,
            ServiceJourneyEvent event,
            ServiceJourneyTransitionContext context) {
        ServiceJourneyTransitionRule rule = getRule(fromState, event);
        validateRule(rule, context);
        return new ServiceJourneyTransitionResult(
                fromState,
                event,
                rule.toState(),
                rule.requiredAuthority()
        );
    }

    public ServiceJourneyTransitionRule getRule(ServiceJourneyState fromState, ServiceJourneyEvent event) {
        ServiceJourneyTransitionRule rule = transitions.getOrDefault(fromState, Map.of()).get(event);
        if (rule == null) {
            throw new IllegalArgumentException("当前旅程状态不允许执行该操作: " + fromState + " -> " + event);
        }
        return rule;
    }

    public ServiceJourneyState deriveCurrentState(ServiceJourneyFacts facts) {
        if (facts == null || facts.getApplicationStatus() == null || facts.getApplicationStatus().isBlank()) {
            return ServiceJourneyState.TERMINATED;
        }

        if (ServiceApplication.STATUS_SUBMITTED.equals(facts.getApplicationStatus())
                || ServiceApplication.STATUS_ASSESSED.equals(facts.getApplicationStatus())) {
            return ServiceJourneyState.PENDING_ASSESSMENT;
        }

        if (ServiceApplication.STATUS_FAILED.equals(facts.getApplicationStatus())
                || ServiceApplication.STATUS_WITHDRAWN.equals(facts.getApplicationStatus())) {
            return ServiceJourneyState.TERMINATED;
        }

        if (!ServiceApplication.STATUS_PASSED.equals(facts.getApplicationStatus())) {
            return ServiceJourneyState.TERMINATED;
        }

        if (ServiceAgreement.STATUS_TERMINATED.equals(facts.getAgreementStatus())) {
            return ServiceJourneyState.TERMINATED;
        }

        if (ServiceAgreement.STATUS_RENEWED.equals(facts.getAgreementStatus())
                || ServiceReview.REVIEW_CONCLUSION_RENEW.equals(facts.getReviewConclusion())) {
            return ServiceJourneyState.RENEWED;
        }

        if (ServiceAgreement.STATUS_DRAFT.equals(facts.getAgreementStatus())
                && !HEALTH_ASSESSMENT_PASSED.equals(facts.getHealthAssessmentStatus())) {
            return ServiceJourneyState.PENDING_HEALTH_ASSESSMENT;
        }

        if (ServiceReview.REVIEW_CONCLUSION_TERMINATE.equals(facts.getReviewConclusion())) {
            return ServiceJourneyState.TERMINATED;
        }

        if (ServiceReview.REVIEW_CONCLUSION_IMPROVE.equals(facts.getReviewConclusion())) {
            return ServiceJourneyState.IMPROVEMENT_REQUIRED;
        }

        if (ServiceAgreement.STATUS_ACTIVE.equals(facts.getAgreementStatus())) {
            return ServiceJourneyState.IN_SERVICE;
        }

        if (facts.getAgreementStatus() != null && !facts.getAgreementStatus().isBlank()) {
            return ServiceJourneyState.PENDING_AGREEMENT;
        }

        if (HEALTH_ASSESSMENT_FAILED.equals(facts.getHealthAssessmentStatus())) {
            return ServiceJourneyState.TERMINATED;
        }

        if (HEALTH_ASSESSMENT_PASSED.equals(facts.getHealthAssessmentStatus())) {
            return ServiceJourneyState.PENDING_AGREEMENT;
        }

        return ServiceJourneyState.PENDING_HEALTH_ASSESSMENT;
    }

    public String getDefaultMessage(ServiceJourneyState state) {
        return switch (state) {
            case PENDING_ASSESSMENT -> "申请已提交，待管理端完成需求评估";
            case PENDING_HEALTH_ASSESSMENT -> "需求评估已通过，待完成健康评估后继续签约";
            case PENDING_AGREEMENT -> "健康评估已通过，待签订服务协议";
            case IN_SERVICE -> "已签订有效协议，服务执行中";
            case IMPROVEMENT_REQUIRED -> "服务评价完成，建议继续改进";
            case RENEWED -> "服务评价结果为续约，协议已续约";
            case TERMINATED -> "当前申请已结束";
        };
    }

    public ServiceJourneyState toExternalState(ServiceJourneyState state) {
        if (state == ServiceJourneyState.PENDING_AGREEMENT) {
            return ServiceJourneyState.PENDING_HEALTH_ASSESSMENT;
        }
        return state;
    }

    public String getAssessmentFailureMessage() {
        return "需求评估未通过，服务终止";
    }

    public String getHealthFailureMessage() {
        return "健康评估未通过，服务终止";
    }

    public String getWithdrawnMessage() {
        return "申请已撤回";
    }

    public String getInServiceCreatedMessage() {
        return "需求评估与健康评估均已通过，已签订协议并进入在服状态";
    }

    public String getImproveMessage(String reviewConclusion) {
        return "服务评价完成，建议结果：" + reviewConclusion;
    }

    public boolean isOngoing(ServiceJourneyState state) {
        return state != null && state.isOngoing();
    }

    public Optional<ServiceJourneyEvent> resolveReviewEvent(String reviewConclusion) {
        if (ServiceReview.REVIEW_CONCLUSION_TERMINATE.equals(reviewConclusion)) {
            return Optional.of(ServiceJourneyEvent.REVIEW_TERMINATE);
        }
        if (ServiceReview.REVIEW_CONCLUSION_RENEW.equals(reviewConclusion)) {
            return Optional.of(ServiceJourneyEvent.REVIEW_RENEW);
        }
        if (ServiceReview.REVIEW_CONCLUSION_IMPROVE.equals(reviewConclusion)) {
            return Optional.of(ServiceJourneyEvent.REVIEW_IMPROVE);
        }
        return Optional.empty();
    }

    public ServiceJourneyState resolveReviewTargetState(String reviewConclusion) {
        return switch (reviewConclusion) {
            case ServiceReview.REVIEW_CONCLUSION_TERMINATE -> ServiceJourneyState.TERMINATED;
            case ServiceReview.REVIEW_CONCLUSION_RENEW -> ServiceJourneyState.RENEWED;
            case ServiceReview.REVIEW_CONCLUSION_IMPROVE -> ServiceJourneyState.IMPROVEMENT_REQUIRED;
            default -> throw new IllegalArgumentException("未知的服务评价结论: " + reviewConclusion);
        };
    }

    public ServiceJourneyEvent resolveReturnEvent(ServiceJourneyState currentState, ServiceJourneyState targetState) {
        if (currentState == ServiceJourneyState.PENDING_HEALTH_ASSESSMENT
                && targetState == ServiceJourneyState.PENDING_ASSESSMENT) {
            return ServiceJourneyEvent.RETURN_TO_ASSESSMENT;
        }
        if (currentState == ServiceJourneyState.PENDING_AGREEMENT
                && targetState == ServiceJourneyState.PENDING_HEALTH_ASSESSMENT) {
            return ServiceJourneyEvent.RETURN_TO_HEALTH_ASSESSMENT;
        }
        throw new IllegalStateException("当前旅程状态不允许退回到目标节点");
    }

    public String resolveJourneyMessage(ServiceJourneyState state, String applicationStatus, String agreementStatus) {
        if (state == ServiceJourneyState.PENDING_ASSESSMENT && ServiceApplication.STATUS_ASSESSED.equals(applicationStatus)) {
            return "需求评估处理中";
        }
        if (state == ServiceJourneyState.TERMINATED && ServiceApplication.STATUS_WITHDRAWN.equals(applicationStatus)) {
            return getWithdrawnMessage();
        }
        if (state == ServiceJourneyState.TERMINATED && ServiceApplication.STATUS_FAILED.equals(applicationStatus)) {
            return getAssessmentFailureMessage();
        }
        if (state == ServiceJourneyState.TERMINATED && ServiceAgreement.STATUS_TERMINATED.equals(agreementStatus)) {
            return "协议已终止";
        }
        return getDefaultMessage(state);
    }

    public String resolveIntakeRecordMessage(ServiceJourneyState state, String applicationStatus, String agreementStatus) {
        if (state == ServiceJourneyState.PENDING_ASSESSMENT && ServiceApplication.STATUS_ASSESSED.equals(applicationStatus)) {
            return "需求评估处理中，暂不可重复发起申请";
        }
        if (state == ServiceJourneyState.TERMINATED && ServiceApplication.STATUS_WITHDRAWN.equals(applicationStatus)) {
            return "申请已撤回，可重新发起申请";
        }
        if (state == ServiceJourneyState.TERMINATED && ServiceApplication.STATUS_FAILED.equals(applicationStatus)) {
            return getAssessmentFailureMessage();
        }
        if (state == ServiceJourneyState.TERMINATED && ServiceAgreement.STATUS_TERMINATED.equals(agreementStatus)) {
            return "协议已终止";
        }
        if (state == ServiceJourneyState.TERMINATED) {
            return "当前申请已结束，可发起新申请";
        }
        if (state == ServiceJourneyState.PENDING_HEALTH_ASSESSMENT) {
            return "需求评估已通过，待完成健康评估与签约";
        }
        return getDefaultMessage(state);
    }

    public String resolveReviewMessage(ServiceJourneyState state, String reviewConclusion) {
        if (state == ServiceJourneyState.TERMINATED) {
            return "服务评价结果为终止，协议已终止";
        }
        if (state == ServiceJourneyState.RENEWED) {
            return getDefaultMessage(state);
        }
        return getImproveMessage(reviewConclusion);
    }

    private void registerTransitions(List<ServiceJourneyTransitionRule> rules) {
        for (ServiceJourneyTransitionRule rule : rules) {
            transitions
                    .computeIfAbsent(rule.fromState(), ignored -> new EnumMap<>(ServiceJourneyEvent.class))
                    .put(rule.event(), rule);
        }
    }

    private void validateRule(ServiceJourneyTransitionRule rule, ServiceJourneyTransitionContext context) {
        if (rule.reasonRequired()) {
            String reason = context == null ? null : context.reason();
            if (reason == null || reason.isBlank()) {
                throw new IllegalArgumentException("当前旅程操作必须填写原因");
            }
        }
    }
}
