package org.smart_elder_system.quality.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.smart_elder_system.common.dto.quality.CaregiverQualificationApplicationDto;
import org.smart_elder_system.quality.po.CaregiverQualificationApplicationPo;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaregiverQualificationApplication {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    private Long applicationId;
    private Long caregiverUserId;
    private String caregiverUsername;
    private String realName;
    private String phone;
    private String idCardNo;
    private String certificateNo;
    private String certificateType;
    private Integer yearsOfExperience;
    private String skillSummary;
    private String status;
    private String reviewedBy;
    private String reviewComment;
    private LocalDateTime reviewedAt;
    private Integer activeFlag;
    private LocalDateTime createdAt;

    public static CaregiverQualificationApplication fromDto(CaregiverQualificationApplicationDto dto) {
        return CaregiverQualificationApplication.builder()
                .applicationId(dto.getApplicationId())
                .caregiverUserId(dto.getCaregiverUserId())
                .caregiverUsername(dto.getCaregiverUsername())
                .realName(dto.getRealName())
                .phone(dto.getPhone())
                .idCardNo(dto.getIdCardNo())
                .certificateNo(dto.getCertificateNo())
                .certificateType(dto.getCertificateType())
                .yearsOfExperience(dto.getYearsOfExperience())
                .skillSummary(dto.getSkillSummary())
                .status(dto.getStatus())
                .reviewedBy(dto.getReviewedBy())
                .reviewComment(dto.getReviewComment())
                .reviewedAt(dto.getReviewedAt())
                .createdAt(dto.getCreatedAt())
                .build();
    }

    public static CaregiverQualificationApplication fromPo(CaregiverQualificationApplicationPo po) {
        return CaregiverQualificationApplication.builder()
                .applicationId(po.getId())
                .caregiverUserId(po.getCaregiverUserId())
                .caregiverUsername(po.getCaregiverUsername())
                .realName(po.getRealName())
                .phone(po.getPhone())
                .idCardNo(po.getIdCardNo())
                .certificateNo(po.getCertificateNo())
                .certificateType(po.getCertificateType())
                .yearsOfExperience(po.getYearsOfExperience())
                .skillSummary(po.getSkillSummary())
                .status(po.getStatus())
                .reviewedBy(po.getReviewedBy())
                .reviewComment(po.getReviewComment())
                .reviewedAt(po.getReviewedAt())
                .activeFlag(po.getActiveFlag())
                .createdAt(po.getCreatedDateTimeUtc())
                .build();
    }

    public CaregiverQualificationApplicationDto toDto() {
        return CaregiverQualificationApplicationDto.builder()
                .applicationId(applicationId)
                .caregiverUserId(caregiverUserId)
                .caregiverUsername(caregiverUsername)
                .realName(realName)
                .phone(phone)
                .idCardNo(idCardNo)
                .certificateNo(certificateNo)
                .certificateType(certificateType)
                .yearsOfExperience(yearsOfExperience)
                .skillSummary(skillSummary)
                .status(status)
                .reviewedBy(reviewedBy)
                .reviewComment(reviewComment)
                .reviewedAt(reviewedAt)
                .createdAt(createdAt)
                .build();
    }

    public void submit() {
        this.status = STATUS_PENDING;
        this.activeFlag = 1;
    }

    public void approve(String reviewer, String comment) {
        ensurePending();
        this.status = STATUS_APPROVED;
        this.reviewedBy = reviewer;
        this.reviewComment = comment;
        this.reviewedAt = LocalDateTime.now();
        this.activeFlag = null;
    }

    public void reject(String reviewer, String comment) {
        ensurePending();
        this.status = STATUS_REJECTED;
        this.reviewedBy = reviewer;
        this.reviewComment = comment;
        this.reviewedAt = LocalDateTime.now();
        this.activeFlag = null;
    }

    public void applyTo(CaregiverQualificationApplicationPo po) {
        po.setId(applicationId);
        po.setCaregiverUserId(caregiverUserId);
        po.setCaregiverUsername(caregiverUsername);
        po.setRealName(realName);
        po.setPhone(phone);
        po.setIdCardNo(idCardNo);
        po.setCertificateNo(certificateNo);
        po.setCertificateType(certificateType);
        po.setYearsOfExperience(yearsOfExperience);
        po.setSkillSummary(skillSummary);
        po.setStatus(status);
        po.setReviewedBy(reviewedBy);
        po.setReviewComment(reviewComment);
        po.setReviewedAt(reviewedAt);
        po.setActiveFlag(activeFlag);
    }

    private void ensurePending() {
        if (!STATUS_PENDING.equals(status)) {
            throw new IllegalStateException("当前资质申请状态不允许审核");
        }
    }
}
