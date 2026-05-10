package org.smart_elder_system.admission.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.smart_elder_system.admission.AdmissionAuthorizationPolicy;
import org.smart_elder_system.admission.vo.FamilyVisitSlot;
import org.smart_elder_system.admission.po.FamilyVisitReservationPo;
import org.smart_elder_system.admission.po.FamilyVisitSlotPo;
import org.smart_elder_system.admission.po.ServiceApplicationPo;
import org.smart_elder_system.admission.repository.FamilyVisitReservationRepository;
import org.smart_elder_system.admission.repository.FamilyVisitSlotRepository;
import org.smart_elder_system.admission.repository.ServiceApplicationRepository;
import org.smart_elder_system.admission.rules.FamilyVisitReservationRules;
import org.smart_elder_system.careorchestration.service.ServiceJourneyTaskService;
import org.smart_elder_system.careorchestration.service.ServiceJourneyTransitionLogService;
import org.smart_elder_system.common.dto.admission.EligibilityAssessmentDto;
import org.smart_elder_system.common.dto.admission.FamilyVisitReservationDto;
import org.smart_elder_system.common.dto.admission.FamilyVisitReservationReviewDto;
import org.smart_elder_system.common.dto.admission.FamilyVisitReservationRuleDto;
import org.smart_elder_system.common.dto.admission.FamilyVisitSlotDto;
import org.smart_elder_system.common.dto.admission.ServiceApplicationDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdmissionServiceTest {

    @Mock
    private ServiceApplicationRepository serviceApplicationRepository;

    @Mock
    private FamilyVisitSlotRepository familyVisitSlotRepository;

    @Mock
    private FamilyVisitReservationRepository familyVisitReservationRepository;

    @Mock
    private AdmissionAuthorizationPolicy admissionAuthorizationPolicy;

    @Mock
    private FamilyVisitReservationRules familyVisitReservationRules;

    @Mock
    private ServiceJourneyTaskService serviceJourneyTaskService;

    @Mock
    private ServiceJourneyTransitionLogService serviceJourneyTransitionLogService;

    @InjectMocks
    private AdmissionService admissionService;

    @Test
    void shouldSetActiveFlagWhenSubmittingApplication() {
        ServiceApplicationDto request = new ServiceApplicationDto();
        request.setElderId(1001L);
        request.setGuardianId(2001L);
        request.setApplicantName("张三");
        request.setContactPhone("13800000000");
        request.setServiceScene("HOME");
        request.setServiceRequest("助餐");

        when(serviceApplicationRepository.save(any())).thenAnswer(invocation -> {
            ServiceApplicationPo po = invocation.getArgument(0);
            po.setId(1L);
            return po;
        });

        ServiceApplicationDto result = admissionService.submitApplication(request);

        assertEquals(1L, result.getApplicationId());
        ArgumentCaptor<ServiceApplicationPo> captor = ArgumentCaptor.forClass(ServiceApplicationPo.class);
        verify(serviceApplicationRepository).save(captor.capture());
        assertEquals(1, captor.getValue().getActiveFlag());
    }

    @Test
    void shouldClearActiveFlagWhenAssessmentFails() {
        ServiceApplicationPo po = applicationPo(1L, org.smart_elder_system.admission.vo.ServiceApplication.STATUS_SUBMITTED, 1);
        when(serviceApplicationRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(po));
        when(serviceApplicationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EligibilityAssessmentDto assessment = new EligibilityAssessmentDto();
        assessment.setApplicationId(1L);
        assessment.setEligible(false);
        assessment.setAssessmentConclusion("不符合条件");
        assessment.setAssessor("tester");

        ServiceApplicationDto result = admissionService.assessEligibility(assessment);

        assertEquals(org.smart_elder_system.admission.vo.ServiceApplication.STATUS_FAILED, result.getStatus());
        org.junit.jupiter.api.Assertions.assertNull(po.getActiveFlag());
    }

    @Test
    void shouldKeepActiveFlagWhenRevertingToAssessment() {
        ServiceApplicationPo po = applicationPo(1L, org.smart_elder_system.admission.vo.ServiceApplication.STATUS_PASSED, 1);
        when(serviceApplicationRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(po));
        when(serviceApplicationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceApplicationDto result = admissionService.revertToAssessment(1L, "补资料");

        assertEquals(org.smart_elder_system.admission.vo.ServiceApplication.STATUS_ASSESSED, result.getStatus());
        assertEquals(1, po.getActiveFlag());
    }

    @Test
    void shouldClearActiveFlagWhenWithdrawingApplication() {
        ServiceApplicationPo po = applicationPo(1L, org.smart_elder_system.admission.vo.ServiceApplication.STATUS_PASSED, 1);
        when(serviceApplicationRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(po));
        when(serviceApplicationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceApplicationDto result = admissionService.withdrawApplication(1L, "主动撤回");

        assertEquals(org.smart_elder_system.admission.vo.ServiceApplication.STATUS_WITHDRAWN, result.getStatus());
        org.junit.jupiter.api.Assertions.assertNull(po.getActiveFlag());
    }

    @Test
    void shouldFilterExpiredSlotsWhenListingTodaySlots() {
        LocalDate today = LocalDate.now();
        FamilyVisitSlotPo expiredSlot = familyVisitSlotPo(1L, today, LocalTime.now().minusMinutes(30), LocalTime.now().plusMinutes(30), 6, 1);
        FamilyVisitSlotPo futureSlot = familyVisitSlotPo(2L, today, LocalTime.now().plusMinutes(30), LocalTime.now().plusHours(1), 6, 1);

        when(familyVisitSlotRepository.findBySlotDateAndStatusOrderByStartTimeAsc(eq(today), eq(FamilyVisitSlot.STATUS_OPEN)))
                .thenReturn(List.of(expiredSlot, futureSlot));
        when(familyVisitReservationRules.isSlotReservable(any(), any()))
                .thenAnswer(invocation -> {
                    FamilyVisitSlot slot = invocation.getArgument(0);
                    return Long.valueOf(2L).equals(slot.getSlotId());
                });

        List<FamilyVisitSlotDto> result = admissionService.listFamilyVisitSlots(today);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getSlotId());
        assertTrue(result.stream().noneMatch(slot -> Long.valueOf(1L).equals(slot.getSlotId())));
    }

    @Test
    void shouldKeepFutureDatesEvenIfTodayHasExpiredSlots() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        FamilyVisitSlotPo expiredTodaySlot = familyVisitSlotPo(1L, today, LocalTime.now().minusMinutes(30), LocalTime.now().plusMinutes(30), 6, 1);
        FamilyVisitSlotPo tomorrowSlot = familyVisitSlotPo(2L, tomorrow, LocalTime.of(9, 0), LocalTime.of(10, 0), 6, 6);

        when(familyVisitSlotRepository.findBySlotDateGreaterThanEqualAndStatusOrderBySlotDateAscStartTimeAsc(eq(today), eq(FamilyVisitSlot.STATUS_OPEN)))
                .thenReturn(List.of(expiredTodaySlot, tomorrowSlot));
        when(familyVisitReservationRules.isSlotReservable(any(), any()))
                .thenAnswer(invocation -> {
                    FamilyVisitSlot slot = invocation.getArgument(0);
                    return Long.valueOf(2L).equals(slot.getSlotId());
                });

        List<FamilyVisitSlotDto> result = admissionService.listFamilyVisitSlots(null);

        assertEquals(1, result.size());
        assertEquals(tomorrow, result.get(0).getSlotDate());
        assertFalse(result.stream().anyMatch(slot -> today.equals(slot.getSlotDate())));
    }

    @Test
    void shouldRejectDuplicateFamilyVisitReservationBeforeLockingSlot() {
        when(admissionAuthorizationPolicy.requireCurrentUserId()).thenReturn(3001L);
        when(familyVisitReservationRepository.existsBySlotIdAndFamilyUserIdAndElderId(11L, 3001L, 1001L)).thenReturn(true);

        FamilyVisitReservationDto request = familyVisitReservationRequest(11L, 1001L);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> admissionService.createFamilyVisitReservation(request));

        assertEquals("当前家属已预约该老人此参观时段", exception.getMessage());
        verify(familyVisitSlotRepository, never()).findByIdForUpdate(any());
        verify(serviceJourneyTaskService, never()).createFamilyVisitReviewTask(any(), any());
        verify(serviceJourneyTransitionLogService, never()).logBusinessAction(any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldCreateFamilyVisitReservationWhenNotDuplicated() {
        when(admissionAuthorizationPolicy.requireCurrentUserId()).thenReturn(3001L);
        when(admissionAuthorizationPolicy.requireCurrentUsername()).thenReturn("family1");
        when(familyVisitReservationRepository.existsBySlotIdAndFamilyUserIdAndElderId(11L, 3001L, 1001L)).thenReturn(false);

        FamilyVisitSlotPo slotPo = familyVisitSlotPo(11L, LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0), 3, 1);
        when(familyVisitSlotRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(slotPo));
        when(familyVisitReservationRepository.save(any())).thenAnswer(invocation -> {
            FamilyVisitReservationPo po = invocation.getArgument(0);
            po.setId(21L);
            return po;
        });
        when(familyVisitSlotRepository.findById(11L)).thenReturn(Optional.of(slotPo));

        FamilyVisitReservationDto request = familyVisitReservationRequest(11L, 1001L);

        FamilyVisitReservationDto result = admissionService.createFamilyVisitReservation(request);

        assertEquals(21L, result.getReservationId());
        assertEquals(2, slotPo.getReservedCount());
        verify(familyVisitSlotRepository).findByIdForUpdate(11L);
        verify(familyVisitReservationRepository).save(any());
        verify(serviceJourneyTaskService).createFamilyVisitReviewTask(21L, 1001L);
        verify(serviceJourneyTransitionLogService).logBusinessAction(
                eq("FAMILY_VISIT_RESERVATION"),
                eq(21L),
                eq(1001L),
                eq("SUBMIT"),
                eq("家属预约提交成功"),
                eq(request)
        );
    }

    @Test
    void shouldRejectSlotOutsideReservationRulesAfterLocking() {
        when(admissionAuthorizationPolicy.requireCurrentUserId()).thenReturn(3001L);
        when(admissionAuthorizationPolicy.requireCurrentUsername()).thenReturn("family1");
        when(familyVisitReservationRepository.existsBySlotIdAndFamilyUserIdAndElderId(11L, 3001L, 1001L)).thenReturn(false);

        FamilyVisitSlotPo slotPo = familyVisitSlotPo(11L, LocalDate.now().plusDays(1), LocalTime.of(12, 0), LocalTime.of(13, 0), 3, 1);
        when(familyVisitSlotRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(slotPo));
        org.mockito.Mockito.doThrow(new IllegalStateException("当前时段不符合预约规则"))
                .when(familyVisitReservationRules).validateSlotReservable(any(), any());

        FamilyVisitReservationDto request = familyVisitReservationRequest(11L, 1001L);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> admissionService.createFamilyVisitReservation(request));

        assertEquals("当前时段不符合预约规则", exception.getMessage());
        verify(familyVisitReservationRepository, never()).save(any());
        verify(serviceJourneyTaskService, never()).createFamilyVisitReviewTask(any(), any());
        verify(serviceJourneyTransitionLogService, never()).logBusinessAction(any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldReturnFamilyVisitReservationRules() {
        FamilyVisitReservationRuleDto rules = new FamilyVisitReservationRuleDto(
                1,
                7,
                true,
                "08:00",
                "17:00",
                60,
                List.of("12:00-13:00"),
                List.of(1, 2, 3, 4, 5)
        );
        when(familyVisitReservationRules.describe()).thenReturn(rules);

        FamilyVisitReservationRuleDto result = admissionService.getFamilyVisitReservationRules();

        assertEquals(1, result.minAdvanceDays());
        assertEquals(7, result.maxWorkingDaysAhead());
        verify(admissionAuthorizationPolicy).requireSlotReadPermission();
    }

    @Test
    void shouldApproveFamilyVisitReservationAndWriteTaskLog() {
        when(admissionAuthorizationPolicy.requireCurrentUsername()).thenReturn("medic1");
        FamilyVisitSlotPo slotPo = familyVisitSlotPo(21L, LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0), 3, 1);
        when(familyVisitSlotRepository.findById(21L)).thenReturn(Optional.of(slotPo));

        FamilyVisitReservationPo reservationPo = familyVisitReservationPo(31L, 21L, 1001L, org.smart_elder_system.admission.vo.FamilyVisitReservation.STATUS_PENDING);
        when(familyVisitReservationRepository.findByIdForUpdate(31L)).thenReturn(Optional.of(reservationPo));
        when(familyVisitReservationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        FamilyVisitReservationReviewDto reviewDto = new FamilyVisitReservationReviewDto();
        reviewDto.setReviewComment("同意预约");

        FamilyVisitReservationDto result = admissionService.approveFamilyVisitReservation(31L, reviewDto);

        assertEquals(org.smart_elder_system.admission.vo.FamilyVisitReservation.STATUS_APPROVED, result.getStatus());
        assertEquals("medic1", result.getReviewedBy());
        assertEquals("同意预约", result.getReviewComment());
        verify(serviceJourneyTaskService).completeOpenTask(31L, ServiceJourneyTaskService.TASK_TYPE_FAMILY_VISIT_REVIEW);
        verify(serviceJourneyTransitionLogService).logBusinessAction(
                eq("FAMILY_VISIT_RESERVATION"),
                eq(31L),
                eq(1001L),
                eq("APPROVE"),
                eq("同意预约"),
                eq(reviewDto)
        );
    }

    @Test
    void shouldRejectFamilyVisitReservationAndReleaseSlot() {
        when(admissionAuthorizationPolicy.requireCurrentUsername()).thenReturn("medic1");
        FamilyVisitSlotPo slotPo = familyVisitSlotPo(22L, LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0), 3, 1);
        when(familyVisitSlotRepository.findByIdForUpdate(22L)).thenReturn(Optional.of(slotPo));
        when(familyVisitSlotRepository.findById(22L)).thenReturn(Optional.of(slotPo));

        FamilyVisitReservationPo reservationPo = familyVisitReservationPo(32L, 22L, 1002L, org.smart_elder_system.admission.vo.FamilyVisitReservation.STATUS_PENDING);
        when(familyVisitReservationRepository.findByIdForUpdate(32L)).thenReturn(Optional.of(reservationPo));
        when(familyVisitReservationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        FamilyVisitReservationReviewDto reviewDto = new FamilyVisitReservationReviewDto();
        reviewDto.setReviewComment("暂不方便接待");

        FamilyVisitReservationDto result = admissionService.rejectFamilyVisitReservation(32L, reviewDto);

        assertEquals(org.smart_elder_system.admission.vo.FamilyVisitReservation.STATUS_REJECTED, result.getStatus());
        assertEquals("medic1", result.getReviewedBy());
        assertEquals("暂不方便接待", result.getReviewComment());
        assertEquals(0, slotPo.getReservedCount());
        verify(serviceJourneyTaskService).completeOpenTask(32L, ServiceJourneyTaskService.TASK_TYPE_FAMILY_VISIT_REVIEW);
        verify(serviceJourneyTransitionLogService).logBusinessAction(
                eq("FAMILY_VISIT_RESERVATION"),
                eq(32L),
                eq(1002L),
                eq("REJECT"),
                eq("暂不方便接待"),
                eq(reviewDto)
        );
    }

    @Test
    void shouldRejectRepeatedApprovalOfFamilyVisitReservation() {
        when(admissionAuthorizationPolicy.requireCurrentUsername()).thenReturn("medic1");

        FamilyVisitReservationPo reservationPo = familyVisitReservationPo(33L, 11L, 1001L, org.smart_elder_system.admission.vo.FamilyVisitReservation.STATUS_APPROVED);
        when(familyVisitReservationRepository.findByIdForUpdate(33L)).thenReturn(Optional.of(reservationPo));

        FamilyVisitReservationReviewDto reviewDto = new FamilyVisitReservationReviewDto();
        reviewDto.setReviewComment("已审核");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> admissionService.approveFamilyVisitReservation(33L, reviewDto));

        assertEquals("当前预约状态不允许审核", exception.getMessage());
        verify(familyVisitReservationRepository, never()).save(any());
        verify(serviceJourneyTaskService, never()).completeOpenTask(any(), any());
        verify(serviceJourneyTransitionLogService, never()).logBusinessAction(any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldRejectRepeatedRejectionOfFamilyVisitReservationWithoutReleasingSlotAgain() {
        when(admissionAuthorizationPolicy.requireCurrentUsername()).thenReturn("medic1");

        FamilyVisitReservationPo reservationPo = familyVisitReservationPo(34L, 12L, 1002L, org.smart_elder_system.admission.vo.FamilyVisitReservation.STATUS_REJECTED);
        when(familyVisitReservationRepository.findByIdForUpdate(34L)).thenReturn(Optional.of(reservationPo));

        FamilyVisitReservationReviewDto reviewDto = new FamilyVisitReservationReviewDto();
        reviewDto.setReviewComment("重复驳回");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> admissionService.rejectFamilyVisitReservation(34L, reviewDto));

        assertEquals("当前预约状态不允许审核", exception.getMessage());
        verify(familyVisitSlotRepository, never()).findByIdForUpdate(any());
        verify(familyVisitReservationRepository, never()).save(any());
        verify(serviceJourneyTaskService, never()).completeOpenTask(any(), any());
        verify(serviceJourneyTransitionLogService, never()).logBusinessAction(any(), any(), any(), any(), any(), any());
    }

    private ServiceApplicationPo applicationPo(Long id, String status, Integer activeFlag) {
        ServiceApplicationPo po = new ServiceApplicationPo();
        po.setId(id);
        po.setElderId(1001L);
        po.setGuardianId(2001L);
        po.setApplicantName("张三");
        po.setContactPhone("13800000000");
        po.setServiceScene("HOME");
        po.setServiceRequest("助餐");
        po.setStatus(status);
        po.setActiveFlag(activeFlag);
        return po;
    }

    private FamilyVisitReservationDto familyVisitReservationRequest(Long slotId, Long elderId) {
        FamilyVisitReservationDto request = new FamilyVisitReservationDto();
        request.setSlotId(slotId);
        request.setElderId(elderId);
        request.setVisitorName("张三");
        request.setVisitorPhone("13800000000");
        request.setRelationToElder("儿子");
        request.setVisitPurpose("探望");
        return request;
    }

    private FamilyVisitReservationPo familyVisitReservationPo(Long id, Long slotId, Long elderId, String status) {
        FamilyVisitReservationPo po = new FamilyVisitReservationPo();
        po.setId(id);
        po.setSlotId(slotId);
        po.setElderId(elderId);
        po.setFamilyUserId(3001L);
        po.setFamilyUsername("family1");
        po.setVisitorName("张三");
        po.setVisitorPhone("13800000000");
        po.setRelationToElder("儿子");
        po.setVisitPurpose("探望");
        po.setStatus(status);
        return po;
    }

    private FamilyVisitSlotPo familyVisitSlotPo(
            Long id,
            LocalDate slotDate,
            LocalTime startTime,
            LocalTime endTime,
            Integer capacity,
            Integer reservedCount) {
        FamilyVisitSlotPo po = new FamilyVisitSlotPo();
        po.setId(id);
        po.setSlotDate(slotDate);
        po.setStartTime(startTime);
        po.setEndTime(endTime);
        po.setCapacity(capacity);
        po.setReservedCount(reservedCount);
        po.setStatus(FamilyVisitSlot.STATUS_OPEN);
        return po;
    }
}
