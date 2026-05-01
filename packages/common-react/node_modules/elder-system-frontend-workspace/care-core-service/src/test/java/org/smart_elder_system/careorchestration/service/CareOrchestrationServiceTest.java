package org.smart_elder_system.careorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.smart_elder_system.admission.po.ServiceApplicationPo;
import org.smart_elder_system.admission.repository.ServiceApplicationRepository;
import org.smart_elder_system.admission.service.AdmissionService;
import org.smart_elder_system.caredelivery.repository.CarePlanRepository;
import org.smart_elder_system.caredelivery.service.CareDeliveryService;
import org.smart_elder_system.careorchestration.feign.AdmissionClient;
import org.smart_elder_system.careorchestration.feign.CareDeliveryClient;
import org.smart_elder_system.careorchestration.feign.ContractClient;
import org.smart_elder_system.careorchestration.feign.HealthClient;
import org.smart_elder_system.careorchestration.feign.QualityClient;
import org.smart_elder_system.careorchestration.journey.ServiceJourneyEvent;
import org.smart_elder_system.careorchestration.journey.ServiceJourneyState;
import org.smart_elder_system.careorchestration.journey.ServiceJourneyStateMachine;
import org.smart_elder_system.careorchestration.security.JourneyAuthorizationException;
import org.smart_elder_system.careorchestration.security.ServiceJourneyTransitionPolicy;
import org.smart_elder_system.common.dto.care.CarePlanDTO;
import org.smart_elder_system.common.dto.care.HealthAssessmentDTO;
import org.smart_elder_system.common.dto.care.HealthAssessmentRequestDTO;
import org.smart_elder_system.common.dto.care.HealthProfileDTO;
import org.smart_elder_system.common.dto.care.ServiceAgreementDTO;
import org.smart_elder_system.common.dto.care.ServiceApplicationDTO;
import org.smart_elder_system.common.dto.care.ServiceJourneyResultDTO;
import org.smart_elder_system.common.dto.care.ServiceReviewDTO;
import org.smart_elder_system.contract.po.ServiceAgreementPo;
import org.smart_elder_system.contract.repository.ServiceAgreementRepository;
import org.smart_elder_system.contract.service.ContractService;
import org.smart_elder_system.health.po.HealthAssessmentRecordPo;
import org.smart_elder_system.health.repository.HealthAssessmentRecordRepository;
import org.smart_elder_system.health.repository.HealthProfileRepository;
import org.smart_elder_system.health.service.HealthService;
import org.smart_elder_system.quality.po.ServiceReviewPo;
import org.smart_elder_system.quality.repository.ServiceReviewRepository;
import org.smart_elder_system.quality.service.QualityService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CareOrchestrationServiceTest {

    @Mock
    private AdmissionClient admissionClient;
    @Mock
    private ContractClient contractClient;
    @Mock
    private CareDeliveryClient careDeliveryClient;
    @Mock
    private HealthClient healthClient;
    @Mock
    private QualityClient qualityClient;
    @Mock
    private AdmissionService admissionService;
    @Mock
    private ContractService contractService;
    @Mock
    private CareDeliveryService careDeliveryService;
    @Mock
    private HealthService healthService;
    @Mock
    private QualityService qualityService;
    @Mock
    private ServiceApplicationRepository serviceApplicationRepository;
    @Mock
    private ServiceAgreementRepository serviceAgreementRepository;
    @Mock
    private CarePlanRepository carePlanRepository;
    @Mock
    private HealthProfileRepository healthProfileRepository;
    @Mock
    private HealthAssessmentRecordRepository healthAssessmentRecordRepository;
    @Mock
    private ServiceReviewRepository serviceReviewRepository;
    @Mock
    private ServiceJourneyTransitionLogService serviceJourneyTransitionLogService;
    @Mock
    private ServiceJourneyTransitionPolicy serviceJourneyTransitionPolicy;
    @Mock
    private ServiceJourneyTaskService serviceJourneyTaskService;

    @Test
    void shouldStartServiceJourney() {
        ServiceApplicationDTO submitted = new ServiceApplicationDTO();
        submitted.setApplicationId(1001L);
        submitted.setElderId(3003L);
        submitted.setStatus(org.smart_elder_system.admission.model.ServiceApplication.STATUS_SUBMITTED);

        when(serviceApplicationRepository.findByElderIdForUpdate(3003L)).thenReturn(java.util.List.of());
        when(admissionService.submitApplication(any())).thenReturn(submitted);

        CareOrchestrationService service = newService();
        ServiceJourneyResultDTO result = service.startServiceJourney(
                3003L,
                4004L,
                "张三",
                "13800000000",
                "HOME",
                "助餐");

        assertEquals(1001L, result.getApplicationId());
        assertEquals(3003L, result.getElderId());
        assertEquals("PENDING_ASSESSMENT", result.getFinalStatus());
        assertEquals("申请已提交，待管理端完成需求评估", result.getMessage());
        verify(admissionService).submitApplication(any());
        verify(serviceJourneyTransitionLogService).logTransition(
                eq(1001L),
                isNull(),
                eq(3003L),
                isNull(),
                eq(ServiceJourneyEvent.APPLICATION_SUBMITTED),
                eq(ServiceJourneyState.PENDING_ASSESSMENT),
                eq("申请受理提交成功"),
                any(ServiceApplicationDTO.class)
        );
        verify(serviceJourneyTaskService).createAdmissionAssessmentTask(1001L, 3003L);
    }

    @Test
    void shouldRejectDuplicateOngoingJourneyWhenStarting() {
        ServiceApplicationPo existing = applicationPo(9001L, 3003L,
                org.smart_elder_system.admission.model.ServiceApplication.STATUS_SUBMITTED);
        when(serviceApplicationRepository.findByElderIdForUpdate(3003L)).thenReturn(java.util.List.of(existing));

        CareOrchestrationService service = newService();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.startServiceJourney(3003L, 4004L, "张三", "13800000000", "HOME", "助餐"));
        assertEquals("该老人存在进行中的受理记录，不可重复发起申请", exception.getMessage());
        verify(admissionService, never()).submitApplication(any());
    }

    @Test
    void shouldContinueToPendingHealthAssessmentAfterAdmissionApproval() {
        ServiceApplicationPo application = applicationPo(1001L, 3003L,
                org.smart_elder_system.admission.model.ServiceApplication.STATUS_PASSED);

        when(serviceApplicationRepository.findByIdForUpdate(1001L)).thenReturn(Optional.of(application));
        when(serviceAgreementRepository.findLatestByApplicationIdForUpdate(1001L)).thenReturn(Optional.empty());
        when(healthAssessmentRecordRepository.findTopByApplicationIdAndAssessmentTypeInOrderByAssessedAtDescIdDesc(eq(1001L), any()))
                .thenReturn(Optional.empty());

        CareOrchestrationService service = newService();
        ServiceJourneyResultDTO result = service.continueAfterAssessment(1001L);

        assertEquals("PENDING_HEALTH_ASSESSMENT", result.getFinalStatus());
        assertEquals("需求评估已通过，待完成健康评估后继续签约", result.getMessage());
        verify(serviceJourneyTransitionPolicy).requireAuthority("journey:assessment:approve");
        verify(serviceJourneyTransitionLogService).logTransition(
                eq(1001L),
                isNull(),
                eq(3003L),
                eq(ServiceJourneyState.PENDING_ASSESSMENT),
                eq(ServiceJourneyEvent.ADMISSION_APPROVED),
                eq(ServiceJourneyState.PENDING_HEALTH_ASSESSMENT),
                eq("需求评估已通过，待完成健康评估后继续签约"),
                any(ServiceApplicationPo.class)
        );
        verify(serviceJourneyTaskService).completeOpenTask(1001L, ServiceJourneyTaskService.TASK_TYPE_ADMISSION_ASSESSMENT);
        verify(serviceJourneyTaskService).createHealthAssessmentTask(1001L, 3003L);
    }

    @Test
    void shouldContinueTerminatedJourneyAfterWithdrawnAdmission() {
        ServiceApplicationPo application = applicationPo(1001L, 3003L,
                org.smart_elder_system.admission.model.ServiceApplication.STATUS_WITHDRAWN);

        when(serviceApplicationRepository.findByIdForUpdate(1001L)).thenReturn(Optional.of(application));
        when(serviceAgreementRepository.findLatestByApplicationIdForUpdate(1001L)).thenReturn(Optional.empty());
        when(healthAssessmentRecordRepository.findTopByApplicationIdAndAssessmentTypeInOrderByAssessedAtDescIdDesc(eq(1001L), any()))
                .thenReturn(Optional.empty());

        CareOrchestrationService service = newService();
        ServiceJourneyResultDTO result = service.continueAfterAssessment(1001L);

        assertEquals("TERMINATED", result.getFinalStatus());
        assertEquals("申请已撤回", result.getMessage());
        verify(serviceJourneyTransitionPolicy).requireAuthority("journey:withdraw");
        verify(serviceJourneyTransitionLogService).logTransition(
                eq(1001L),
                isNull(),
                eq(3003L),
                eq(ServiceJourneyState.PENDING_ASSESSMENT),
                eq(ServiceJourneyEvent.JOURNEY_WITHDRAWN),
                eq(ServiceJourneyState.TERMINATED),
                eq("申请已撤回"),
                any(ServiceApplicationPo.class)
        );
    }

    @Test
    void shouldContinueToInServiceAfterHealthApproval() {
        ServiceApplicationPo application = applicationPo(1001L, 3003L,
                org.smart_elder_system.admission.model.ServiceApplication.STATUS_PASSED);
        application.setApplicantName("张三");
        application.setServiceScene("HOME");

        ServiceAgreementPo agreement = new ServiceAgreementPo();
        agreement.setId(2002L);
        agreement.setApplicationId(1001L);
        agreement.setElderId(3003L);
        agreement.setStatus(org.smart_elder_system.contract.model.ServiceAgreement.STATUS_DRAFT);
        agreement.setServiceScene("HOME");

        HealthAssessmentRecordPo assessment = new HealthAssessmentRecordPo();
        assessment.setApplicationId(1001L);
        assessment.setElderId(3003L);
        assessment.setAgreementId(2002L);
        assessment.setAssessmentType("PRE_SIGN_PASS");
        assessment.setAssessedAt(LocalDateTime.now());

        ServiceAgreementDTO signedAgreement = new ServiceAgreementDTO();
        signedAgreement.setAgreementId(2002L);
        signedAgreement.setApplicationId(1001L);
        signedAgreement.setElderId(3003L);
        signedAgreement.setServiceScene("HOME");
        signedAgreement.setStatus(org.smart_elder_system.contract.model.ServiceAgreement.STATUS_ACTIVE);

        when(serviceApplicationRepository.findByIdForUpdate(1001L)).thenReturn(Optional.of(application));
        when(serviceAgreementRepository.findLatestByApplicationIdForUpdate(1001L)).thenReturn(Optional.of(agreement));
        when(healthAssessmentRecordRepository.findTopByApplicationIdAndAssessmentTypeInOrderByAssessedAtDescIdDesc(eq(1001L), any()))
                .thenReturn(Optional.of(assessment));
        when(contractService.signAgreement(any())).thenReturn(signedAgreement);
        when(carePlanRepository.findTopByAgreementIdOrderByPlanDateDescIdDesc(2002L)).thenReturn(Optional.empty());
        when(healthProfileRepository.findTopByElderIdAndAgreementIdOrderByProfileDateDescIdDesc(3003L, 2002L)).thenReturn(Optional.empty());
        when(healthAssessmentRecordRepository.findTopByAgreementIdAndAssessmentTypeOrderByAssessedAtDescIdDesc(2002L, "INTAKE"))
                .thenReturn(Optional.empty());

        CarePlanDTO carePlan = new CarePlanDTO();
        carePlan.setPlanId(5001L);
        when(careDeliveryService.createCarePlan(any())).thenReturn(carePlan);

        HealthProfileDTO healthProfile = new HealthProfileDTO();
        healthProfile.setProfileId(6001L);
        when(healthService.createHealthProfile(any())).thenReturn(healthProfile);

        HealthAssessmentDTO intakeAssessment = new HealthAssessmentDTO();
        intakeAssessment.setAssessmentId(7001L);
        when(healthService.performAssessment(any())).thenReturn(intakeAssessment);

        CareOrchestrationService service = newService();
        ServiceJourneyResultDTO result = service.continueAfterAssessment(1001L);

        assertEquals("IN_SERVICE", result.getFinalStatus());
        assertEquals("需求评估与健康评估均已通过，已签订协议并进入在服状态", result.getMessage());
        assertEquals(2002L, result.getAgreementId());
        assertEquals(5001L, result.getCarePlanId());
        assertEquals(6001L, result.getHealthProfileId());
        assertEquals(7001L, result.getHealthAssessmentId());
        verify(serviceJourneyTransitionPolicy).requireAuthority("journey:health:approve");
        verify(serviceJourneyTransitionLogService).logTransition(
                eq(1001L),
                eq(2002L),
                eq(3003L),
                eq(ServiceJourneyState.PENDING_HEALTH_ASSESSMENT),
                eq(ServiceJourneyEvent.HEALTH_APPROVED),
                eq(ServiceJourneyState.PENDING_AGREEMENT),
                eq("健康评估通过，进入待签约阶段"),
                eq(assessment)
        );
        verify(serviceJourneyTransitionLogService).logTransition(
                eq(1001L),
                eq(2002L),
                eq(3003L),
                eq(ServiceJourneyState.PENDING_AGREEMENT),
                eq(ServiceJourneyEvent.AGREEMENT_SIGNED),
                eq(ServiceJourneyState.IN_SERVICE),
                eq("需求评估与健康评估均已通过，已签订协议并进入在服状态"),
                eq(signedAgreement)
        );
    }

    @Test
    void shouldRejectAdmissionJourney() {
        ServiceApplicationPo application = applicationPo(1001L, 3003L,
                org.smart_elder_system.admission.model.ServiceApplication.STATUS_SUBMITTED);

        ServiceApplicationDTO rejected = new ServiceApplicationDTO();
        rejected.setApplicationId(1001L);
        rejected.setElderId(3003L);
        rejected.setStatus(org.smart_elder_system.admission.model.ServiceApplication.STATUS_FAILED);

        when(serviceApplicationRepository.findByIdForUpdate(1001L)).thenReturn(Optional.of(application));
        when(admissionService.assessEligibility(any())).thenReturn(rejected);

        CareOrchestrationService service = newService();
        ServiceJourneyResultDTO result = service.rejectAdmissionJourney(1001L, "不符合准入条件", "tester");

        assertEquals("TERMINATED", result.getFinalStatus());
        assertEquals("需求评估未通过，服务终止", result.getMessage());
        verify(serviceJourneyTransitionPolicy).requireAuthority("journey:assessment:reject");
        verify(serviceJourneyTransitionLogService).logTransition(
                1001L,
                null,
                3003L,
                ServiceJourneyState.PENDING_ASSESSMENT,
                ServiceJourneyEvent.ADMISSION_REJECTED,
                ServiceJourneyState.TERMINATED,
                "需求评估未通过，服务终止",
                rejected
        );
        verify(serviceJourneyTaskService).completeOpenTask(
                1001L,
                ServiceJourneyTaskService.TASK_TYPE_ADMISSION_ASSESSMENT);
    }

    @Test
    void shouldRejectHealthJourney() {
        ServiceApplicationPo application = applicationPo(1001L, 3003L,
                org.smart_elder_system.admission.model.ServiceApplication.STATUS_PASSED);

        HealthAssessmentRequestDTO rejected = new HealthAssessmentRequestDTO();
        rejected.setApplicationId(1001L);
        rejected.setElderId(3003L);
        rejected.setAssessmentStatus("FAILED");

        when(serviceApplicationRepository.findByIdForUpdate(1001L)).thenReturn(Optional.of(application));
        when(serviceAgreementRepository.findLatestByApplicationIdForUpdate(1001L)).thenReturn(Optional.empty());
        when(healthAssessmentRecordRepository.findTopByApplicationIdAndAssessmentTypeInOrderByAssessedAtDescIdDesc(eq(1001L), any()))
                .thenReturn(Optional.empty());
        when(healthService.submitPreSignAssessment(any())).thenReturn(rejected);

        CareOrchestrationService service = newService();
        ServiceJourneyResultDTO result = service.rejectHealthJourney(1001L, "高风险不适合签约", "nurse", "doctor", 40);

        assertEquals("TERMINATED", result.getFinalStatus());
        assertEquals("健康评估未通过，服务终止", result.getMessage());
        verify(serviceJourneyTransitionPolicy).requireAuthority("journey:health:reject");
        verify(serviceJourneyTransitionLogService).logTransition(
                1001L,
                null,
                3003L,
                ServiceJourneyState.PENDING_HEALTH_ASSESSMENT,
                ServiceJourneyEvent.HEALTH_REJECTED,
                ServiceJourneyState.TERMINATED,
                "健康评估未通过，服务终止",
                rejected
        );
        verify(serviceJourneyTaskService).completeOpenTask(
                1001L,
                ServiceJourneyTaskService.TASK_TYPE_HEALTH_ASSESSMENT);
    }

    @Test
    void shouldWithdrawJourneyBeforeServiceStarts() {
        ServiceApplicationPo application = applicationPo(1001L, 3003L,
                org.smart_elder_system.admission.model.ServiceApplication.STATUS_PASSED);

        ServiceApplicationDTO withdrawn = new ServiceApplicationDTO();
        withdrawn.setApplicationId(1001L);
        withdrawn.setElderId(3003L);
        withdrawn.setStatus(org.smart_elder_system.admission.model.ServiceApplication.STATUS_WITHDRAWN);

        when(serviceApplicationRepository.findByIdForUpdate(1001L)).thenReturn(Optional.of(application));
        when(serviceAgreementRepository.findLatestByApplicationIdForUpdate(1001L)).thenReturn(Optional.empty());
        when(healthAssessmentRecordRepository.findTopByApplicationIdAndAssessmentTypeInOrderByAssessedAtDescIdDesc(eq(1001L), any()))
                .thenReturn(Optional.empty());
        when(admissionService.withdrawApplication(1001L, "用户主动撤回")).thenReturn(withdrawn);

        CareOrchestrationService service = newService();
        ServiceJourneyResultDTO result = service.withdrawServiceJourney(1001L, "用户主动撤回");

        assertEquals("TERMINATED", result.getFinalStatus());
        assertEquals("申请已撤回", result.getMessage());
        assertEquals(1001L, result.getApplicationId());
        verify(serviceJourneyTransitionPolicy).requireAuthority("journey:withdraw");
        verify(admissionService).withdrawApplication(1001L, "用户主动撤回");
        verify(serviceJourneyTransitionLogService).logTransition(
                1001L,
                null,
                3003L,
                ServiceJourneyState.PENDING_HEALTH_ASSESSMENT,
                ServiceJourneyEvent.JOURNEY_WITHDRAWN,
                ServiceJourneyState.TERMINATED,
                "申请已撤回",
                withdrawn
        );
        verify(serviceJourneyTaskService).completeOpenTask(
                1001L,
                ServiceJourneyTaskService.TASK_TYPE_ADMISSION_ASSESSMENT);
        verify(serviceJourneyTaskService).completeOpenTask(
                1001L,
                ServiceJourneyTaskService.TASK_TYPE_HEALTH_ASSESSMENT);
    }

    @Test
    void shouldRejectWithdrawForInServiceJourney() {
        ServiceApplicationPo application = applicationPo(1001L, 3003L,
                org.smart_elder_system.admission.model.ServiceApplication.STATUS_PASSED);

        ServiceAgreementPo agreement = new ServiceAgreementPo();
        agreement.setId(2002L);
        agreement.setApplicationId(1001L);
        agreement.setElderId(3003L);
        agreement.setStatus(org.smart_elder_system.contract.model.ServiceAgreement.STATUS_ACTIVE);

        when(serviceApplicationRepository.findByIdForUpdate(1001L)).thenReturn(Optional.of(application));
        when(serviceAgreementRepository.findLatestByApplicationIdForUpdate(1001L)).thenReturn(Optional.of(agreement));
        when(healthAssessmentRecordRepository.findTopByApplicationIdAndAssessmentTypeInOrderByAssessedAtDescIdDesc(eq(1001L), any()))
                .thenReturn(Optional.empty());

        CareOrchestrationService service = newService();

        assertThrows(IllegalStateException.class, () -> service.withdrawServiceJourney(1001L, "用户主动撤回"));
        verify(admissionService, never()).withdrawApplication(1001L, "用户主动撤回");
    }

    @Test
    void shouldReturnFromHealthAssessmentToAssessment() {
        ServiceApplicationPo application = applicationPo(1001L, 3003L,
                org.smart_elder_system.admission.model.ServiceApplication.STATUS_PASSED);

        ServiceApplicationDTO returned = new ServiceApplicationDTO();
        returned.setApplicationId(1001L);
        returned.setElderId(3003L);
        returned.setStatus(org.smart_elder_system.admission.model.ServiceApplication.STATUS_ASSESSED);

        when(serviceApplicationRepository.findByIdForUpdate(1001L)).thenReturn(Optional.of(application));
        when(serviceAgreementRepository.findLatestByApplicationIdForUpdate(1001L)).thenReturn(Optional.empty());
        when(healthAssessmentRecordRepository.findTopByApplicationIdAndAssessmentTypeInOrderByAssessedAtDescIdDesc(eq(1001L), any()))
                .thenReturn(Optional.empty());
        when(admissionService.revertToAssessment(1001L, "资料需补充")).thenReturn(returned);

        CareOrchestrationService service = newService();
        ServiceJourneyResultDTO result = service.returnJourneyStep(1001L, ServiceJourneyState.PENDING_ASSESSMENT, "资料需补充");

        assertEquals("PENDING_ASSESSMENT", result.getFinalStatus());
        verify(serviceJourneyTransitionPolicy).requireAuthority("journey:return:assessment");
        verify(serviceJourneyTransitionLogService).logTransition(
                1001L,
                null,
                3003L,
                ServiceJourneyState.PENDING_HEALTH_ASSESSMENT,
                ServiceJourneyEvent.RETURN_TO_ASSESSMENT,
                ServiceJourneyState.PENDING_ASSESSMENT,
                "资料需补充",
                returned
        );
        verify(serviceJourneyTaskService).cancelOpenTask(1001L, ServiceJourneyTaskService.TASK_TYPE_HEALTH_ASSESSMENT);
        verify(serviceJourneyTaskService).createAdmissionAssessmentTask(1001L, 3003L);
    }

    @Test
    void shouldReturnFromPendingAgreementToHealthAssessment() {
        ServiceApplicationPo application = applicationPo(1001L, 3003L,
                org.smart_elder_system.admission.model.ServiceApplication.STATUS_PASSED);

        ServiceAgreementPo agreement = new ServiceAgreementPo();
        agreement.setId(2002L);
        agreement.setApplicationId(1001L);
        agreement.setElderId(3003L);
        agreement.setStatus(org.smart_elder_system.contract.model.ServiceAgreement.STATUS_DRAFT);

        HealthAssessmentRecordPo assessment = new HealthAssessmentRecordPo();
        assessment.setApplicationId(1001L);
        assessment.setElderId(3003L);
        assessment.setAssessmentType("PRE_SIGN_PASS");
        assessment.setAssessedAt(LocalDateTime.now());

        ServiceAgreementDTO returnedAgreement = new ServiceAgreementDTO();
        returnedAgreement.setAgreementId(2002L);
        returnedAgreement.setApplicationId(1001L);
        returnedAgreement.setElderId(3003L);
        returnedAgreement.setStatus(org.smart_elder_system.contract.model.ServiceAgreement.STATUS_DRAFT);

        when(serviceApplicationRepository.findByIdForUpdate(1001L)).thenReturn(Optional.of(application));
        when(serviceAgreementRepository.findLatestByApplicationIdForUpdate(1001L)).thenReturn(Optional.of(agreement));
        when(healthAssessmentRecordRepository.findTopByApplicationIdAndAssessmentTypeInOrderByAssessedAtDescIdDesc(eq(1001L), any()))
                .thenReturn(Optional.of(assessment));
        when(contractService.revertToDraftAgreement(2002L, "重新评估健康状况")).thenReturn(returnedAgreement);

        CareOrchestrationService service = newService();
        ServiceJourneyResultDTO result = service.returnJourneyStep(1001L, ServiceJourneyState.PENDING_HEALTH_ASSESSMENT, "重新评估健康状况");

        assertEquals("PENDING_HEALTH_ASSESSMENT", result.getFinalStatus());
        verify(serviceJourneyTransitionPolicy).requireAuthority("journey:return:health");
        verify(serviceJourneyTransitionLogService).logTransition(
                1001L,
                2002L,
                3003L,
                ServiceJourneyState.PENDING_AGREEMENT,
                ServiceJourneyEvent.RETURN_TO_HEALTH_ASSESSMENT,
                ServiceJourneyState.PENDING_HEALTH_ASSESSMENT,
                "重新评估健康状况",
                returnedAgreement
        );
        verify(serviceJourneyTaskService).cancelOpenTask(1001L, ServiceJourneyTaskService.TASK_TYPE_HEALTH_ASSESSMENT);
        verify(serviceJourneyTaskService).createHealthAssessmentTask(1001L, 3003L);
    }

    @Test
    void shouldRenewFromImprovementRequiredJourney() {
        ServiceAgreementPo agreement = new ServiceAgreementPo();
        agreement.setId(2002L);
        agreement.setApplicationId(1001L);
        agreement.setElderId(3003L);
        agreement.setStatus(org.smart_elder_system.contract.model.ServiceAgreement.STATUS_ACTIVE);

        ServiceReviewPo previousReview = new ServiceReviewPo();
        previousReview.setAgreementId(2002L);
        previousReview.setReviewConclusion(org.smart_elder_system.quality.model.ServiceReview.REVIEW_CONCLUSION_IMPROVE);

        ServiceReviewDTO reviewed = new ServiceReviewDTO();
        reviewed.setAgreementId(2002L);
        reviewed.setElderId(3003L);
        reviewed.setSatisfactionScore(85);
        reviewed.setReviewComment("改善后续约");
        reviewed.setReviewConclusion(org.smart_elder_system.quality.model.ServiceReview.REVIEW_CONCLUSION_RENEW);

        when(serviceAgreementRepository.findByIdForUpdate(2002L)).thenReturn(Optional.of(agreement));
        when(serviceReviewRepository.findLatestByAgreementIdForUpdate(2002L)).thenReturn(Optional.of(previousReview));
        when(qualityService.reviewService(any())).thenReturn(reviewed);

        CareOrchestrationService service = newService();
        ServiceJourneyResultDTO result = service.reviewAndFinalize(2002L, 3003L, 85, "改善后续约");

        assertEquals("RENEWED", result.getFinalStatus());
        assertEquals("服务评价结果为续约，协议已续约", result.getMessage());
        verify(serviceJourneyTransitionPolicy).requireAuthority("journey:review:renew");
        verify(contractService).renewAgreement(anyLong(), any());
        verify(serviceJourneyTransitionLogService).logTransition(
                eq(1001L),
                eq(2002L),
                eq(3003L),
                eq(ServiceJourneyState.IMPROVEMENT_REQUIRED),
                eq(ServiceJourneyEvent.REVIEW_RENEW),
                eq(ServiceJourneyState.RENEWED),
                eq("服务评价结果为续约，协议已续约"),
                any(ServiceReviewDTO.class)
        );
    }

    @Test
    void shouldReturnCurrentResultWhenRepeatedImproveReview() {
        ServiceAgreementPo agreement = new ServiceAgreementPo();
        agreement.setId(2002L);
        agreement.setApplicationId(1001L);
        agreement.setElderId(3003L);
        agreement.setStatus(org.smart_elder_system.contract.model.ServiceAgreement.STATUS_ACTIVE);

        ServiceReviewPo previousReview = new ServiceReviewPo();
        previousReview.setAgreementId(2002L);
        previousReview.setReviewConclusion(org.smart_elder_system.quality.model.ServiceReview.REVIEW_CONCLUSION_IMPROVE);

        when(serviceAgreementRepository.findByIdForUpdate(2002L)).thenReturn(Optional.of(agreement));
        when(serviceReviewRepository.findLatestByAgreementIdForUpdate(2002L)).thenReturn(Optional.of(previousReview));

        CareOrchestrationService service = newService();
        ServiceJourneyResultDTO result = service.reviewAndFinalize(2002L, 3003L, 70, "继续改进");

        assertEquals("IMPROVEMENT_REQUIRED", result.getFinalStatus());
        assertEquals("服务评价完成，建议结果：IMPROVE", result.getMessage());
        verify(serviceJourneyTransitionPolicy).requireAuthority(ServiceJourneyEvent.REVIEW_IMPROVE);
        verify(qualityService, never()).reviewService(any());
        verify(contractService, never()).renewAgreement(anyLong(), any());
        verify(contractService, never()).terminateAgreement(anyLong());
    }

    @Test
    void shouldRejectWhenPermissionMissing() {
        ServiceApplicationPo application = applicationPo(1001L, 3003L,
                org.smart_elder_system.admission.model.ServiceApplication.STATUS_SUBMITTED);
        when(serviceApplicationRepository.findByIdForUpdate(1001L)).thenReturn(Optional.of(application));
        doThrow(new JourneyAuthorizationException("当前用户无权执行旅程操作: journey:assessment:reject"))
                .when(serviceJourneyTransitionPolicy)
                .requireAuthority("journey:assessment:reject");

        CareOrchestrationService service = newService();

        assertThrows(JourneyAuthorizationException.class,
                () -> service.rejectAdmissionJourney(1001L, "不符合准入条件", "tester"));
        verify(admissionService, never()).assessEligibility(any());
    }

    private CareOrchestrationService newService() {
        return new CareOrchestrationService(
                admissionClient,
                contractClient,
                careDeliveryClient,
                healthClient,
                qualityClient,
                admissionService,
                contractService,
                careDeliveryService,
                healthService,
                qualityService,
                serviceApplicationRepository,
                serviceAgreementRepository,
                carePlanRepository,
                healthProfileRepository,
                healthAssessmentRecordRepository,
                serviceReviewRepository,
                new ServiceJourneyStateMachine(),
                serviceJourneyTransitionPolicy,
                serviceJourneyTaskService,
                serviceJourneyTransitionLogService
        );
    }

    private ServiceApplicationPo applicationPo(Long applicationId, Long elderId, String status) {
        ServiceApplicationPo application = new ServiceApplicationPo();
        application.setId(applicationId);
        application.setElderId(elderId);
        application.setStatus(status);
        application.setSubmittedAt(LocalDateTime.now().minusDays(1));
        return application;
    }
}
