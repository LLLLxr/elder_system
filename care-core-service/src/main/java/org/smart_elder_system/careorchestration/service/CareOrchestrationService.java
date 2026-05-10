package org.smart_elder_system.careorchestration.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.smart_elder_system.admission.vo.ServiceApplication;
import org.smart_elder_system.admission.po.ServiceApplicationPo;
import org.smart_elder_system.admission.repository.ServiceApplicationRepository;
import org.smart_elder_system.admission.service.AdmissionService;
import org.smart_elder_system.caredelivery.vo.CarePlan;
import org.smart_elder_system.caredelivery.repository.CarePlanRepository;
import org.smart_elder_system.caredelivery.service.CareDeliveryService;
import org.smart_elder_system.careorchestration.dto.CareAnalyticsOverviewDto;
import org.smart_elder_system.careorchestration.dto.CareAnalyticsTrendsDto;
import org.smart_elder_system.careorchestration.dto.ServiceJourneyTaskItemDto;
import org.smart_elder_system.careorchestration.dto.ServiceJourneyTaskOverviewDto;
import org.smart_elder_system.careorchestration.dto.ServiceJourneyTransitionLogItemDto;
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
import org.smart_elder_system.common.dto.caredelivery.CarePlanDto;
import org.smart_elder_system.common.dto.health.HealthAssessmentDto;
import org.smart_elder_system.common.dto.health.HealthAssessmentRequestDto;
import org.smart_elder_system.common.dto.health.HealthAssessmentSubmitDto;
import org.smart_elder_system.common.dto.health.HealthProfileDto;
import org.smart_elder_system.common.dto.admission.IntakeRecordDto;
import org.smart_elder_system.common.dto.contract.RenewalContextDto;
import org.smart_elder_system.common.dto.contract.ServiceAgreementDto;
import org.smart_elder_system.common.dto.admission.ServiceApplicationDto;
import org.smart_elder_system.common.dto.careorchestration.ServiceJourneyResultDto;
import org.smart_elder_system.common.dto.quality.ServiceReviewDto;
import org.smart_elder_system.common.dto.admission.EligibilityAssessmentDto;
import org.smart_elder_system.contract.vo.ServiceAgreement;
import org.smart_elder_system.contract.po.ServiceAgreementPo;
import org.smart_elder_system.contract.repository.ServiceAgreementRepository;
import org.smart_elder_system.contract.service.ContractService;
import org.smart_elder_system.health.vo.HealthAssessmentRecord;
import org.smart_elder_system.health.vo.HealthProfile;
import org.smart_elder_system.health.po.HealthAssessmentRecordPo;
import org.smart_elder_system.health.repository.HealthAssessmentRecordRepository;
import org.smart_elder_system.health.repository.HealthProfileRepository;
import org.smart_elder_system.health.service.HealthService;
import org.smart_elder_system.quality.vo.ServiceReview;
import org.smart_elder_system.quality.po.ServiceReviewPo;
import org.smart_elder_system.quality.repository.ServiceReviewRepository;
import org.smart_elder_system.quality.service.QualityService;

import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
    public ServiceJourneyResultDto startServiceJourney(
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

        ServiceApplicationDto application = new ServiceApplicationDto();
        application.setElderId(elderId);
        application.setGuardianId(guardianId);
        application.setApplicantName(applicantName);
        application.setContactPhone(contactPhone);
        application.setServiceScene(serviceScene);
        application.setServiceRequest(serviceRequest);

        ServiceApplicationDto submittedApplication = admissionService.submitApplication(application);

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
    public ServiceJourneyResultDto continueAfterAssessment(Long applicationId) {
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
            case PENDING_AGREEMENT -> continueToPendingAgreement(
                    context.application(),
                    context.agreement(),
                    context.preSignAssessment());
            case IN_SERVICE -> continueToInService(
                    context.application(),
                    context.agreement());
            default -> throw new IllegalStateException("当前旅程状态不允许继续推进");
        };
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceJourneyResultDto rejectAdmissionJourney(Long applicationId, String assessmentConclusion, String assessor) {
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

        EligibilityAssessmentDto assessment = new EligibilityAssessmentDto();
        assessment.setApplicationId(applicationId);
        assessment.setEligible(false);
        assessment.setAssessmentConclusion(assessmentConclusion);
        assessment.setAssessor(assessor);
        ServiceApplicationDto rejectedApplication = admissionService.assessEligibility(assessment);

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
    public ServiceJourneyResultDto rejectHealthJourney(
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
    public ServiceJourneyResultDto withdrawServiceJourney(Long applicationId, String reason) {
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
    public ServiceJourneyResultDto reviewAndFinalize(Long agreementId, Long elderId, Integer satisfactionScore, String reviewComment) {
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

    public Page<ServiceJourneyTaskItemDto> listJourneyTasks(
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

    public List<ServiceJourneyTaskItemDto> listJourneyTaskTimeline(Long applicationId) {
        return serviceJourneyTaskService.listTaskTimeline(applicationId);
    }

    public ServiceJourneyTaskOverviewDto getJourneyTaskOverview(
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

    public List<ServiceJourneyTransitionLogItemDto> listJourneyTransitionLogsByApplication(Long applicationId) {
        return serviceJourneyTransitionLogService.listByApplicationId(applicationId);
    }

    public List<ServiceJourneyTransitionLogItemDto> listJourneyTransitionLogsByAgreement(Long agreementId) {
        return serviceJourneyTransitionLogService.listByAgreementId(agreementId);
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceJourneyResultDto returnJourneyStep(Long applicationId, ServiceJourneyState targetState, String reason) {
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

    public List<IntakeRecordDto> listIntakeRecords(Long elderId) {
        List<ServiceApplicationPo> applications = serviceApplicationRepository.findByElderIdOrderBySubmittedAtDesc(elderId);
        return toIntakeRecords(applications);
    }

    public List<IntakeRecordDto> listIntakeRecordsByApplicant(String applicantName) {
        List<ServiceApplicationPo> applications = serviceApplicationRepository.findByApplicantNameOrderBySubmittedAtDesc(applicantName);
        return toIntakeRecords(applications);
    }

    public ServiceJourneyResultDto getLatestJourneyResultByApplicant(String applicantName) {
        Optional<ServiceApplicationPo> latestApplication = serviceApplicationRepository
                .findTopByApplicantNameOrderBySubmittedAtDescIdDesc(applicantName);

        if (latestApplication.isEmpty()) {
            return new ServiceJourneyResultDto();
        }

        ServiceApplicationPo application = latestApplication.get();
        ServiceAgreementPo agreement = serviceAgreementRepository
                .findTopByApplicationIdOrderByIdDesc(application.getId())
                .orElse(null);
        JourneyQuerySummary summary = buildJourneyQuerySummary(application, agreement);

        ServiceJourneyResultDto result = new ServiceJourneyResultDto();
        result.setApplicationId(application.getId());
        result.setElderId(application.getElderId());
        if (agreement != null) {
            result.setAgreementId(agreement.getId());
        }
        result.setFinalStatus(summary.externalState().name());
        result.setMessage(summary.journeyMessage());
        return result;
    }

    public RenewalContextDto getLatestRenewalContextByApplicant(String applicantName) {
        Optional<ServiceApplicationPo> latestApplication = serviceApplicationRepository
                .findTopByApplicantNameOrderBySubmittedAtDescIdDesc(applicantName);
        if (latestApplication.isEmpty()) {
            return RenewalContextDto.builder()
                    .message("当前登录用户名下暂无申请记录")
                    .build();
        }

        ServiceApplicationPo application = latestApplication.get();
        ServiceAgreementPo agreement = serviceAgreementRepository
                .findTopByApplicationIdOrderByIdDesc(application.getId())
                .orElse(null);
        if (agreement == null) {
            return RenewalContextDto.builder()
                    .applicationId(application.getId())
                    .elderId(application.getElderId())
                    .message("当前申请暂无服务协议")
                    .build();
        }

        ServiceReviewPo latestReview = serviceReviewRepository
                .findTopByAgreementIdOrderByReviewedAtDescIdDesc(agreement.getId())
                .orElse(null);
        return buildRenewalContext(agreement, latestReview);
    }

    @Transactional(rollbackFor = Exception.class)
    public RenewalContextDto submitRenewalReview(Long agreementId, Long elderId, Integer satisfactionScore, String reviewComment) {
        RenewalOperationContext context = loadRenewalOperationContextForUpdate(agreementId);
        validateRenewalAgreement(context.agreement());
        if (hasCurrentCycleReview(context.agreement(), context.latestReview())) {
            return buildRenewalContext(context.agreement(), context.latestReview());
        }

        ServiceReviewDto review = new ServiceReviewDto();
        review.setAgreementId(agreementId);
        review.setElderId(elderId);
        review.setSatisfactionScore(satisfactionScore);
        review.setReviewComment(reviewComment);

        ServiceReviewDto savedReview = qualityService.reviewService(review);
        return buildRenewalContext(context.agreement(), ServiceReview.fromDto(savedReview).toPo());
    }

    @Transactional(rollbackFor = Exception.class)
    public RenewalContextDto confirmRenewal(Long agreementId, Integer renewMonths) {
        RenewalOperationContext context = loadRenewalOperationContextForUpdate(agreementId);
        validateRenewalAgreement(context.agreement());
        if (renewMonths == null || renewMonths < 1 || renewMonths > 12) {
            throw new IllegalArgumentException("续约月数必须在1到12个月之间");
        }

        serviceJourneyTransitionPolicy.requireAuthority("journey:review:renew");
        ServiceAgreementDto renewalRequest = new ServiceAgreementDto();
        renewalRequest.setExpiryDate(context.agreement().getExpiryDate().plusMonths(renewMonths));
        ServiceAgreementDto renewedAgreement = contractService.renewAgreement(agreementId, renewalRequest);
        RenewalContextDto result = buildRenewalContext(ServiceAgreement.fromDto(renewedAgreement).toPo(), context.latestReview());
        result.setRenewalStage("RENEWED");
        result.setReviewSubmitted(false);
        result.setCanReview(false);
        result.setCanRenew(false);
        result.setCanTerminate(false);
        result.setMessage("已续约" + renewMonths + "个月，新的服务周期已生效");
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public RenewalContextDto declineRenewal(Long agreementId, String reason) {
        RenewalOperationContext context = loadRenewalOperationContextForUpdate(agreementId);
        validateRenewalAgreement(context.agreement());

        serviceJourneyTransitionPolicy.requireAuthority("journey:review:terminate");
        ServiceAgreementDto terminatedAgreement = contractService.terminateAgreement(agreementId);
        RenewalContextDto result = buildRenewalContext(ServiceAgreement.fromDto(terminatedAgreement).toPo(), context.latestReview());
        result.setRenewalStage("TERMINATED");
        result.setReviewSubmitted(true);
        result.setCanReview(false);
        result.setCanRenew(false);
        result.setCanTerminate(false);
        result.setMessage(reason == null || reason.isBlank() ? "已结束本期服务，不再续约" : reason);
        return result;
    }

    public CareAnalyticsOverviewDto getAnalyticsOverview() {
        int applicationsTotal = (int) serviceApplicationRepository.count();
        int agreementsActive = (int) serviceAgreementRepository.countByStatus(ServiceAgreement.STATUS_ACTIVE);
        int plansInProgress = (int) carePlanRepository.countByStatus(CarePlan.STATUS_IN_PROGRESS);

        Double avg = serviceReviewRepository.averageSatisfaction();
        int averageSatisfaction = avg == null ? 0 : (int) Math.round(avg);

        List<CareAnalyticsOverviewDto.StagePoint> stages = new ArrayList<>();
        stages.add(stage("申请受理", applicationsTotal));
        stages.add(stage("健康评估", (int) serviceApplicationRepository.countByStatus(ServiceApplication.STATUS_ASSESSED)));
        stages.add(stage("签约", (int) serviceAgreementRepository.countByStatus(ServiceAgreement.STATUS_ACTIVE)));
        stages.add(stage("照护执行", (int) carePlanRepository.countByStatus(CarePlan.STATUS_IN_PROGRESS)));
        stages.add(stage("质量回访", (int) serviceReviewRepository.count()));

        CareAnalyticsOverviewDto dto = new CareAnalyticsOverviewDto();
        dto.setApplicationsTotal(applicationsTotal);
        dto.setAgreementsActive(agreementsActive);
        dto.setPlansInProgress(plansInProgress);
        dto.setAverageSatisfaction(averageSatisfaction);
        dto.setStageDistribution(stages);
        return dto;
    }

    public CareAnalyticsTrendsDto getAnalyticsTrends(Integer days) {
        int actualDays = days == null ? 30 : Math.max(7, Math.min(days, 90));
        LocalDate startDate = LocalDate.now().minusDays(actualDays - 1L);
        LocalDateTime startTime = startDate.atStartOfDay();

        Map<String, Integer> applicationMap = buildDateMap(startDate, actualDays);
        Map<String, Integer> agreementMap = buildDateMap(startDate, actualDays);
        Map<String, Integer> reviewMap = buildDateMap(startDate, actualDays);

        fillMapFromRows(applicationMap, serviceApplicationRepository.countSubmittedDaily(startTime));
        fillMapFromRows(agreementMap, serviceAgreementRepository.countEffectiveDaily(startDate));
        fillMapFromRows(reviewMap, serviceReviewRepository.countReviewedDaily(startTime));

        CareAnalyticsTrendsDto dto = new CareAnalyticsTrendsDto();
        dto.setApplicationTrend(toTrendPoints(applicationMap));
        dto.setAgreementTrend(toTrendPoints(agreementMap));
        dto.setReviewTrend(toTrendPoints(reviewMap));
        return dto;
    }

    private ServiceJourneyResultDto rejectHealthJourneyWithSideEffects(
            ServiceApplicationPo application,
            ServiceAgreementPo agreement,
            ServiceJourneyTransitionResult transition,
            Long applicationId,
            String assessmentConclusion,
            String assessor,
            String responsibleDoctor,
            Integer score) {
        HealthAssessmentSubmitDto submitDto = new HealthAssessmentSubmitDto();
        submitDto.setApplicationId(applicationId);
        submitDto.setPassed(false);
        submitDto.setAssessmentConclusion(assessmentConclusion);
        submitDto.setAssessor(assessor);
        submitDto.setResponsibleDoctor(responsibleDoctor);
        submitDto.setScore(score);
        HealthAssessmentRequestDto rejectedHealthAssessment = healthService.submitPreSignAssessment(submitDto);

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

    private ServiceJourneyResultDto withdrawServiceJourneyWithSideEffects(
            ServiceApplicationPo application,
            ServiceAgreementPo agreement,
            ServiceJourneyTransitionResult transition,
            Long applicationId,
            String reason) {
        ServiceApplicationDto withdrawnApplication = admissionService.withdrawApplication(applicationId, reason);
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

    private ServiceJourneyResultDto returnJourneyStepWithSideEffects(
            ServiceApplicationPo application,
            ServiceAgreementPo agreement,
            ServiceJourneyTransitionResult transition,
            Long applicationId,
            String reason) {
        ServiceJourneyState targetState = transition.toState();
        if (transition.event() == ServiceJourneyEvent.RETURN_TO_ASSESSMENT) {
            ServiceApplicationDto returnedApplication = admissionService.revertToAssessment(applicationId, reason);
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

        ServiceAgreementDto returnedAgreement = contractService.revertToDraftAgreement(agreement.getId(), reason);
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
        String reviewConclusion = pendingReview.toDto().getReviewConclusion();

        ServiceReviewDto review = new ServiceReviewDto();
        review.setAgreementId(agreementId);
        review.setElderId(elderId);
        review.setSatisfactionScore(satisfactionScore);
        review.setReviewComment(reviewComment);

        ServiceJourneyEvent reviewEvent = serviceJourneyStateMachine.resolveReviewEvent(reviewConclusion)
                .orElseThrow(() -> new IllegalArgumentException("未知的服务评价结论: " + reviewConclusion));
        ServiceJourneyState requestedState = serviceJourneyStateMachine.resolveReviewTargetState(reviewConclusion);
        return new ReviewDecision(review, reviewConclusion, reviewEvent, requestedState);
    }

    private ServiceJourneyResultDto reviewAndFinalizeWithSideEffects(
            ServiceAgreementPo agreement,
            ServiceReviewDto review,
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
            ServiceAgreementDto renewAgreement = new ServiceAgreementDto();
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

    private ServiceJourneyResultDto continueTerminatedJourney(
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

    private ServiceJourneyResultDto continueToPendingHealthAssessment(
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

    private ServiceJourneyResultDto continueToPendingAgreement(
            ServiceApplicationPo application,
            ServiceAgreementPo agreement,
            HealthAssessmentRecordPo preSignAssessment) {
        ServiceJourneyTransitionResult transition = transition(
                ServiceJourneyState.PENDING_HEALTH_ASSESSMENT,
                ServiceJourneyEvent.HEALTH_APPROVED,
                null);
        serviceJourneyTransitionPolicy.requireAuthority(transition.requiredAuthority());
        serviceJourneyTaskService.completeOpenTask(
                application.getId(),
                ServiceJourneyTaskService.TASK_TYPE_HEALTH_ASSESSMENT);

        ServiceAgreementDto draftAgreement = ensureDraftAgreement(application, agreement);
        if (!serviceJourneyTransitionLogService.hasTransition(application.getId(), transition.event(), transition.toState())) {
            serviceJourneyTransitionLogService.logTransition(
                    application.getId(),
                    draftAgreement.getAgreementId(),
                    application.getElderId(),
                    transition.fromState(),
                    transition.event(),
                    transition.toState(),
                    "健康评估通过，进入待签约阶段",
                    preSignAssessment);
        }

        return buildJourneyResult(
                application.getId(),
                application.getElderId(),
                draftAgreement.getAgreementId(),
                ServiceJourneyState.PENDING_AGREEMENT,
                serviceJourneyStateMachine.getDefaultMessage(ServiceJourneyState.PENDING_AGREEMENT));
    }

    private ServiceJourneyResultDto continueToInService(
            ServiceApplicationPo application,
            ServiceAgreementPo agreement) {
        ServiceAgreementDto activeAgreement = ensureActiveAgreement(application, agreement);
        CarePlanDto createdCarePlan = ensureCarePlan(activeAgreement);
        HealthProfileDto createdHealthProfile = ensureHealthProfile(activeAgreement);
        HealthAssessmentDto assessedHealth = ensureIntakeAssessment(activeAgreement);

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

        ServiceJourneyResultDto result = new ServiceJourneyResultDto();
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

    private ServiceAgreementDto ensureDraftAgreement(ServiceApplicationPo application, ServiceAgreementPo agreement) {
        if (agreement != null) {
            return ServiceAgreement.fromPo(agreement).toDto();
        }

        ServiceAgreementDto draftAgreement = new ServiceAgreementDto();
        draftAgreement.setApplicationId(application.getId());
        draftAgreement.setElderId(application.getElderId());
        draftAgreement.setServiceScene(application.getServiceScene());
        return contractService.createDraftAgreement(draftAgreement);
    }

    private ServiceAgreementDto ensureActiveAgreement(ServiceApplicationPo application, ServiceAgreementPo agreement) {
        if (agreement != null && ServiceAgreement.STATUS_ACTIVE.equals(agreement.getStatus())) {
            return ServiceAgreement.fromPo(agreement).toDto();
        }

        ServiceAgreementDto agreementToSign = ensureDraftAgreement(application, agreement);
        ServiceAgreementDto signAgreement = new ServiceAgreementDto();
        signAgreement.setAgreementId(agreementToSign.getAgreementId());
        signAgreement.setSignedBy(application.getApplicantName());
        signAgreement.setEffectiveDate(LocalDate.now());
        signAgreement.setExpiryDate(LocalDate.now().plusYears(1));
        return contractService.signAgreement(signAgreement);
    }

    private CarePlanDto ensureCarePlan(ServiceAgreementDto activeAgreement) {
        return carePlanRepository.findTopByAgreementIdOrderByPlanDateDescIdDesc(activeAgreement.getAgreementId())
                .map(CarePlan::fromPo)
                .map(CarePlan::toDto)
                .orElseGet(() -> {
                    CarePlanDto carePlan = new CarePlanDto();
                    carePlan.setAgreementId(activeAgreement.getAgreementId());
                    carePlan.setElderId(activeAgreement.getElderId());
                    carePlan.setPlanName("标准护理计划");
                    carePlan.setServiceScene(activeAgreement.getServiceScene());
                    carePlan.setPersonalizationNote("根据服务场景提供个性化护理");
                    return careDeliveryService.createCarePlan(carePlan);
                });
    }

    private HealthProfileDto ensureHealthProfile(ServiceAgreementDto activeAgreement) {
        return healthProfileRepository
                .findTopByElderIdAndAgreementIdOrderByProfileDateDescIdDesc(activeAgreement.getElderId(), activeAgreement.getAgreementId())
                .map(HealthProfile::fromPo)
                .map(HealthProfile::toDto)
                .orElseGet(() -> {
                    HealthProfileDto healthProfile = new HealthProfileDto();
                    healthProfile.setAgreementId(activeAgreement.getAgreementId());
                    healthProfile.setElderId(activeAgreement.getElderId());
                    healthProfile.setRiskLevel("MEDIUM");
                    return healthService.createHealthProfile(healthProfile);
                });
    }

    private HealthAssessmentDto ensureIntakeAssessment(ServiceAgreementDto activeAgreement) {
        return healthAssessmentRecordRepository
                .findTopByAgreementIdAndAssessmentTypeOrderByAssessedAtDescIdDesc(activeAgreement.getAgreementId(), ASSESSMENT_TYPE_INTAKE)
                .map(HealthAssessmentRecord::fromPo)
                .map(HealthAssessmentRecord::toDto)
                .orElseGet(() -> {
                    HealthAssessmentDto initialAssessment = new HealthAssessmentDto();
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

    private List<IntakeRecordDto> toIntakeRecords(List<ServiceApplicationPo> applications) {
        List<IntakeRecordDto> records = new ArrayList<>();

        for (ServiceApplicationPo application : applications) {
            IntakeRecordDto record = new IntakeRecordDto();
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

        HealthAssessmentRequestDto assessment = healthClient.listAssessmentHistory().stream()
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

    private ServiceJourneyResultDto buildJourneyResult(
            Long applicationId,
            Long elderId,
            Long agreementId,
            ServiceJourneyState state,
            String message) {
        ServiceJourneyResultDto result = new ServiceJourneyResultDto();
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

    private RenewalOperationContext loadRenewalOperationContextForUpdate(Long agreementId) {
        ServiceAgreementPo agreement = serviceAgreementRepository.findByIdForUpdate(agreementId)
                .orElseThrow(() -> new IllegalArgumentException("当前服务协议不存在"));
        ServiceReviewPo latestReview = serviceReviewRepository.findLatestByAgreementIdForUpdate(agreementId)
                .orElse(null);
        return new RenewalOperationContext(agreement, latestReview);
    }

    private void validateRenewalAgreement(ServiceAgreementPo agreement) {
        if (agreement == null) {
            throw new IllegalArgumentException("当前服务协议不存在");
        }
        if (!ServiceAgreement.STATUS_ACTIVE.equals(agreement.getStatus())) {
            throw new IllegalStateException("当前协议状态不支持续约操作");
        }
        if (agreement.getExpiryDate() == null) {
            throw new IllegalStateException("当前协议缺少到期日，无法办理续约");
        }
    }

    private ServiceReviewPo requireCurrentCycleReview(ServiceAgreementPo agreement, ServiceReviewPo latestReview) {
        if (!hasCurrentCycleReview(agreement, latestReview)) {
            throw new IllegalStateException("当前周期尚未提交满意度评价");
        }
        return latestReview;
    }

    private boolean hasCurrentCycleReview(ServiceAgreementPo agreement, ServiceReviewPo latestReview) {
        if (agreement == null || agreement.getExpiryDate() == null || latestReview == null || latestReview.getReviewedAt() == null) {
            return false;
        }
        LocalDate reviewDate = latestReview.getReviewedAt().toLocalDate();
        return !reviewDate.isAfter(agreement.getExpiryDate())
                && !reviewDate.isBefore(agreement.getExpiryDate().minusMonths(1));
    }

    private RenewalContextDto buildRenewalContext(ServiceAgreementPo agreement, ServiceReviewPo latestReview) {
        LocalDate today = LocalDate.now();
        LocalDate expiryDate = agreement.getExpiryDate();
        long daysUntilExpiry = expiryDate == null ? 0L : ChronoUnit.DAYS.between(today, expiryDate);
        boolean reviewSubmitted = hasCurrentCycleReview(agreement, latestReview);
        String reviewConclusion = latestReview == null ? null : latestReview.getReviewConclusion();
        String renewalStage = resolveRenewalStage(agreement, latestReview, reviewSubmitted, daysUntilExpiry);

        return RenewalContextDto.builder()
                .agreementId(agreement.getId())
                .applicationId(agreement.getApplicationId())
                .elderId(agreement.getElderId())
                .agreementStatus(agreement.getStatus())
                .effectiveDate(agreement.getEffectiveDate())
                .expiryDate(expiryDate)
                .daysUntilExpiry(daysUntilExpiry)
                .renewalStage(renewalStage)
                .latestReviewScore(latestReview == null ? null : latestReview.getSatisfactionScore())
                .latestReviewConclusion(reviewConclusion)
                .reviewSubmitted(reviewSubmitted)
                .canReview(ServiceAgreement.STATUS_ACTIVE.equals(agreement.getStatus()) && !reviewSubmitted)
                .canRenew(ServiceAgreement.STATUS_ACTIVE.equals(agreement.getStatus()))
                .canTerminate(ServiceAgreement.STATUS_ACTIVE.equals(agreement.getStatus()))
                .suggestedNextExpiryDate(expiryDate == null ? null : expiryDate.plusMonths(1))
                .message(resolveRenewalMessage(renewalStage, reviewConclusion, daysUntilExpiry))
                .build();
    }

    private String resolveRenewalStage(
            ServiceAgreementPo agreement,
            ServiceReviewPo latestReview,
            boolean reviewSubmitted,
            long daysUntilExpiry) {
        if (agreement == null) {
            return "NO_AGREEMENT";
        }
        if (ServiceAgreement.STATUS_TERMINATED.equals(agreement.getStatus())) {
            return "TERMINATED";
        }
        if (!ServiceAgreement.STATUS_ACTIVE.equals(agreement.getStatus())) {
            return "IN_SERVICE";
        }
        if (reviewSubmitted && latestReview != null) {
            return "PENDING_RENEWAL";
        }
        if (daysUntilExpiry <= 7) {
            return "UPCOMING_EXPIRY";
        }
        return "IN_SERVICE";
    }

    private String resolveRenewalMessage(String renewalStage, String reviewConclusion, long daysUntilExpiry) {
        return switch (renewalStage) {
            case "UPCOMING_EXPIRY" -> "协议将于" + daysUntilExpiry + "天内到期，可直接续约、结束本期服务，或先提交满意度评价";
            case "PENDING_RENEWAL" -> "当前可直接选择续约月数并办理下一服务周期、结束本期服务，或补充查看本周期评价";
            case "PENDING_REVIEW" -> "当前周期已完成评价，仍可直接续约或结束本期服务";
            case "TERMINATED" -> "当前服务已结束";
            default -> "当前服务执行中";
        };
    }

    private CareAnalyticsOverviewDto.StagePoint stage(String name, int value) {
        CareAnalyticsOverviewDto.StagePoint point = new CareAnalyticsOverviewDto.StagePoint();
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

    private List<CareAnalyticsTrendsDto.TrendPoint> toTrendPoints(Map<String, Integer> map) {
        List<CareAnalyticsTrendsDto.TrendPoint> points = new ArrayList<>();
        DateTimeFormatter keyFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("M/d");
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            CareAnalyticsTrendsDto.TrendPoint point = new CareAnalyticsTrendsDto.TrendPoint();
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

    private record RenewalOperationContext(
            ServiceAgreementPo agreement,
            ServiceReviewPo latestReview) {
    }

    private record ReviewDecision(
            ServiceReviewDto review,
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
