package org.smart_elder_system.health.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.smart_elder_system.admission.model.ServiceApplication;
import org.smart_elder_system.admission.po.ServiceApplicationPo;
import org.smart_elder_system.admission.repository.ServiceApplicationRepository;
import org.smart_elder_system.common.dto.care.HealthAssessmentRequestDTO;
import org.smart_elder_system.common.dto.care.HealthAssessmentSubmitDTO;
import org.smart_elder_system.common.dto.care.HealthCheckFormCreateRequestDTO;
import org.smart_elder_system.common.dto.care.HealthCheckFormDTO;
import org.smart_elder_system.common.dto.care.HealthProfileDTO;
import org.smart_elder_system.health.HealthAuthorizationException;
import org.smart_elder_system.health.HealthAuthorizationPolicy;
import org.smart_elder_system.health.po.HealthAssessmentRecordPo;
import org.smart_elder_system.health.po.HealthCheckFormPo;
import org.smart_elder_system.health.po.HealthProfilePo;
import org.smart_elder_system.health.repository.HealthAssessmentRecordRepository;
import org.smart_elder_system.health.repository.HealthCheckFormRepository;
import org.smart_elder_system.health.repository.HealthProfileRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthServiceTest {

    @Mock
    private HealthProfileRepository healthProfileRepository;

    @Mock
    private HealthAssessmentRecordRepository healthAssessmentRecordRepository;

    @Mock
    private HealthCheckFormRepository healthCheckFormRepository;

    @Mock
    private ServiceApplicationRepository serviceApplicationRepository;

    @Mock
    private HealthAuthorizationPolicy healthAuthorizationPolicy;

    @InjectMocks
    private HealthService healthService;

    @Test
    void shouldCreateHealthProfileUsingModelConversion() {
        HealthProfileDTO request = new HealthProfileDTO();
        request.setElderId(1001L);
        request.setAgreementId(1L);
        request.setBloodType("A");
        request.setChronicDiseaseSummary("高血压");
        request.setAllergySummary("青霉素");
        request.setRiskLevel("MEDIUM");

        when(healthProfileRepository.findByElderIdAndAgreementIdForUpdate(1001L, 1L)).thenReturn(Optional.empty());
        when(healthProfileRepository.save(any())).thenAnswer(invocation -> {
            HealthProfilePo po = invocation.getArgument(0);
            po.setId(10L);
            return po;
        });

        HealthProfileDTO result = healthService.createHealthProfile(request);

        assertEquals(10L, result.getProfileId());
        assertEquals("A", result.getBloodType());
        assertEquals("高血压", result.getChronicDiseaseSummary());
        assertNotNull(result.getProfileDate());

        ArgumentCaptor<HealthProfilePo> captor = ArgumentCaptor.forClass(HealthProfilePo.class);
        verify(healthProfileRepository).save(captor.capture());
        assertEquals(1001L, captor.getValue().getElderId());
        assertEquals(1L, captor.getValue().getAgreementId());
        assertEquals("MEDIUM", captor.getValue().getRiskLevel());
    }

    @Test
    void shouldCreateAdminHealthCheckFormUsingCurrentUserId() {
        HealthCheckFormCreateRequestDTO request = new HealthCheckFormCreateRequestDTO();
        request.setElderId(1001L);
        request.setAgreementId(1L);
        request.setElderName("张三");
        request.setFormCode("HC-001");
        request.setResponsibleDoctor("李医生");
        request.setSymptomSection("乏力");
        request.setVitalSignSection("BP:120/80");
        request.setSelfEvaluationSection("良好");
        request.setCognitiveEmotionSection("稳定");
        request.setLifestyleSection("清淡饮食");
        request.setChronicDiseaseSummary("糖尿病");
        request.setAllergySummary("无");

        when(healthAuthorizationPolicy.requireCurrentUserId()).thenReturn(2001L);
        when(healthCheckFormRepository.save(any())).thenAnswer(invocation -> {
            HealthCheckFormPo po = invocation.getArgument(0);
            po.setId(20L);
            return po;
        });
        when(healthProfileRepository.findByElderIdAndAgreementIdForUpdate(1001L, 1L)).thenReturn(Optional.empty());
        when(healthProfileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        HealthCheckFormDTO result = healthService.createAdminHealthCheckForm(request);

        assertEquals(20L, result.getFormId());
        assertEquals(2001L, result.getAuthorUserId());
        assertEquals("PAPER_V1", result.getFormVersion());
        assertNotNull(result.getCheckDate());
        assertEquals("李医生", result.getResponsibleDoctor());

        ArgumentCaptor<HealthCheckFormPo> formCaptor = ArgumentCaptor.forClass(HealthCheckFormPo.class);
        verify(healthCheckFormRepository).save(formCaptor.capture());
        assertEquals(1001L, formCaptor.getValue().getElderId());
        assertEquals(2001L, formCaptor.getValue().getAuthorUserId());
        assertEquals("HC-001", formCaptor.getValue().getFormCode());

        ArgumentCaptor<HealthProfilePo> profileCaptor = ArgumentCaptor.forClass(HealthProfilePo.class);
        verify(healthProfileRepository).save(profileCaptor.capture());
        assertEquals("糖尿病", profileCaptor.getValue().getChronicDiseaseSummary());
        assertEquals("无", profileCaptor.getValue().getAllergySummary());
    }

    @Test
    void shouldRejectAdminHealthCheckFormWithoutPermission() {
        HealthCheckFormCreateRequestDTO request = new HealthCheckFormCreateRequestDTO();
        request.setElderId(1001L);
        request.setAgreementId(1L);
        request.setElderName("张三");

        doThrow(new HealthAuthorizationException("当前用户无权执行健康体检表操作: health:check-form:create"))
                .when(healthAuthorizationPolicy).requireCheckFormCreatePermission();

        HealthAuthorizationException exception = assertThrows(
                HealthAuthorizationException.class,
                () -> healthService.createAdminHealthCheckForm(request));

        assertEquals("当前用户无权执行健康体检表操作: health:check-form:create", exception.getMessage());
    }

    @Test
    void shouldSubmitPreSignAssessmentUsingModelConversion() {
        ServiceApplicationPo application = new ServiceApplicationPo();
        application.setId(1L);
        application.setElderId(1001L);
        application.setApplicantName("家属A");
        application.setServiceScene("HOME");
        application.setStatus(ServiceApplication.STATUS_PASSED);
        application.setSubmittedAt(LocalDateTime.of(2026, 4, 26, 10, 0));
        application.setAssessedAt(LocalDateTime.of(2026, 4, 26, 11, 0));

        HealthCheckFormPo checkForm = new HealthCheckFormPo();
        checkForm.setId(20L);
        checkForm.setElderId(1001L);
        checkForm.setAgreementId(1L);
        checkForm.setCheckDate(LocalDate.of(2026, 4, 26));

        when(serviceApplicationRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(application));
        when(healthCheckFormRepository.findTopByElderIdOrderByCheckDateDescIdDesc(1001L)).thenReturn(Optional.of(checkForm));
        when(healthAssessmentRecordRepository.findTopByApplicationIdAndAssessmentTypeInOrderByAssessedAtDescIdDesc(1L, List.of("PRE_SIGN_PASS", "PRE_SIGN_FAIL")))
                .thenReturn(Optional.empty());
        when(healthAssessmentRecordRepository.findTopByElderIdAndAssessmentTypeInAndAssessedAtGreaterThanEqualOrderByAssessedAtDescIdDesc(
                1001L,
                List.of("PRE_SIGN_PASS", "PRE_SIGN_FAIL"),
                LocalDateTime.of(2026, 4, 26, 10, 0)))
                .thenReturn(Optional.empty());
        when(healthAssessmentRecordRepository.save(any())).thenAnswer(invocation -> {
            HealthAssessmentRecordPo po = invocation.getArgument(0);
            po.setId(30L);
            return po;
        });

        HealthAssessmentSubmitDTO request = new HealthAssessmentSubmitDTO();
        request.setApplicationId(1L);
        request.setPassed(true);
        request.setAssessmentConclusion("适合签约");
        request.setAssessor("评估员");
        request.setResponsibleDoctor("李医生");
        request.setScore(88);

        HealthAssessmentRequestDTO result = healthService.submitPreSignAssessment(request);

        assertEquals(1L, result.getApplicationId());
        assertEquals("PASSED", result.getAssessmentStatus());
        assertEquals(88, result.getScore());
        assertNotNull(result.getHealthAssessedAt());

        ArgumentCaptor<HealthAssessmentRecordPo> captor = ArgumentCaptor.forClass(HealthAssessmentRecordPo.class);
        verify(healthAssessmentRecordRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getApplicationId());
        assertEquals(1001L, captor.getValue().getElderId());
        assertEquals(1L, captor.getValue().getAgreementId());
        assertEquals("PRE_SIGN_PASS", captor.getValue().getAssessmentType());
    }
}
