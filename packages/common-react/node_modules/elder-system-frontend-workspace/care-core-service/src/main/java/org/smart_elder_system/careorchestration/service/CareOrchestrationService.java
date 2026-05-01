package org.smart_elder_system.careorchestration.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.smart_elder_system.admission.model.ServiceApplication;
import org.smart_elder_system.admission.po.ServiceApplicationPo;
import org.smart_elder_system.admission.repository.ServiceApplicationRepository;
import org.smart_elder_system.admission.service.AdmissionService;
import org.smart_elder_system.caredelivery.model.CarePlan;
import org.smart_elder_system.caredelivery.po.CarePlanPo;
import org.smart_elder_system.caredelivery.repository.CarePlanRepository;
import org.smart_elder_system.caredelivery.service.CareDeliveryService;
import org.smart_elder_system.careorchestration.dto.CareAnalyticsOverviewDTO;
import org.smart_elder_system.careorchestration.dto.CareAnalyticsTrendsDTO;
import org.smart_elder_system.careorchestration.dto.ServiceJourneyTaskItemDTO;
import org.smart_elder_system.careorchestration.dto.ServiceJourneyTaskOverviewDTO;
import org.smart_elder_system.careorchestration.dto.ServiceJourneyTransitionLogItemDTO;
import org.smart_elder_system.careorchestration.feign.AdmissionClient;
import org.smart_elder_system.careorchestration.feign.CareDeliveryClient;
import org.smart_elder_system.careorchestration.feign.ContractClient;
import org.smart_elder_system.careorchestration.feign.HealthClient;
import org.smart_elder_system.careorchestration.feign.QualityClient;
import org.smart_elder_system.careorchestration.journey.ServiceJourneyEvent;
import org.smart_elder_system.careorchestration.journey.ServiceJourneyFacts;
import org.smart_elder_system.careorchestration.journey.ServiceJourneyState;
import org.smart_elder_system.careorchestration.journey.ServiceJourneyStateMachine;
import org.smart_elder_system.careorchestration.journey.ServiceJourneyTransitionContext;
import org.smart_elder_system.careorchestration.journey.ServiceJourneyTransitionResult;
import org.smart_elder_system.careorchestration.security.ServiceJourneyTransitionPolicy;
import org.smart_elder_system.common.dto.care.CarePlanDTO;
import org.smart_elder_system.common.dto.care.HealthAssessmentDTO;
import org.smart_elder_system.common.dto.care.HealthAssessmentRequestDTO;
import org.smart_elder_system.common.dto.care.HealthAssessmentSubmitDTO;
import org.smart_elder_system.common.dto.care.HealthProfileDTO;
import org.smart_elder_system.common.dto.care.IntakeRecordDTO;
import org.smart_elder_system.common.dto.care.ServiceAgreementDTO;
import org.smart_elder_system.common.dto.care.ServiceApplicationDTO;
import org.smart_elder_system.common.dto.care.ServiceJourneyResultDTO;
import org.smart_elder_system.common.dto.care.ServiceReviewDTO;
import org.smart_elder_system.common.dto.care.EligibilityAssessmentDTO;
import org.smart_elder_system.contract.model.ServiceAgreement;
import org.smart_elder_system.contract.po.ServiceAgreementPo;
import org.smart_elder_system.contract.repository.ServiceAgreementRepository;
import org.smart_elder_system.contract.service.ContractService;
import org.smart_elder_system.health.model.HealthAssessmentRecord;
import org.smart_elder_system.health.model.HealthProfile;
import org.smart_elder_system.health.po.HealthAssessmentRecordPo;
import org.smart_elder_system.health.po.HealthProfilePo;
import org.smart_elder_system.health.repository.HealthAssessmentRecordRepository;
import org.smart_elder_system.health.repository.HealthProfileRepository;
import org.smart_elder_system.health.service.HealthService;
import org.smart_elder_system.quality.model.ServiceReview;
import org.smart_elder_system.quality.po.ServiceReviewPo;
import org.smart_elder_system.quality.repository.ServiceReviewRepository;
import org.smart_elder_system.quality.service.QualityService;

import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CareOrchestrationService {

    private static final String ASSESSMENT_TYPE_PRE_SIGN_PASS = "PRE_SIGN_PASS";
    private static final String ASSESSMENT_TYPE_PRE_SIGN_FAIL = "PRE_SIGN_FAIL";
    private static final String ASSESSMENT_TYPE_INTAKE = "INTAKE";

    private final AdmissionClient admissionClient;
    private final ContractClient contractClient;
    private final CareDeliveryClient careDeliveryClient;
    private final HealthClient healthClient;
    private final QualityClient qualityClient;
    private final AdmissionService admissionService;
    private final ContractService contractService;
    private final CareDeliveryService careDeliveryService;
    private final HealthService healthService;
    private final QualityService qualityService;
    private final ServiceApplicationRepository serviceApplicationRepository;
    private final ServiceAgreementRepository serviceAgreementRepository;
    private final CarePlanRepository carePlanRepository;
    private final HealthProfileRepository healthProfileRepository;
    private final HealthAssessmentRecordRepository healthAssessmentRecordRepository;
    private final ServiceReviewRepository serviceReviewRepository;
    private final ServiceJourneyStateMachine serviceJourneyStateMachine;
    private final ServiceJourneyTransitionPolicy serviceJourneyTransitionPolicy;
    private final ServiceJourneyTaskService serviceJourneyTaskService;
    private final ServiceJourneyTransitionLogService serviceJourneyTransitionLogService;

    public String getServiceJourneyOverview() {
        return "服务流程总览：申请受理 -> 需求评估 -> 协议签订 -> 服务执行 -> 健康管理 -> 评价改进 -> 到期续约/终止";
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceJourneyResultDTO startServiceJourney(
            Long elderId,
            Long guardianId,
            String applicantName,
            String contactPhone,
            String serviceScene,
            String serviceRequest) {

        List<ServiceApplicationPo> existingApplications = serviceApplicationRepository.findByElderIdForUpdate(elderId);
        boolean hasOngoing = existingApplications.stream()
                .map(this::deriveJourneyState)
                .anyMatch(serviceJourneyStateMachine::isOngoing);
        if (hasOngoing) {
            throw new IllegalArgumentException("该老人存在进行中的受理记录，不可重复发起申请");
        }

        ServiceApplicationDTO application = new ServiceApplicationDTO();
        application.setElderId(elderId);
        application.setGuardianId(guardianId);
        application.setApplicantName(applicantName);
        application.setContactPhone(contactPhone);
        application.setServiceScene(serviceScene);
        application.setServiceRequest(serviceRequest);

        ServiceApplicationDTO submittedApplication = admissionService.submitApplication(application);

        ServiceJourneyState currentState = serviceJourneyStateMachine.initialState(ServiceJourneyEvent.APPLICATION_SUBMITTED);

        if (!serviceJourneyTransitionLogService.hasTransition(
                submittedApplication.getApplicationId(),
                ServiceJourneyEvent.APPLICATION_SUBMITTED,
                currentState)) {
            serviceJourneyTransitionLogService.logTransition(
                    submittedApplication.getApplicationId(),
                    null,
                    submittedApplication.getElderId(),
                    null,
                    ServiceJourneyEvent.APPLICATION_SUBMITTED,
                    currentState,
                    "申请受理提交成功",
                    application);
        }
        serviceJourneyTaskService.createAdmissionAssessmentTask(
                submittedApplication.getApplicationId(),
                submittedApplication.getElderId());

        return buildJourneyResult(
                submittedApplication.getApplicationId(),
                submittedApplication.getElderId(),
                null,
                currentState,
                serviceJourneyStateMachine.getDefaultMessage(currentState));
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceJourneyResultDTO continueAfterAssessment(Long applicationId) {
        JourneyContext context = loadJourneyContextForUpdate(applicationId);

        return switch (context.currentState()) {
            case TERMINATED -> continueTerminatedJourney(
                    context.application(),
                    context.agreement(),
                    context.healthAssessmentStatus());
            case PENDING_HEALTH_ASSESSMENT -> continueToPendingHealthAssessment(
                    context.application(),
                    context.agreement(),
                    context.healthAssessmentStatus());
            case PENDING_AGREEMENT, IN_SERVICE -> continueToInService(
                    context.application(),
                    context.agreement(),
                    context.preSignAssessment(),
                    context.currentState());
            default -> throw new IllegalStateException("当前旅程状态不允许继续推进");
        };
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceJourneyResultDTO rejectAdmissionJourney(Long applicationId, String assessmentConclusion, String assessor) {
        ServiceApplicationPo application = serviceApplicationRepository.findByIdForUpdate(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("未找到服务申请"));
        if (ServiceApplication.STATUS_FAILED.equals(application.getStatus())) {
            return buildJourneyResult(application.getId(), application.getElderId(), null,
                    ServiceJourneyState.TERMINATED, serviceJourneyStateMachine.getAssessmentFailureMessage());
        }

        ServiceJourneyState currentState = deriveJourneyState(application, null, null);
        ServiceJourneyTransitionResult transition = transition(
                currentState,
                ServiceJourneyEvent.ADMISSION_REJECTED,
                assessmentConclusion);
        serviceJourneyTransitionPolicy.requireAuthority(transition.requiredAuthority());

        EligibilityAssessmentDTO assessment = new EligibilityAssessmentDTO();
        assessment.setApplicationId(applicationId);
        assessment.setEligible(false);
        assessment.setAssessmentConclusion(assessmentConclusion);
        assessment.setAssessor(assessor);
        ServiceApplicationDTO rejectedApplication = admissionService.assessEligibility(assessment);

        if (!serviceJourneyTransitionLogService.hasTransition(applicationId, ServiceJourneyEvent.ADMISSION_REJECTED, ServiceJourneyState.TERMINATED)) {
            serviceJourneyTransitionLogService.logTransition(
                    rejectedApplication.getApplicationId(),
                    null,
                    rejectedApplication.getElderId(),
                    transition.fromState(),
                    transition.event(),
                    transition.toState(),
                    serviceJourneyStateMachine.getAssessmentFailureMessage(),
                    rejectedApplication);
        }
        serviceJourneyTaskService.completeOpenTask(
                rejectedApplication.getApplicationId(),
                ServiceJourneyTaskService.TASK_TYPE_ADMISSION_ASSESSMENT);

        return buildJourneyResult(
                rejectedApplication.getApplicationId(),
                rejectedApplication.getElderId(),
                null,
                transition.toState(),
                serviceJourneyStateMachine.getAssessmentFailureMessage());
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceJourneyResultDTO rejectHealthJourney(
            Long applicationId,
            String assessmentConclusion,
            String assessor,
            String responsibleDoctor,
            Integer score) {
        JourneyContext context = loadJourneyContextForUpdate(applicationId);
        if (ServiceJourneyStateMachine.HEALTH_ASSESSMENT_FAILED.equals(context.healthAssessmentStatus())) {
            return buildJourneyResult(
                    applicationId,
                    context.application().getElderId(),
                    context.agreement() == null ? null : context.agreement().getId(),
                    ServiceJourneyState.TERMINATED,
                    serviceJourneyStateMachine.getHealthFailureMessage());
        }

        ServiceJourneyTransitionResult transition = transition(
                context.currentState(),
                ServiceJourneyEvent.HEALTH_REJECTED,
                assessmentConclusion);
        serviceJourneyTransitionPolicy.requireAuthority(transition.requiredAuthority());

        return rejectHealthJourneyWithSideEffects(
                context.application(),
                context.agreement(),
                transition,
                applicationId,
                assessmentConclusion,
                assessor,
                responsibleDoctor,
                score);
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceJourneyResultDTO withdrawServiceJourney(Long applicationId, String reason) {
        JourneyContext context = loadJourneyContextForUpdate(applicationId);
        if (ServiceApplication.STATUS_WITHDRAWN.equals(context.application().getStatus())) {
            return buildJourneyResult(
                    context.application().getId(),
                    context.application().getElderId(),
                    context.agreement() == null ? null : context.agreement().getId(),
                    ServiceJourneyState.TERMINATED,
                    serviceJourneyStateMachine.getWithdrawnMessage());
        }

        ServiceJourneyTransitionResult transition = transition(
                context.currentState(),
                ServiceJourneyEvent.JOURNEY_WITHDRAWN,
                reason);
        serviceJourneyTransitionPolicy.requireAuthority(transition.requiredAuthority());

        return withdrawServiceJourneyWithSideEffects(
                context.application(),
                context.agreement(),
                transition,
                applicationId,
                reason);
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceJourneyResultDTO reviewAndFinalize(Long agreementId, Long elderId, Integer satisfactionScore, String reviewComment) {
        ReviewContext context = loadReviewContextForUpdate(agreementId);
        ReviewDecision decision = buildReviewDecision(agreementId, elderId, satisfactionScore, reviewComment);
        if (context.currentState() == decision.requestedState()) {
            serviceJourneyTransitionPolicy.requireAuthority(decision.reviewEvent());
            return buildJourneyResult(
                    context.agreement().getApplicationId(),
                    elderId,
                    agreementId,
                    decision.requestedState(),
                    serviceJourneyStateMachine.resolveReviewMessage(decision.requestedState(), decision.reviewConclusion()));
        }

        ServiceJourneyTransitionResult transition = transition(
                context.currentState(),
                decision.reviewEvent(),
                reviewComment);
        serviceJourneyTransitionPolicy.requireAuthority(transition.requiredAuthority());

        return reviewAndFinalizeWithSideEffects(
                context.agreement(),
                decision.review(),
                transition);
    }

    public Page<ServiceJourneyTaskItemDTO> listJourneyTasks(
            Long applicationId,
            Long elderId,
            Long agreementId,
            String taskType,
            List<String> statuses,
            String assigneeRole,
            Integer page,
            Integer size,
            String sortBy,
            String sortOrder) {
        return serviceJourneyTaskService.listTasks(
                applicationId,
                elderId,
                agreementId,
                taskType,
                statuses,
                assigneeRole,
                page == null ? 0 : page,
                size == null ? 20 : Math.min(size, 100),
                sortBy,
                sortOrder);
    }

    public List<ServiceJourneyTaskItemDTO> listJourneyTaskTimeline(Long applicationId) {
        return serviceJourneyTaskService.listTaskTimeline(applicationId);
    }

    public ServiceJourneyTaskOverviewDTO getJourneyTaskOverview(
            Long applicationId,
            Long elderId,
            Long agreementId,
            String taskType,
            List<String> statuses,
            String assigneeRole) {
        return serviceJourneyTaskService.getTaskOverview(
                applicationId,
                elderId,
                agreementId,
                taskType,
                statuses,
                assigneeRole);
    }

    public List<ServiceJourneyTransitionLogItemDTO> listJourneyTransitionLogsByApplication(Long applicationId) {
        return serviceJourneyTransitionLogService.listByApplicationId(applicationId);
    }

    public List<ServiceJourneyTransitionLogItemDTO> listJourneyTransitionLogsByAgreement(Long agreementId) {
        return serviceJourneyTransitionLogService.listByAgreementId(agreementId);
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceJourneyResultDTO returnJourneyStep(Long applicationId, ServiceJourneyState targetState, String reason) {
        JourneyContext context = loadJourneyContextForUpdate(applicationId);

        if (context.currentState() == targetState) {
            return buildJourneyResult(
                    applicationId,
                    context.application().getElderId(),
                    context.agreement() == null ? null : context.agreement().getId(),
                    targetState,
                    serviceJourneyStateMachine.getDefaultMessage(targetState));
        }

        ServiceJourneyEvent event = serviceJourneyStateMachine.resolveReturnEvent(context.currentState(), targetState);
        ServiceJourneyTransitionResult transition = transition(context.currentState(), event, reason);
        serviceJourneyTransitionPolicy.requireAuthority(transition.requiredAuthority());

        return returnJourneyStepWithSideEffects(
                context.application(),
                context.agreement(),
                transition,
                applicationId,
                reason);
    }

    public List<IntakeRecordDTO> listIntakeRecords(Long elderId) {
        List<ServiceApplicationPo> applications = serviceApplicationRepository.findByElderIdOrderBySubmittedAtDesc(elderId);
        return toIntakeRecords(applications);
    }

    public List<IntakeRecordDTO> listIntakeRecordsByApplicant(String applicantName) {
        List<ServiceApplicationPo> applications = serviceApplicationRepository.findByApplicantNameOrderBySubmittedAtDesc(applicantName);
        return toIntakeRecords(applications);
    }

    public ServiceJourneyResultDTO getLatestJourneyResultByApplicant(String applicantName) {
        Optional<ServiceApplicationPo> latestApplication = serviceApplicationRepository
                .findTopByApplicantNameOrderBySubmittedAtDescIdDesc(applicantName);

        if (latestApplication.isEmpty()) {
            return new ServiceJourneyResultDTO();
        }

        ServiceApplicationPo application = latestApplication.get();
        ServiceAgreementPo agreement = serviceAgreementRepository
                .findTopByApplicationIdOrderByIdDesc(application.getId())
                .orElse(null);
        JourneyQuerySummary summary = buildJourneyQuerySummary(application, agreement);

        ServiceJourneyResultDTO result = new ServiceJourneyResultDTO();
        result.setApplicationId(application.getId());
        result.setElderId(application.getElderId());
        if (agreement != null) {
            result.setAgreementId(agreement.getId());
        }
        result.setFinalStatus(summary.externalState().name());
        result.setMessage(summary.journeyMessage());
        return result;
    }

    public CareAnalyticsOverviewDTO getAnalyticsOverview() {
        int applicationsTotal = (int) serviceApplicationRepository.count();
        int agreementsActive = (int) serviceAgreementRepository.countByStatus(ServiceAgreement.STATUS_ACTIVE);
        int plansInProgress = (int) carePlanRepository.countByStatus(CarePlan.STATUS_IN_PROGRESS);

        Double avg = serviceReviewRepository.averageSatisfaction();
        int averageSatisfaction = avg == null ? 0 : (int) Math.round(avg);

        List<CareAnalyticsOverviewDTO.StagePoint> stages = new ArrayList<>();
        stages.add(stage("申请受理", applicationsTotal));
        stages.add(stage("健康评估", (int) serviceApplicationRepository.countByStatus(ServiceApplication.STATUS_ASSESSED)));
        stages.add(stage("签约", (int) serviceAgreementRepository.countByStatus(ServiceAgreement.STATUS_ACTIVE)));
        stages.add(stage("照护执行", (int) carePlanRepository.countByStatus(CarePlan.STATUS_IN_PROGRESS)));
        stages.add(stage("质量回访", (int) serviceReviewRepository.count()));

        CareAnalyticsOverviewDTO dto = new CareAnalyticsOverviewDTO();
        dto.setApplicationsTotal(applicationsTotal);
        dto.setAgreementsActive(agreementsActive);
        dto.setPlansInProgress(plansInProgress);
        dto.setAverageSatisfaction(averageSatisfaction);
        dto.setStageDistribution(stages);
        return dto;
    }

    public CareAnalyticsTrendsDTO getAnalyticsTrends(Integer days) {
        int actualDays = days == null ? 30 : Math.max(7, Math.min(days, 90));
        LocalDate startDate = LocalDate.now().minusDays(actualDays - 1L);
        LocalDateTime startTime = startDate.atStartOfDay();

        Map<String, Integer> applicationMap = buildDateMap(startDate, actualDays);
        Map<String, Integer> agreementMap = buildDateMap(startDate, actualDays);
        Map<String, Integer> reviewMap = buildDateMap(startDate, actualDays);

        fillMapFromRows(applicationMap, serviceApplicationRepository.countSubmittedDaily(startTime));
        fillMapFromRows(agreementMap, serviceAgreementRepository.countEffectiveDaily(startDate));
        fillMapFromRows(reviewMap, serviceReviewRepository.countReviewedDaily(startTime));

        CareAnalyticsTrendsDTO dto = new CareAnalyticsTrendsDTO();
        dto.setApplicationTrend(toTrendPoints(applicationMap));
        dto.setAgreementTrend(toTrendPoints(agreementMap));
        dto.setReviewTrend(toTrendPoints(reviewMap));
        return dto;
    }

    private ServiceJourneyResultDTO rejectHealthJourneyWithSideEffects(
            ServiceApplicationPo application,
            ServiceAgreementPo agreement,
            ServiceJourneyTransitionResult transition,
            Long applicationId,
            String assessmentConclusion,
            String assessor,
            String responsibleDoctor,
            Integer score) {
        HealthAssessmentSubmitDTO submitDTO = new HealthAssessmentSubmitDTO();
        submitDTO.setApplicationId(applicationId);
        submitDTO.setPassed(false);
        submitDTO.setAssessmentConclusion(assessmentConclusion);
        submitDTO.setAssessor(assessor);
        submitDTO.setResponsibleDoctor(responsibleDoctor);
        submitDTO.setScore(score);
        HealthAssessmentRequestDTO rejectedHealthAssessment = healthService.submitPreSignAssessment(submitDTO);

        if (!serviceJourneyTransitionLogService.hasTransition(applicationId, transition.event(), transition.toState())) {
            serviceJourneyTransitionLogService.logTransition(
                    applicationId,
                    agreement == null ? null : agreement.getId(),
                    application.getElderId(),
                    transition.fromState(),
                    transition.event(),
                    transition.toState(),
                    serviceJourneyStateMachine.getHealthFailureMessage(),
                    rejectedHealthAssessment);
        }
        serviceJourneyTaskService.completeOpenTask(
                applicationId,
                ServiceJourneyTaskService.TASK_TYPE_HEALTH_ASSESSMENT);

        return buildJourneyResult(
                applicationId,
                application.getElderId(),
                agreement == null ? null : agreement.getId(),
                transition.toState(),
                serviceJourneyStateMachine.getHealthFailureMessage());
    }

    private ServiceJourneyResultDTO withdrawServiceJourneyWithSideEffects(
            ServiceApplicationPo application,
            ServiceAgreementPo agreement,
            ServiceJourneyTransitionResult transition,
            Long applicationId,
            String reason) {
        ServiceApplicationDTO withdrawnApplication = admissionService.withdrawApplication(applicationId, reason);
        if (!serviceJourneyTransitionLogService.hasTransition(applicationId, transition.event(), transition.toState())) {
            serviceJourneyTransitionLogService.logTransition(
                    withdrawnApplication.getApplicationId(),
                    agreement == null ? null : agreement.getId(),
                    withdrawnApplication.getElderId(),
                    transition.fromState(),
                    transition.event(),
                    transition.toState(),
                    serviceJourneyStateMachine.getWithdrawnMessage(),
                    withdrawnApplication);
        }
        serviceJourneyTaskService.completeOpenTask(
                withdrawnApplication.getApplicationId(),
                ServiceJourneyTaskService.TASK_TYPE_ADMISSION_ASSESSMENT);
        serviceJourneyTaskService.completeOpenTask(
                withdrawnApplication.getApplicationId(),
                ServiceJourneyTaskService.TASK_TYPE_HEALTH_ASSESSMENT);

        return buildJourneyResult(
                withdrawnApplication.getApplicationId(),
                withdrawnApplication.getElderId(),
                agreement == null ? null : agreement.getId(),
                ServiceJourneyState.TERMINATED,
                serviceJourneyStateMachine.getWithdrawnMessage());
    }

    private ServiceJourneyResultDTO returnJourneyStepWithSideEffects(
            ServiceApplicationPo application,
            ServiceAgreementPo agreement,
            ServiceJourneyTransitionResult transition,
            Long applicationId,
            String reason) {
        ServiceJourneyState targetState = transition.toState();
        if (transition.event() == ServiceJourneyEvent.RETURN_TO_ASSESSMENT) {
            ServiceApplicationDTO returnedApplication = admissionService.revertToAssessment(applicationId, reason);
            if (!serviceJourneyTransitionLogService.hasTransition(applicationId, transition.event(), targetState)) {
                serviceJourneyTransitionLogService.logTransition(
                        returnedApplication.getApplicationId(),
                        agreement == null ? null : agreement.getId(),
                        returnedApplication.getElderId(),
                        transition.fromState(),
                        transition.event(),
                        targetState,
                        reason,
                        returnedApplication);
            }
            serviceJourneyTaskService.cancelOpenTask(applicationId, ServiceJourneyTaskService.TASK_TYPE_HEALTH_ASSESSMENT);
            serviceJourneyTaskService.createAdmissionAssessmentTask(returnedApplication.getApplicationId(), returnedApplication.getElderId());
            return buildJourneyResult(
                    returnedApplication.getApplicationId(),
                    returnedApplication.getElderId(),
                    agreement == null ? null : agreement.getId(),
                    targetState,
                    serviceJourneyStateMachine.getDefaultMessage(targetState));
        }

        if (agreement == null || agreement.getId() == null) {
            throw new IllegalStateException("当前旅程不存在可退回的服务协议");
        }

        ServiceAgreementDTO returnedAgreement = contractService.revertToDraftAgreement(agreement.getId(), reason);
        if (!serviceJourneyTransitionLogService.hasTransition(applicationId, transition.event(), targetState)) {
            serviceJourneyTransitionLogService.logTransition(
                    applicationId,
                    returnedAgreement.getAgreementId(),
                    application.getElderId(),
                    transition.fromState(),
                    transition.event(),
                    targetState,
                    reason,
                    returnedAgreement);
        }
        serviceJourneyTaskService.cancelOpenTask(applicationId, ServiceJourneyTaskService.TASK_TYPE_HEALTH_ASSESSMENT);
        serviceJourneyTaskService.createHealthAssessmentTask(applicationId, application.getElderId());
        return buildJourneyResult(
                applicationId,
                application.getElderId(),
                returnedAgreement.getAgreementId(),
                targetState,
                serviceJourneyStateMachine.getDefaultMessage(targetState));
    }

    private ReviewContext loadReviewContextForUpdate(Long agreementId) {
        ServiceAgreementPo agreement = serviceAgreementRepository.findByIdForUpdate(agreementId)
                .orElseThrow(() -> new IllegalStateException("当前服务协议不存在"));
        ServiceReviewPo latestReview = serviceReviewRepository.findLatestByAgreementIdForUpdate(agreementId)
                .orElse(null);
        String latestReviewConclusion = latestReview == null ? null : latestReview.getReviewConclusion();
        ServiceJourneyState currentState = deriveReviewJourneyState(agreement, latestReviewConclusion);
        return new ReviewContext(agreement, latestReviewConclusion, currentState);
    }

    private ReviewDecision buildReviewDecision(Long agreementId, Long elderId, Integer satisfactionScore, String reviewComment) {
        ServiceReview pendingReview = new ServiceReview();
        pendingReview.setAgreementId(agreementId);
        pendingReview.setElderId(elderId);
        pendingReview.setSatisfactionScore(satisfactionScore);
        pendingReview.setReviewComment(reviewComment);
        pendingReview.review();
        String reviewConclusion = pendingReview.toDTO().getReviewConclusion();

        ServiceReviewDTO review = new ServiceReviewDTO();
        review.setAgreementId(agreementId);
        review.setElderId(elderId);
        review.setSatisfactionScore(satisfactionScore);
        review.setReviewComment(reviewComment);

        ServiceJourneyEvent reviewEvent = serviceJourneyStateMachine.resolveReviewEvent(reviewConclusion)
                .orElseThrow(() -> new IllegalArgumentException("未知的服务评价结论: " + reviewConclusion));
        ServiceJourneyState requestedState = serviceJourneyStateMachine.resolveReviewTargetState(reviewConclusion);
        return new ReviewDecision(review, reviewConclusion, reviewEvent, requestedState);
    }

    private ServiceJourneyResultDTO reviewAndFinalizeWithSideEffects(
            ServiceAgreementPo agreement,
            ServiceReviewDTO review,
            ServiceJourneyTransitionResult transition) {
        qualityService.reviewService(review);
        ServiceJourneyState finalState = transition.toState();
        String reviewConclusion = transition.event() == ServiceJourneyEvent.REVIEW_IMPROVE
                ? ServiceReview.REVIEW_CONCLUSION_IMPROVE
                : null;
        String message = serviceJourneyStateMachine.resolveReviewMessage(finalState, reviewConclusion);

        if (finalState == ServiceJourneyState.TERMINATED) {
            contractService.terminateAgreement(agreement.getId());
        } else if (finalState == ServiceJourneyState.RENEWED) {
            ServiceAgreementDTO renewAgreement = new ServiceAgreementDTO();
            renewAgreement.setExpiryDate(LocalDate.now().plusYears(1));
            contractService.renewAgreement(agreement.getId(), renewAgreement);
        }

        serviceJourneyTransitionLogService.logTransition(
                agreement.getApplicationId(),
                agreement.getId(),
                review.getElderId(),
                transition.fromState(),
                transition.event(),
                finalState,
                message,
                review);
        return buildJourneyResult(
                agreement.getApplicationId(),
                review.getElderId(),
                agreement.getId(),
                finalState,
                message);
    }

    private ServiceJourneyResultDTO continueTerminatedJourney(
            ServiceApplicationPo application,
            ServiceAgreementPo agreement,
            String healthAssessmentStatus) {
        ServiceJourneyEvent terminalEvent = resolveContinueTerminalEvent(application, healthAssessmentStatus);
        String terminalMessage = resolveContinueTerminalMessage(application, healthAssessmentStatus);
        ServiceJourneyState fromState = terminalEvent == ServiceJourneyEvent.HEALTH_REJECTED
                ? ServiceJourneyState.PENDING_HEALTH_ASSESSMENT
                : ServiceJourneyState.PENDING_ASSESSMENT;
        ServiceJourneyTransitionResult transition = transition(fromState, terminalEvent, terminalMessage);
        serviceJourneyTransitionPolicy.requireAuthority(transition.requiredAuthority());
        if (!serviceJourneyTransitionLogService.hasTransition(application.getId(), terminalEvent, ServiceJourneyState.TERMINATED)) {
            serviceJourneyTransitionLogService.logTransition(
                    application.getId(),
                    agreement == null ? null : agreement.getId(),
                    application.getElderId(),
                    transition.fromState(),
                    transition.event(),
                    transition.toState(),
                    terminalMessage,
                    application);
        }
        serviceJourneyTaskService.completeOpenTask(
                application.getId(),
                ServiceJourneyTaskService.TASK_TYPE_ADMISSION_ASSESSMENT);
        if (terminalEvent == ServiceJourneyEvent.HEALTH_REJECTED) {
            serviceJourneyTaskService.completeOpenTask(
                    application.getId(),
                    ServiceJourneyTaskService.TASK_TYPE_HEALTH_ASSESSMENT);
        }
        return buildJourneyResult(
                application.getId(),
                application.getElderId(),
                agreement == null ? null : agreement.getId(),
                ServiceJourneyState.TERMINATED,
                terminalMessage);
    }

    private ServiceJourneyResultDTO continueToPendingHealthAssessment(
            ServiceApplicationPo application,
            ServiceAgreementPo agreement,
            String healthAssessmentStatus) {
        ServiceJourneyTransitionResult transition = transition(
                ServiceJourneyState.PENDING_ASSESSMENT,
                ServiceJourneyEvent.ADMISSION_APPROVED,
                null);
        serviceJourneyTransitionPolicy.requireAuthority(transition.requiredAuthority());
        ServiceJourneyState targetState = transition.toState();
        String message = serviceJourneyStateMachine.getDefaultMessage(targetState);
        if (ServiceJourneyStateMachine.HEALTH_ASSESSMENT_PENDING.equals(healthAssessmentStatus)
                && !serviceJourneyTransitionLogService.hasTransition(application.getId(), ServiceJourneyEvent.ADMISSION_APPROVED, targetState)) {
            serviceJourneyTransitionLogService.logTransition(
                    application.getId(),
                    agreement == null ? null : agreement.getId(),
                    application.getElderId(),
                    transition.fromState(),
                    transition.event(),
                    transition.toState(),
                    message,
                    application);
        }
        serviceJourneyTaskService.completeOpenTask(
                application.getId(),
                ServiceJourneyTaskService.TASK_TYPE_ADMISSION_ASSESSMENT);
        serviceJourneyTaskService.createHealthAssessmentTask(
                application.getId(),
                application.getElderId());
        return buildJourneyResult(
                application.getId(),
                application.getElderId(),
                agreement == null ? null : agreement.getId(),
                targetState,
                message);
    }

    private ServiceJourneyResultDTO continueToInService(
            ServiceApplicationPo application,
            ServiceAgreementPo agreement,
            HealthAssessmentRecordPo preSignAssessment,
            ServiceJourneyState currentState) {
        if (currentState == ServiceJourneyState.PENDING_AGREEMENT) {
            ServiceJourneyTransitionResult healthApprovedTransition = transition(
                    ServiceJourneyState.PENDING_HEALTH_ASSESSMENT,
                    ServiceJourneyEvent.HEALTH_APPROVED,
                    null);
            serviceJourneyTransitionPolicy.requireAuthority(healthApprovedTransition.requiredAuthority());
            if (!serviceJourneyTransitionLogService.hasTransition(application.getId(), ServiceJourneyEvent.HEALTH_APPROVED, ServiceJourneyState.PENDING_AGREEMENT)) {
                serviceJourneyTransitionLogService.logTransition(
                        application.getId(),
                        agreement == null ? null : agreement.getId(),
                        application.getElderId(),
                        healthApprovedTransition.fromState(),
                        healthApprovedTransition.event(),
                        healthApprovedTransition.toState(),
                        "健康评估通过，进入待签约阶段",
                        preSignAssessment);
            }
        }
        serviceJourneyTaskService.completeOpenTask(
                application.getId(),
                ServiceJourneyTaskService.TASK_TYPE_HEALTH_ASSESSMENT);

        ServiceAgreementDTO activeAgreement = ensureActiveAgreement(application, agreement);
        CarePlanDTO createdCarePlan = ensureCarePlan(activeAgreement);
        HealthProfileDTO createdHealthProfile = ensureHealthProfile(activeAgreement);
        HealthAssessmentDTO assessedHealth = ensureIntakeAssessment(activeAgreement);

        if (!serviceJourneyTransitionLogService.hasTransition(application.getId(), ServiceJourneyEvent.AGREEMENT_SIGNED, ServiceJourneyState.IN_SERVICE)) {
            serviceJourneyTransitionLogService.logTransition(
                    application.getId(),
                    activeAgreement.getAgreementId(),
                    application.getElderId(),
                    ServiceJourneyState.PENDING_AGREEMENT,
                    ServiceJourneyEvent.AGREEMENT_SIGNED,
                    ServiceJourneyState.IN_SERVICE,
                    serviceJourneyStateMachine.getInServiceCreatedMessage(),
                    activeAgreement);
        }

        ServiceJourneyResultDTO result = new ServiceJourneyResultDTO();
        result.setApplicationId(application.getId());
        result.setElderId(application.getElderId());
        result.setAgreementId(activeAgreement.getAgreementId());
        result.setCarePlanId(createdCarePlan.getPlanId());
        result.setHealthProfileId(createdHealthProfile.getProfileId());
        result.setHealthAssessmentId(assessedHealth.getAssessmentId());
        result.setFinalStatus(serviceJourneyStateMachine.toExternalState(ServiceJourneyState.IN_SERVICE).name());
        result.setMessage(serviceJourneyStateMachine.getInServiceCreatedMessage());
        return result;
    }

    private ServiceAgreementDTO ensureActiveAgreement(ServiceApplicationPo application, ServiceAgreementPo agreement) {
        if (agreement != null && ServiceAgreement.STATUS_ACTIVE.equals(agreement.getStatus())) {
            return ServiceAgreement.fromPo(agreement).toDTO();
        }

        ServiceAgreementDTO agreementToSign;
        if (agreement == null) {
            ServiceAgreementDTO draftAgreement = new ServiceAgreementDTO();
            draftAgreement.setApplicationId(application.getId());
            draftAgreement.setElderId(application.getElderId());
            draftAgreement.setServiceScene(application.getServiceScene());
            agreementToSign = contractService.createDraftAgreement(draftAgreement);
        } else {
            agreementToSign = ServiceAgreement.fromPo(agreement).toDTO();
        }

        ServiceAgreementDTO signAgreement = new ServiceAgreementDTO();
        signAgreement.setAgreementId(agreementToSign.getAgreementId());
        signAgreement.setSignedBy(application.getApplicantName());
        signAgreement.setEffectiveDate(LocalDate.now());
        signAgreement.setExpiryDate(LocalDate.now().plusYears(1));
        return contractService.signAgreement(signAgreement);
    }

    private CarePlanDTO ensureCarePlan(ServiceAgreementDTO activeAgreement) {
        return carePlanRepository.findTopByAgreementIdOrderByPlanDateDescIdDesc(activeAgreement.getAgreementId())
                .map(CarePlan::fromPo)
                .map(CarePlan::toDTO)
                .orElseGet(() -> {
                    CarePlanDTO carePlan = new CarePlanDTO();
                    carePlan.setAgreementId(activeAgreement.getAgreementId());
                    carePlan.setElderId(activeAgreement.getElderId());
                    carePlan.setPlanName("标准护理计划");
                    carePlan.setServiceScene(activeAgreement.getServiceScene());
                    carePlan.setPersonalizationNote("根据服务场景提供个性化护理");
                    return careDeliveryService.createCarePlan(carePlan);
                });
    }

    private HealthProfileDTO ensureHealthProfile(ServiceAgreementDTO activeAgreement) {
        return healthProfileRepository
                .findTopByElderIdAndAgreementIdOrderByProfileDateDescIdDesc(activeAgreement.getElderId(), activeAgreement.getAgreementId())
                .map(HealthProfile::fromPo)
                .map(HealthProfile::toDTO)
                .orElseGet(() -> {
                    HealthProfileDTO healthProfile = new HealthProfileDTO();
                    healthProfile.setAgreementId(activeAgreement.getAgreementId());
                    healthProfile.setElderId(activeAgreement.getElderId());
                    healthProfile.setRiskLevel("MEDIUM");
                    return healthService.createHealthProfile(healthProfile);
                });
    }

    private HealthAssessmentDTO ensureIntakeAssessment(ServiceAgreementDTO activeAgreement) {
        return healthAssessmentRecordRepository
                .findTopByAgreementIdAndAssessmentTypeOrderByAssessedAtDescIdDesc(activeAgreement.getAgreementId(), ASSESSMENT_TYPE_INTAKE)
                .map(HealthAssessmentRecord::fromPo)
                .map(HealthAssessmentRecord::toDTO)
                .orElseGet(() -> {
                    HealthAssessmentDTO initialAssessment = new HealthAssessmentDTO();
                    initialAssessment.setElderId(activeAgreement.getElderId());
                    initialAssessment.setAgreementId(activeAgreement.getAgreementId());
                    initialAssessment.setAssessmentType(ASSESSMENT_TYPE_INTAKE);
                    initialAssessment.setConclusion("入服初评完成");
                    initialAssessment.setScore(75);
                    return healthService.performAssessment(initialAssessment);
                });
    }

    private ServiceJourneyEvent resolveContinueTerminalEvent(
            ServiceApplicationPo application,
            String healthAssessmentStatus) {
        if (ServiceApplication.STATUS_WITHDRAWN.equals(application.getStatus())) {
            return ServiceJourneyEvent.JOURNEY_WITHDRAWN;
        }
        if (ServiceApplication.STATUS_FAILED.equals(application.getStatus())) {
            return ServiceJourneyEvent.ADMISSION_REJECTED;
        }
        if (ServiceJourneyStateMachine.HEALTH_ASSESSMENT_FAILED.equals(healthAssessmentStatus)) {
            return ServiceJourneyEvent.HEALTH_REJECTED;
        }
        throw new IllegalStateException("当前旅程状态不允许继续推进");
    }

    private String resolveContinueTerminalMessage(
            ServiceApplicationPo application,
            String healthAssessmentStatus) {
        if (ServiceApplication.STATUS_WITHDRAWN.equals(application.getStatus())) {
            return serviceJourneyStateMachine.getWithdrawnMessage();
        }
        if (ServiceApplication.STATUS_FAILED.equals(application.getStatus())) {
            return serviceJourneyStateMachine.getAssessmentFailureMessage();
        }
        if (ServiceJourneyStateMachine.HEALTH_ASSESSMENT_FAILED.equals(healthAssessmentStatus)) {
            return serviceJourneyStateMachine.getHealthFailureMessage();
        }
        throw new IllegalStateException("当前旅程状态不允许继续推进");
    }

    private List<IntakeRecordDTO> toIntakeRecords(List<ServiceApplicationPo> applications) {
        List<IntakeRecordDTO> records = new ArrayList<>();

        for (ServiceApplicationPo application : applications) {
            IntakeRecordDTO record = new IntakeRecordDTO();
            record.setApplicationId(application.getId());
            record.setElderId(application.getElderId());
            record.setApplicantName(application.getApplicantName());
            record.setSubmittedAt(application.getSubmittedAt());
            record.setAdmissionStatus(application.getStatus());

            ServiceAgreementPo agreement = serviceAgreementRepository
                    .findTopByApplicationIdOrderByIdDesc(application.getId())
                    .orElse(null);
            JourneyQuerySummary summary = buildJourneyQuerySummary(application, agreement);

            record.setJourneyStatus(summary.rawState().name());
            record.setMessage(summary.intakeRecordMessage());

            records.add(record);
        }

        return records;
    }

    private String resolveHealthAssessmentStatus(Long applicationId) {
        if (applicationId == null) {
            return ServiceJourneyStateMachine.HEALTH_ASSESSMENT_PENDING;
        }

        HealthAssessmentRequestDTO assessment = healthClient.listAssessmentHistory().stream()
                .filter(item -> applicationId.equals(item.getApplicationId()))
                .findFirst()
                .orElse(null);
        if (assessment == null) {
            return ServiceJourneyStateMachine.HEALTH_ASSESSMENT_PENDING;
        }

        if (serviceJourneyTransitionLogService.hasReturnToHealthAfter(applicationId, assessment.getHealthAssessedAt())) {
            return ServiceJourneyStateMachine.HEALTH_ASSESSMENT_PENDING;
        }

        return assessment.getAssessmentStatus();
    }

    private JourneyContext loadJourneyContextForUpdate(Long applicationId) {
        ServiceApplicationPo application = serviceApplicationRepository.findByIdForUpdate(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("未找到服务申请"));
        ServiceAgreementPo agreement = serviceAgreementRepository.findLatestByApplicationIdForUpdate(applicationId)
                .orElse(null);
        return buildJourneyContext(application, agreement);
    }

    private JourneyContext buildJourneyContext(ServiceApplicationPo application, ServiceAgreementPo agreement) {
        HealthAssessmentRecordPo preSignAssessment = findLatestPreSignAssessment(application).orElse(null);
        String healthAssessmentStatus = resolveHealthAssessmentStatus(application, preSignAssessment);
        ServiceJourneyState currentState = deriveJourneyState(application, agreement, healthAssessmentStatus);
        return new JourneyContext(application, agreement, preSignAssessment, healthAssessmentStatus, currentState);
    }

    private ServiceJourneyState deriveJourneyState(ServiceApplicationPo application) {
        if (application == null) {
            return ServiceJourneyState.TERMINATED;
        }

        ServiceAgreementPo agreement = serviceAgreementRepository
                .findTopByApplicationIdOrderByIdDesc(application.getId())
                .orElse(null);
        return buildJourneyContext(application, agreement).currentState();
    }

    private ServiceJourneyState deriveJourneyState(
            ServiceApplicationPo application,
            ServiceAgreementPo agreement,
            String healthAssessmentStatus) {
        if (application == null) {
            return ServiceJourneyState.TERMINATED;
        }

        return serviceJourneyStateMachine.deriveCurrentState(
                buildJourneyFacts(application, agreement, healthAssessmentStatus));
    }

    private ServiceJourneyState deriveReviewJourneyState(ServiceAgreementPo agreement, String reviewConclusion) {
        return serviceJourneyStateMachine.deriveCurrentState(
                buildReviewJourneyFacts(agreement, reviewConclusion));
    }

    private ServiceJourneyFacts buildJourneyFacts(
            ServiceApplicationPo application,
            ServiceAgreementPo agreement,
            String healthAssessmentStatus) {
        return ServiceJourneyFacts.builder()
                .applicationStatus(application.getStatus())
                .healthAssessmentStatus(healthAssessmentStatus)
                .agreementStatus(agreement == null ? null : agreement.getStatus())
                .reviewConclusion(findLatestReviewConclusion(agreement == null ? null : agreement.getId()))
                .build();
    }

    private ServiceJourneyFacts buildReviewJourneyFacts(ServiceAgreementPo agreement, String reviewConclusion) {
        return ServiceJourneyFacts.builder()
                .applicationStatus(ServiceApplication.STATUS_PASSED)
                .agreementStatus(agreement == null ? null : agreement.getStatus())
                .reviewConclusion(reviewConclusion)
                .build();
    }

    private Optional<HealthAssessmentRecordPo> findLatestPreSignAssessment(ServiceApplicationPo application) {
        if (application == null) {
            return Optional.empty();
        }
        return healthAssessmentRecordRepository.findTopByApplicationIdAndAssessmentTypeInOrderByAssessedAtDescIdDesc(
                application.getId(),
                List.of(ASSESSMENT_TYPE_PRE_SIGN_PASS, ASSESSMENT_TYPE_PRE_SIGN_FAIL));
    }

    private String resolveHealthAssessmentStatus(ServiceApplicationPo application, HealthAssessmentRecordPo assessment) {
        if (application == null || assessment == null) {
            return ServiceJourneyStateMachine.HEALTH_ASSESSMENT_PENDING;
        }
        if (serviceJourneyTransitionLogService.hasReturnToHealthAfter(application.getId(), assessment.getAssessedAt())) {
            return ServiceJourneyStateMachine.HEALTH_ASSESSMENT_PENDING;
        }
        return ASSESSMENT_TYPE_PRE_SIGN_PASS.equals(assessment.getAssessmentType())
                ? ServiceJourneyStateMachine.HEALTH_ASSESSMENT_PASSED
                : ServiceJourneyStateMachine.HEALTH_ASSESSMENT_FAILED;
    }

    private ServiceJourneyTransitionResult transition(
            ServiceJourneyState currentState,
            ServiceJourneyEvent event,
            String reason) {
        try {
            return serviceJourneyStateMachine.transition(
                    currentState,
                    event,
                    new ServiceJourneyTransitionContext(reason));
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("当前旅程状态不允许执行该操作", exception);
        }
    }

    private ServiceJourneyResultDTO buildJourneyResult(
            Long applicationId,
            Long elderId,
            Long agreementId,
            ServiceJourneyState state,
            String message) {
        ServiceJourneyResultDTO result = new ServiceJourneyResultDTO();
        result.setApplicationId(applicationId);
        result.setElderId(elderId);
        result.setAgreementId(agreementId);
        result.setFinalStatus(serviceJourneyStateMachine.toExternalState(state).name());
        result.setMessage(message);
        return result;
    }

    private JourneyQuerySummary buildJourneyQuerySummary(ServiceApplicationPo application, ServiceAgreementPo agreement) {
        String healthAssessmentStatus = resolveHealthAssessmentStatus(application.getId());
        ServiceJourneyState rawState = deriveJourneyState(application, agreement, healthAssessmentStatus);
        ServiceJourneyState externalState = serviceJourneyStateMachine.toExternalState(rawState);
        String agreementStatus = agreement == null ? null : agreement.getStatus();
        return new JourneyQuerySummary(
                rawState,
                externalState,
                serviceJourneyStateMachine.resolveJourneyMessage(rawState, application.getStatus(), agreementStatus),
                serviceJourneyStateMachine.resolveIntakeRecordMessage(rawState, application.getStatus(), agreementStatus));
    }

    private String findLatestReviewConclusion(Long agreementId) {
        if (agreementId == null) {
            return null;
        }

        return serviceReviewRepository.findTopByAgreementIdOrderByReviewedAtDescIdDesc(agreementId)
                .map(ServiceReviewPo::getReviewConclusion)
                .orElse(null);
    }

    private CareAnalyticsOverviewDTO.StagePoint stage(String name, int value) {
        CareAnalyticsOverviewDTO.StagePoint point = new CareAnalyticsOverviewDTO.StagePoint();
        point.setName(name);
        point.setValue(value);
        return point;
    }

    private Map<String, Integer> buildDateMap(LocalDate startDate, int days) {
        Map<String, Integer> map = new LinkedHashMap<>();
        DateTimeFormatter keyFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            map.put(date.format(keyFormatter), 0);
        }
        return map;
    }

    private void fillMapFromRows(Map<String, Integer> map, List<Object[]> rows) {
        for (Object[] row : rows) {
            String date = (String) row[0];
            Number count = (Number) row[1];
            if (map.containsKey(date)) {
                map.put(date, count.intValue());
            }
        }
    }

    private List<CareAnalyticsTrendsDTO.TrendPoint> toTrendPoints(Map<String, Integer> map) {
        List<CareAnalyticsTrendsDTO.TrendPoint> points = new ArrayList<>();
        DateTimeFormatter keyFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("M/d");
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            CareAnalyticsTrendsDTO.TrendPoint point = new CareAnalyticsTrendsDTO.TrendPoint();
            point.setLabel(LocalDate.parse(entry.getKey(), keyFormatter).format(labelFormatter));
            point.setValue(entry.getValue());
            points.add(point);
        }
        return points;
    }

    private record JourneyContext(
            ServiceApplicationPo application,
            ServiceAgreementPo agreement,
            HealthAssessmentRecordPo preSignAssessment,
            String healthAssessmentStatus,
            ServiceJourneyState currentState) {
    }

    private record ReviewContext(
            ServiceAgreementPo agreement,
            String latestReviewConclusion,
            ServiceJourneyState currentState) {
    }

    private record ReviewDecision(
            ServiceReviewDTO review,
            String reviewConclusion,
            ServiceJourneyEvent reviewEvent,
            ServiceJourneyState requestedState) {
    }

    private record JourneyQuerySummary(
            ServiceJourneyState rawState,
            ServiceJourneyState externalState,
            String journeyMessage,
            String intakeRecordMessage) {
    }
}
