package org.smart_elder_system.admission.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.smart_elder_system.admission.AdmissionAuthorizationPolicy;
import org.smart_elder_system.admission.vo.EligibilityAssessment;
import org.smart_elder_system.admission.vo.FamilyVisitReservation;
import org.smart_elder_system.admission.vo.FamilyVisitSlot;
import org.smart_elder_system.admission.vo.ServiceApplication;
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
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdmissionService {

    private final ServiceApplicationRepository serviceApplicationRepository;
    private final FamilyVisitSlotRepository familyVisitSlotRepository;
    private final FamilyVisitReservationRepository familyVisitReservationRepository;
    private final AdmissionAuthorizationPolicy admissionAuthorizationPolicy;
    private final FamilyVisitReservationRules familyVisitReservationRules;
    private final ServiceJourneyTaskService serviceJourneyTaskService;
    private final ServiceJourneyTransitionLogService serviceJourneyTransitionLogService;

    public String getModuleScope() {
        return "准入模块：负责服务申请、接待登记、准入评估与家属预约参观";
    }

    public FamilyVisitReservationRuleDto getFamilyVisitReservationRules() {
        admissionAuthorizationPolicy.requireSlotReadPermission();
        return familyVisitReservationRules.describe();
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceApplicationDto submitApplication(ServiceApplicationDto applicationDto) {
        ServiceApplication domain = ServiceApplication.fromDto(applicationDto);
        domain.submit();

        ServiceApplicationPo po = domain.toPo();
        po.setActiveFlag(resolveActiveFlag(domain.getStatus()));
        ServiceApplicationPo saved = serviceApplicationRepository.save(po);
        return ServiceApplication.fromPo(saved).toDto();
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceApplicationDto assessEligibility(EligibilityAssessmentDto assessmentDto) {
        ServiceApplicationPo po = serviceApplicationRepository.findByIdForUpdate(assessmentDto.getApplicationId())
                .orElseThrow(() -> new IllegalArgumentException("未找到服务申请"));

        ServiceApplication domain = ServiceApplication.fromPo(po);

        EligibilityAssessment assessment = new EligibilityAssessment();
        assessment.setEligible(assessmentDto.getEligible());
        assessment.setAssessmentConclusion(assessmentDto.getAssessmentConclusion());
        assessment.setAssessor(assessmentDto.getAssessor());
        assessment.setAssessedAt(assessmentDto.getAssessedAt());

        domain.assess(assessment);
        domain.applyTo(po);
        po.setActiveFlag(resolveActiveFlag(domain.getStatus()));
        ServiceApplicationPo saved = serviceApplicationRepository.save(po);

        return ServiceApplication.fromPo(saved).toDto();
    }

    @Transactional(readOnly = true)
    public ServiceApplicationDto getApplication(Long applicationId) {
        ServiceApplicationPo po = serviceApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("未找到服务申请"));
        return ServiceApplication.fromPo(po).toDto();
    }

    @Transactional(readOnly = true)
    public List<ServiceApplicationDto> listApplicationsByStatus(String status) {
        return serviceApplicationRepository.findByStatusOrderBySubmittedAtAsc(status).stream()
                .map(ServiceApplication::fromPo)
                .map(ServiceApplication::toDto)
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceApplicationDto withdrawApplication(Long applicationId, String reason) {
        ServiceApplicationPo po = serviceApplicationRepository.findByIdForUpdate(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("未找到服务申请"));

        ServiceApplication domain = ServiceApplication.fromPo(po);
        domain.withdraw(reason);
        domain.applyTo(po);
        po.setActiveFlag(resolveActiveFlag(domain.getStatus()));
        ServiceApplicationPo saved = serviceApplicationRepository.save(po);
        return ServiceApplication.fromPo(saved).toDto();
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceApplicationDto revertToAssessment(Long applicationId, String reason) {
        ServiceApplicationPo po = serviceApplicationRepository.findByIdForUpdate(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("未找到服务申请"));

        ServiceApplication domain = ServiceApplication.fromPo(po);
        domain.revertToAssessment(reason);
        domain.applyTo(po);
        po.setActiveFlag(resolveActiveFlag(domain.getStatus()));
        ServiceApplicationPo saved = serviceApplicationRepository.save(po);
        return ServiceApplication.fromPo(saved).toDto();
    }

    @Transactional(readOnly = true)
    public List<FamilyVisitSlotDto> listFamilyVisitSlots(LocalDate slotDate) {
        admissionAuthorizationPolicy.requireSlotReadPermission();
        LocalDate today = LocalDate.now();
        List<FamilyVisitSlotPo> slotPos = slotDate == null
                ? familyVisitSlotRepository.findBySlotDateGreaterThanEqualAndStatusOrderBySlotDateAscStartTimeAsc(today, FamilyVisitSlot.STATUS_OPEN)
                : familyVisitSlotRepository.findBySlotDateAndStatusOrderByStartTimeAsc(slotDate, FamilyVisitSlot.STATUS_OPEN);
        LocalDateTime now = LocalDateTime.now();
        return slotPos.stream()
                .map(FamilyVisitSlot::fromPo)
                .filter(slot -> familyVisitReservationRules.isSlotReservable(slot, now))
                .map(FamilyVisitSlot::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FamilyVisitReservationDto> listMyFamilyVisitReservations() {
        admissionAuthorizationPolicy.requireMyReservationListPermission();
        Long userId = admissionAuthorizationPolicy.requireCurrentUserId();
        return familyVisitReservationRepository.findByFamilyUserIdOrderByCreatedDateTimeUtcDesc(userId).stream()
                .map(this::toReservationDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FamilyVisitReservationDto> listFamilyVisitReservations(String status) {
        admissionAuthorizationPolicy.requireReservationListPermission();
        String actualStatus = (status == null || status.isBlank()) ? FamilyVisitReservation.STATUS_PENDING : status;
        return familyVisitReservationRepository.findByStatusOrderByCreatedDateTimeUtcDesc(actualStatus).stream()
                .map(this::toReservationDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public FamilyVisitReservationDto getFamilyVisitReservationDetail(Long reservationId) {
        admissionAuthorizationPolicy.requireReservationDetailPermission();
        return familyVisitReservationRepository.findById(reservationId)
                .map(this::toReservationDto)
                .orElseThrow(() -> new IllegalArgumentException("未找到家属预约记录"));
    }

    @Transactional(rollbackFor = Exception.class)
    public FamilyVisitReservationDto createFamilyVisitReservation(FamilyVisitReservationDto reservationDto) {
        admissionAuthorizationPolicy.requireReservationCreatePermission();

        Long currentUserId = admissionAuthorizationPolicy.requireCurrentUserId();
        String currentUsername = admissionAuthorizationPolicy.requireCurrentUsername();
        validateDuplicateFamilyVisitReservation(reservationDto.getSlotId(), currentUserId, reservationDto.getElderId());

        FamilyVisitSlotPo slotPo = familyVisitSlotRepository.findByIdForUpdate(reservationDto.getSlotId())
                .orElseThrow(() -> new IllegalArgumentException("未找到预约时段"));
        FamilyVisitSlot slot = FamilyVisitSlot.fromPo(slotPo);
        familyVisitReservationRules.validateSlotReservable(slot, LocalDateTime.now());
        slot.reserve();
        slot.applyTo(slotPo);

        FamilyVisitReservation reservation = FamilyVisitReservation.fromDto(reservationDto);
        reservation.setFamilyUserId(currentUserId);
        reservation.setFamilyUsername(currentUsername);
        reservation.submit();
        FamilyVisitReservationPo saved = familyVisitReservationRepository.save(toPo(reservation));

        serviceJourneyTaskService.createFamilyVisitReviewTask(saved.getId(), reservation.getElderId());
        serviceJourneyTransitionLogService.logBusinessAction(
                "FAMILY_VISIT_RESERVATION",
                saved.getId(),
                reservation.getElderId(),
                "SUBMIT",
                "家属预约提交成功",
                reservationDto);
        return toReservationDto(saved);
    }

    private void validateDuplicateFamilyVisitReservation(Long slotId, Long familyUserId, Long elderId) {
        if (familyVisitReservationRepository.existsBySlotIdAndFamilyUserIdAndElderId(slotId, familyUserId, elderId)) {
            throw new IllegalArgumentException("当前家属已预约该老人此参观时段");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public FamilyVisitReservationDto approveFamilyVisitReservation(Long reservationId, FamilyVisitReservationReviewDto reviewDto) {
        admissionAuthorizationPolicy.requireReservationApprovePermission();
        FamilyVisitReservationPo po = familyVisitReservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("未找到家属预约记录"));
        FamilyVisitReservation reservation = FamilyVisitReservation.fromPo(po);
        String comment = reviewDto == null ? null : reviewDto.getReviewComment();
        reservation.approve(admissionAuthorizationPolicy.requireCurrentUsername(), comment);
        reservation.applyTo(po);
        FamilyVisitReservationPo saved = familyVisitReservationRepository.save(po);
        serviceJourneyTaskService.completeOpenTask(saved.getId(), ServiceJourneyTaskService.TASK_TYPE_FAMILY_VISIT_REVIEW);
        serviceJourneyTransitionLogService.logBusinessAction(
                "FAMILY_VISIT_RESERVATION",
                saved.getId(),
                reservation.getElderId(),
                "APPROVE",
                comment,
                reviewDto);
        return toReservationDto(saved);
    }

    @Transactional(rollbackFor = Exception.class)
    public FamilyVisitReservationDto rejectFamilyVisitReservation(Long reservationId, FamilyVisitReservationReviewDto reviewDto) {
        admissionAuthorizationPolicy.requireReservationRejectPermission();
        FamilyVisitReservationPo po = familyVisitReservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("未找到家属预约记录"));
        FamilyVisitReservation reservation = FamilyVisitReservation.fromPo(po);

        String comment = reviewDto == null ? null : reviewDto.getReviewComment();
        reservation.reject(admissionAuthorizationPolicy.requireCurrentUsername(), comment);

        FamilyVisitSlotPo slotPo = familyVisitSlotRepository.findByIdForUpdate(reservation.getSlotId())
                .orElseThrow(() -> new IllegalArgumentException("未找到预约时段"));
        FamilyVisitSlot slot = FamilyVisitSlot.fromPo(slotPo);
        slot.release();
        slot.applyTo(slotPo);

        reservation.applyTo(po);
        FamilyVisitReservationPo saved = familyVisitReservationRepository.save(po);
        serviceJourneyTaskService.completeOpenTask(saved.getId(), ServiceJourneyTaskService.TASK_TYPE_FAMILY_VISIT_REVIEW);
        serviceJourneyTransitionLogService.logBusinessAction(
                "FAMILY_VISIT_RESERVATION",
                saved.getId(),
                reservation.getElderId(),
                "REJECT",
                comment,
                reviewDto);
        return toReservationDto(saved);
    }

    private FamilyVisitReservationDto toReservationDto(FamilyVisitReservationPo po) {
        FamilyVisitReservationDto dto = FamilyVisitReservation.fromPo(po).toDto();
        familyVisitSlotRepository.findById(po.getSlotId()).ifPresent(slotPo -> {
            dto.setSlotDate(slotPo.getSlotDate());
            dto.setStartTime(slotPo.getStartTime());
            dto.setEndTime(slotPo.getEndTime());
        });
        return dto;
    }

    private FamilyVisitReservationPo toPo(FamilyVisitReservation reservation) {
        FamilyVisitReservationPo po = new FamilyVisitReservationPo();
        reservation.applyTo(po);
        return po;
    }

    private Integer resolveActiveFlag(String status) {
        if (ServiceApplication.STATUS_FAILED.equals(status) || ServiceApplication.STATUS_WITHDRAWN.equals(status)) {
            return null;
        }
        return 1;
    }
}
