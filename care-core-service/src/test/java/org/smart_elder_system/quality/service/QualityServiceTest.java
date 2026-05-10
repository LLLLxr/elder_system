package org.smart_elder_system.quality.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.smart_elder_system.careorchestration.service.ServiceJourneyTaskService;
import org.smart_elder_system.careorchestration.service.ServiceJourneyTransitionLogService;
import org.smart_elder_system.common.dto.quality.CaregiverQualificationApplicationDto;
import org.smart_elder_system.common.dto.quality.CaregiverQualificationReviewDto;
import org.smart_elder_system.common.dto.quality.ServiceReviewDto;
import org.smart_elder_system.quality.QualityAuthorizationPolicy;
import org.smart_elder_system.quality.vo.CaregiverQualificationApplication;
import org.smart_elder_system.quality.vo.ServiceReview;
import org.smart_elder_system.quality.po.CaregiverQualificationApplicationPo;
import org.smart_elder_system.quality.po.ServiceReviewPo;
import org.smart_elder_system.quality.repository.CaregiverQualificationApplicationRepository;
import org.smart_elder_system.quality.repository.ServiceReviewRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QualityServiceTest {

    @Mock
    private ServiceReviewRepository serviceReviewRepository;
    @Mock
    private CaregiverQualificationApplicationRepository caregiverQualificationApplicationRepository;
    @Mock
    private QualityAuthorizationPolicy qualityAuthorizationPolicy;
    @Mock
    private ServiceJourneyTaskService serviceJourneyTaskService;
    @Mock
    private ServiceJourneyTransitionLogService serviceJourneyTransitionLogService;

    @InjectMocks
    private QualityService qualityService;

    @Test
    void shouldReviewServiceUsingModelConversion() {
        ServiceReviewDto request = new ServiceReviewDto();
        request.setAgreementId(1L);
        request.setElderId(1001L);
        request.setSatisfactionScore(85);
        request.setReviewComment("服务稳定");

        when(serviceReviewRepository.save(any())).thenAnswer(invocation -> {
            ServiceReviewPo po = invocation.getArgument(0);
            po.setId(10L);
            return po;
        });

        ServiceReviewDto result = qualityService.reviewService(request);

        assertEquals(10L, result.getReviewId());
        assertEquals(ServiceReview.REVIEW_CONCLUSION_RENEW, result.getReviewConclusion());
        assertNotNull(result.getReviewedAt());

        ArgumentCaptor<ServiceReviewPo> captor = ArgumentCaptor.forClass(ServiceReviewPo.class);
        verify(serviceReviewRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getAgreementId());
        assertEquals(1001L, captor.getValue().getElderId());
        assertEquals(85, captor.getValue().getSatisfactionScore());
        assertEquals("服务稳定", captor.getValue().getReviewComment());
        assertEquals(ServiceReview.REVIEW_CONCLUSION_RENEW, captor.getValue().getReviewConclusion());
        assertNotNull(captor.getValue().getReviewedAt());
    }

    @Test
    void shouldSubmitCaregiverQualificationApplication() {
        when(qualityAuthorizationPolicy.requireCurrentUserId()).thenReturn(2001L);
        when(qualityAuthorizationPolicy.requireCurrentUsername()).thenReturn("caregiver1");
        when(caregiverQualificationApplicationRepository.findByCaregiverUserIdForUpdate(2001L)).thenReturn(List.of());
        when(caregiverQualificationApplicationRepository.save(any())).thenAnswer(invocation -> {
            CaregiverQualificationApplicationPo po = invocation.getArgument(0);
            po.setId(31L);
            return po;
        });

        CaregiverQualificationApplicationDto request = qualificationRequest();

        CaregiverQualificationApplicationDto result = qualityService.submitCaregiverQualificationApplication(request);

        assertEquals(31L, result.getApplicationId());
        assertEquals(CaregiverQualificationApplication.STATUS_PENDING, result.getStatus());
        assertEquals(2001L, result.getCaregiverUserId());
        assertEquals("caregiver1", result.getCaregiverUsername());

        ArgumentCaptor<CaregiverQualificationApplicationPo> captor = ArgumentCaptor.forClass(CaregiverQualificationApplicationPo.class);
        verify(caregiverQualificationApplicationRepository).save(captor.capture());
        assertEquals(2001L, captor.getValue().getCaregiverUserId());
        assertEquals("caregiver1", captor.getValue().getCaregiverUsername());
        assertEquals(CaregiverQualificationApplication.STATUS_PENDING, captor.getValue().getStatus());
        assertEquals(1, captor.getValue().getActiveFlag());
        verify(serviceJourneyTaskService).createCaregiverQualificationReviewTask(31L, 2001L);
        verify(serviceJourneyTransitionLogService).logBusinessAction(
                eq("CAREGIVER_QUALIFICATION_APPLICATION"),
                eq(31L),
                eq(null),
                eq("SUBMIT"),
                eq("护理员资质申请提交成功"),
                eq(request)
        );
    }

    @Test
    void shouldRejectDuplicatePendingCaregiverQualificationApplication() {
        when(qualityAuthorizationPolicy.requireCurrentUserId()).thenReturn(2001L);
        when(caregiverQualificationApplicationRepository.findByCaregiverUserIdForUpdate(2001L))
                .thenReturn(List.of(qualificationPo(41L, CaregiverQualificationApplication.STATUS_PENDING, 1)));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> qualityService.submitCaregiverQualificationApplication(qualificationRequest()));

        assertEquals("当前护理员已有待审核的资质申请", exception.getMessage());
        verify(caregiverQualificationApplicationRepository, never()).save(any());
        verify(serviceJourneyTaskService, never()).createCaregiverQualificationReviewTask(any(), any());
        verify(serviceJourneyTransitionLogService, never()).logBusinessAction(any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldReturnLatestMyCaregiverQualificationApplication() {
        when(qualityAuthorizationPolicy.requireCurrentUserId()).thenReturn(2001L);
        when(caregiverQualificationApplicationRepository.findByCaregiverUserIdOrderByCreatedDateTimeUtcDesc(2001L))
                .thenReturn(List.of(qualificationPo(51L, CaregiverQualificationApplication.STATUS_APPROVED, null)));

        CaregiverQualificationApplicationDto result = qualityService.getMyCaregiverQualificationApplication();

        assertEquals(51L, result.getApplicationId());
        assertEquals(CaregiverQualificationApplication.STATUS_APPROVED, result.getStatus());
    }

    @Test
    void shouldListMyCaregiverQualificationApplications() {
        when(qualityAuthorizationPolicy.requireCurrentUserId()).thenReturn(2001L);
        when(caregiverQualificationApplicationRepository.findByCaregiverUserIdOrderByCreatedDateTimeUtcDesc(2001L))
                .thenReturn(List.of(
                        qualificationPo(51L, CaregiverQualificationApplication.STATUS_APPROVED, null),
                        qualificationPo(50L, CaregiverQualificationApplication.STATUS_REJECTED, null)));

        List<CaregiverQualificationApplicationDto> result = qualityService.listMyCaregiverQualificationApplications();

        assertEquals(2, result.size());
        assertEquals(51L, result.get(0).getApplicationId());
        assertEquals(50L, result.get(1).getApplicationId());
    }

    @Test
    void shouldUsePendingStatusWhenListingCaregiverQualificationApplicationsWithoutStatus() {
        when(caregiverQualificationApplicationRepository.findByStatusOrderByCreatedDateTimeUtcDesc(CaregiverQualificationApplication.STATUS_PENDING))
                .thenReturn(List.of(qualificationPo(61L, CaregiverQualificationApplication.STATUS_PENDING, 1)));

        List<CaregiverQualificationApplicationDto> result = qualityService.listCaregiverQualificationApplications(" ");

        assertEquals(1, result.size());
        assertEquals(CaregiverQualificationApplication.STATUS_PENDING, result.get(0).getStatus());
    }

    @Test
    void shouldReturnCaregiverQualificationApplicationDetail() {
        when(caregiverQualificationApplicationRepository.findById(71L))
                .thenReturn(Optional.of(qualificationPo(71L, CaregiverQualificationApplication.STATUS_PENDING, 1)));

        CaregiverQualificationApplicationDto result = qualityService.getCaregiverQualificationApplicationDetail(71L);

        assertEquals(71L, result.getApplicationId());
        assertEquals(CaregiverQualificationApplication.STATUS_PENDING, result.getStatus());
    }

    @Test
    void shouldApproveCaregiverQualificationApplication() {
        when(qualityAuthorizationPolicy.requireCurrentUsername()).thenReturn("medic1");
        CaregiverQualificationApplicationPo po = qualificationPo(81L, CaregiverQualificationApplication.STATUS_PENDING, 1);
        when(caregiverQualificationApplicationRepository.findByIdForUpdate(81L)).thenReturn(Optional.of(po));
        when(caregiverQualificationApplicationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CaregiverQualificationReviewDto reviewDto = new CaregiverQualificationReviewDto();
        reviewDto.setReviewComment("资质齐全");

        CaregiverQualificationApplicationDto result = qualityService.approveCaregiverQualificationApplication(81L, reviewDto);

        assertEquals(CaregiverQualificationApplication.STATUS_APPROVED, result.getStatus());
        assertEquals("medic1", result.getReviewedBy());
        assertEquals("资质齐全", result.getReviewComment());
        assertEquals(CaregiverQualificationApplication.STATUS_APPROVED, po.getStatus());
        assertEquals("medic1", po.getReviewedBy());
        assertEquals("资质齐全", po.getReviewComment());
        assertNotNull(po.getReviewedAt());
        assertNull(po.getActiveFlag());
        verify(serviceJourneyTaskService).completeOpenTask(81L, ServiceJourneyTaskService.TASK_TYPE_CAREGIVER_QUALIFICATION_REVIEW);
        verify(serviceJourneyTransitionLogService).logBusinessAction(
                eq("CAREGIVER_QUALIFICATION_APPLICATION"),
                eq(81L),
                eq(null),
                eq("APPROVE"),
                eq("资质齐全"),
                eq(reviewDto)
        );
    }

    @Test
    void shouldRejectCaregiverQualificationApplication() {
        when(qualityAuthorizationPolicy.requireCurrentUsername()).thenReturn("medic1");
        CaregiverQualificationApplicationPo po = qualificationPo(91L, CaregiverQualificationApplication.STATUS_PENDING, 1);
        when(caregiverQualificationApplicationRepository.findByIdForUpdate(91L)).thenReturn(Optional.of(po));
        when(caregiverQualificationApplicationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CaregiverQualificationReviewDto reviewDto = new CaregiverQualificationReviewDto();
        reviewDto.setReviewComment("资料不完整");

        CaregiverQualificationApplicationDto result = qualityService.rejectCaregiverQualificationApplication(91L, reviewDto);

        assertEquals(CaregiverQualificationApplication.STATUS_REJECTED, result.getStatus());
        assertEquals("medic1", result.getReviewedBy());
        assertEquals("资料不完整", result.getReviewComment());
        assertEquals(CaregiverQualificationApplication.STATUS_REJECTED, po.getStatus());
        assertEquals("medic1", po.getReviewedBy());
        assertEquals("资料不完整", po.getReviewComment());
        assertNotNull(po.getReviewedAt());
        assertNull(po.getActiveFlag());
        verify(serviceJourneyTaskService).completeOpenTask(91L, ServiceJourneyTaskService.TASK_TYPE_CAREGIVER_QUALIFICATION_REVIEW);
        verify(serviceJourneyTransitionLogService).logBusinessAction(
                eq("CAREGIVER_QUALIFICATION_APPLICATION"),
                eq(91L),
                eq(null),
                eq("REJECT"),
                eq("资料不完整"),
                eq(reviewDto)
        );
    }

    @Test
    void shouldRejectRepeatedApprovalOfCaregiverQualificationApplication() {
        when(qualityAuthorizationPolicy.requireCurrentUsername()).thenReturn("medic1");
        when(caregiverQualificationApplicationRepository.findByIdForUpdate(101L))
                .thenReturn(Optional.of(qualificationPo(101L, CaregiverQualificationApplication.STATUS_APPROVED, null)));

        CaregiverQualificationReviewDto reviewDto = new CaregiverQualificationReviewDto();
        reviewDto.setReviewComment("重复审核");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> qualityService.approveCaregiverQualificationApplication(101L, reviewDto));

        assertEquals("当前资质申请状态不允许审核", exception.getMessage());
        verify(caregiverQualificationApplicationRepository, never()).save(any());
        verify(serviceJourneyTaskService, never()).completeOpenTask(any(), any());
        verify(serviceJourneyTransitionLogService, never()).logBusinessAction(any(), any(), any(), any(), any(), any());
    }

    private CaregiverQualificationApplicationDto qualificationRequest() {
        CaregiverQualificationApplicationDto request = new CaregiverQualificationApplicationDto();
        request.setRealName("护理员甲");
        request.setPhone("13800000000");
        request.setIdCardNo("110101199001010011");
        request.setCertificateNo("CERT-001");
        request.setCertificateType("护士执业证");
        request.setYearsOfExperience(5);
        request.setSkillSummary("擅长失能老人护理");
        return request;
    }

    private CaregiverQualificationApplicationPo qualificationPo(Long id, String status, Integer activeFlag) {
        CaregiverQualificationApplicationPo po = new CaregiverQualificationApplicationPo();
        po.setId(id);
        po.setCaregiverUserId(2001L);
        po.setCaregiverUsername("caregiver1");
        po.setRealName("护理员甲");
        po.setPhone("13800000000");
        po.setIdCardNo("110101199001010011");
        po.setCertificateNo("CERT-001");
        po.setCertificateType("护士执业证");
        po.setYearsOfExperience(5);
        po.setSkillSummary("擅长失能老人护理");
        po.setStatus(status);
        po.setActiveFlag(activeFlag);
        po.setCreatedDateTimeUtc(LocalDateTime.now());
        return po;
    }
}
