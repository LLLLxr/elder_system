package org.smart_elder_system.quality.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class QualityService {

    private final ServiceReviewRepository serviceReviewRepository;
    private final CaregiverQualificationApplicationRepository caregiverQualificationApplicationRepository;
    private final QualityAuthorizationPolicy qualityAuthorizationPolicy;
    private final ServiceJourneyTaskService serviceJourneyTaskService;
    private final ServiceJourneyTransitionLogService serviceJourneyTransitionLogService;

    public String getModuleScope() {
        return "质量模块：负责服务评价、护理员资质申请与审核";
    }

    public ServiceReviewDto reviewService(ServiceReviewDto reviewDto) {
        ServiceReview review = ServiceReview.fromDto(reviewDto);
        review.review();

        ServiceReviewPo saved = serviceReviewRepository.save(review.toPo());
        review.setReviewId(saved.getId());
        return review.toDto();
    }

    @Transactional(rollbackFor = Exception.class)
    public CaregiverQualificationApplicationDto submitCaregiverQualificationApplication(CaregiverQualificationApplicationDto dto) {
        qualityAuthorizationPolicy.requireQualificationCreatePermission();
        Long currentUserId = qualityAuthorizationPolicy.requireCurrentUserId();
        String currentUsername = qualityAuthorizationPolicy.requireCurrentUsername();

        List<CaregiverQualificationApplicationPo> existingApplications = caregiverQualificationApplicationRepository.findByCaregiverUserIdForUpdate(currentUserId);
        boolean hasPending = existingApplications.stream().anyMatch(item -> CaregiverQualificationApplication.STATUS_PENDING.equals(item.getStatus()));
        if (hasPending) {
            throw new IllegalArgumentException("当前护理员已有待审核的资质申请");
        }

        CaregiverQualificationApplication application = CaregiverQualificationApplication.fromDto(dto);
        application.setCaregiverUserId(currentUserId);
        application.setCaregiverUsername(currentUsername);
        application.submit();
        CaregiverQualificationApplicationPo saved = caregiverQualificationApplicationRepository.save(toPo(application));

        serviceJourneyTaskService.createCaregiverQualificationReviewTask(saved.getId(), saved.getCaregiverUserId());
        serviceJourneyTransitionLogService.logBusinessAction(
                "CAREGIVER_QUALIFICATION_APPLICATION",
                saved.getId(),
                null,
                "SUBMIT",
                "护理员资质申请提交成功",
                dto);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public CaregiverQualificationApplicationDto getMyCaregiverQualificationApplication() {
        qualityAuthorizationPolicy.requireMyQualificationListPermission();
        Long currentUserId = qualityAuthorizationPolicy.requireCurrentUserId();
        return caregiverQualificationApplicationRepository.findByCaregiverUserIdOrderByCreatedDateTimeUtcDesc(currentUserId).stream()
                .findFirst()
                .map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("未找到护理员资质申请记录"));
    }

    @Transactional(readOnly = true)
    public List<CaregiverQualificationApplicationDto> listMyCaregiverQualificationApplications() {
        qualityAuthorizationPolicy.requireMyQualificationListPermission();
        Long currentUserId = qualityAuthorizationPolicy.requireCurrentUserId();
        return caregiverQualificationApplicationRepository.findByCaregiverUserIdOrderByCreatedDateTimeUtcDesc(currentUserId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CaregiverQualificationApplicationDto> listCaregiverQualificationApplications(String status) {
        qualityAuthorizationPolicy.requireQualificationListPermission();
        if (status == null || status.isBlank()) {
            status = CaregiverQualificationApplication.STATUS_PENDING;
        }
        return caregiverQualificationApplicationRepository.findByStatusOrderByCreatedDateTimeUtcDesc(status).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CaregiverQualificationApplicationDto getCaregiverQualificationApplicationDetail(Long id) {
        qualityAuthorizationPolicy.requireQualificationDetailPermission();
        return caregiverQualificationApplicationRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("未找到护理员资质申请记录"));
    }

    @Transactional(rollbackFor = Exception.class)
    public CaregiverQualificationApplicationDto approveCaregiverQualificationApplication(Long id, CaregiverQualificationReviewDto reviewDto) {
        qualityAuthorizationPolicy.requireQualificationApprovePermission();
        CaregiverQualificationApplicationPo po = caregiverQualificationApplicationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("未找到护理员资质申请记录"));
        CaregiverQualificationApplication application = CaregiverQualificationApplication.fromPo(po);
        application.approve(qualityAuthorizationPolicy.requireCurrentUsername(), reviewDto == null ? null : reviewDto.getReviewComment());
        application.applyTo(po);
        CaregiverQualificationApplicationPo saved = caregiverQualificationApplicationRepository.save(po);
        serviceJourneyTaskService.completeOpenTask(saved.getId(), ServiceJourneyTaskService.TASK_TYPE_CAREGIVER_QUALIFICATION_REVIEW);
        serviceJourneyTransitionLogService.logBusinessAction(
                "CAREGIVER_QUALIFICATION_APPLICATION",
                saved.getId(),
                null,
                "APPROVE",
                reviewDto == null ? null : reviewDto.getReviewComment(),
                reviewDto);
        return toDto(saved);
    }

    @Transactional(rollbackFor = Exception.class)
    public CaregiverQualificationApplicationDto rejectCaregiverQualificationApplication(Long id, CaregiverQualificationReviewDto reviewDto) {
        qualityAuthorizationPolicy.requireQualificationRejectPermission();
        CaregiverQualificationApplicationPo po = caregiverQualificationApplicationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("未找到护理员资质申请记录"));
        CaregiverQualificationApplication application = CaregiverQualificationApplication.fromPo(po);
        application.reject(qualityAuthorizationPolicy.requireCurrentUsername(), reviewDto == null ? null : reviewDto.getReviewComment());
        application.applyTo(po);
        CaregiverQualificationApplicationPo saved = caregiverQualificationApplicationRepository.save(po);
        serviceJourneyTaskService.completeOpenTask(saved.getId(), ServiceJourneyTaskService.TASK_TYPE_CAREGIVER_QUALIFICATION_REVIEW);
        serviceJourneyTransitionLogService.logBusinessAction(
                "CAREGIVER_QUALIFICATION_APPLICATION",
                saved.getId(),
                null,
                "REJECT",
                reviewDto == null ? null : reviewDto.getReviewComment(),
                reviewDto);
        return toDto(saved);
    }

    private CaregiverQualificationApplicationDto toDto(CaregiverQualificationApplicationPo po) {
        return CaregiverQualificationApplication.fromPo(po).toDto();
    }

    private CaregiverQualificationApplicationPo toPo(CaregiverQualificationApplication application) {
        CaregiverQualificationApplicationPo po = new CaregiverQualificationApplicationPo();
        application.applyTo(po);
        return po;
    }
}
