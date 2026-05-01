package org.smart_elder_system.admission.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.smart_elder_system.admission.po.ServiceApplicationPo;
import org.smart_elder_system.common.dto.care.ServiceApplicationDTO;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceApplication {

    public static final String STATUS_SUBMITTED = "SUBMITTED";
    public static final String STATUS_ASSESSED = "ASSESSED";
    public static final String STATUS_PASSED = "PASSED";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_WITHDRAWN = "WITHDRAWN";

    private Long applicationId;
    private Long elderId;
    private Long guardianId;
    private String applicantName;
    private String contactPhone;
    private String serviceScene;
    private String serviceRequest;
    private String status;
    private LocalDateTime intakeAt;
    private LocalDateTime submittedAt;
    private LocalDateTime assessedAt;
    private String assessmentConclusion;

    public static ServiceApplication fromDTO(ServiceApplicationDTO dto) {
        return ServiceApplication.builder()
                .applicationId(dto.getApplicationId())
                .elderId(dto.getElderId())
                .guardianId(dto.getGuardianId())
                .applicantName(dto.getApplicantName())
                .contactPhone(dto.getContactPhone())
                .serviceScene(dto.getServiceScene())
                .serviceRequest(dto.getServiceRequest())
                .status(dto.getStatus())
                .intakeAt(dto.getIntakeAt())
                .submittedAt(dto.getSubmittedAt())
                .assessedAt(dto.getAssessedAt())
                .build();
    }

    public static ServiceApplication fromPo(ServiceApplicationPo po) {
        return ServiceApplication.builder()
                .applicationId(po.getId())
                .elderId(po.getElderId())
                .guardianId(po.getGuardianId())
                .applicantName(po.getApplicantName())
                .contactPhone(po.getContactPhone())
                .serviceScene(po.getServiceScene())
                .serviceRequest(po.getServiceRequest())
                .status(po.getStatus())
                .intakeAt(po.getIntakeAt())
                .submittedAt(po.getSubmittedAt())
                .assessedAt(po.getAssessedAt())
                .assessmentConclusion(po.getAssessmentConclusion())
                .build();
    }

    public ServiceApplicationDTO toDTO() {
        return ServiceApplicationDTO.builder()
                .applicationId(applicationId)
                .elderId(elderId)
                .guardianId(guardianId)
                .applicantName(applicantName)
                .contactPhone(contactPhone)
                .serviceScene(serviceScene)
                .serviceRequest(serviceRequest)
                .status(status)
                .intakeAt(intakeAt)
                .submittedAt(submittedAt)
                .assessedAt(assessedAt)
                .build();
    }

    public ServiceApplicationPo toPo() {
        return ServiceApplicationPo.builder()
                .id(applicationId)
                .elderId(elderId)
                .guardianId(guardianId)
                .applicantName(applicantName)
                .contactPhone(contactPhone)
                .serviceScene(serviceScene)
                .serviceRequest(serviceRequest)
                .status(status)
                .intakeAt(intakeAt)
                .submittedAt(submittedAt)
                .assessedAt(assessedAt)
                .assessmentConclusion(assessmentConclusion)
                .build();
    }

    public void applyTo(ServiceApplicationPo po) {
        po.setId(applicationId);
        po.setElderId(elderId);
        po.setGuardianId(guardianId);
        po.setApplicantName(applicantName);
        po.setContactPhone(contactPhone);
        po.setServiceScene(serviceScene);
        po.setServiceRequest(serviceRequest);
        po.setStatus(status);
        po.setIntakeAt(intakeAt);
        po.setSubmittedAt(submittedAt);
        po.setAssessedAt(assessedAt);
        po.setAssessmentConclusion(assessmentConclusion);
    }

    public void submit() {
        LocalDateTime now = LocalDateTime.now();
        this.intakeAt = now;
        this.status = STATUS_SUBMITTED;
        this.submittedAt = now;
    }

    public void assess(EligibilityAssessment assessment) {
        this.assessedAt = assessment.getAssessedAt() == null ? LocalDateTime.now() : assessment.getAssessedAt();
        this.assessmentConclusion = assessment.getAssessmentConclusion();
        this.status = STATUS_ASSESSED;

        if (Boolean.TRUE.equals(assessment.getEligible())) {
            this.status = STATUS_PASSED;
            return;
        }

        this.status = STATUS_FAILED;
    }

    public void withdraw(String reason) {
        if (STATUS_FAILED.equals(this.status) || STATUS_WITHDRAWN.equals(this.status)) {
            throw new IllegalStateException("当前申请状态不允许撤回");
        }
        this.status = STATUS_WITHDRAWN;
        this.assessmentConclusion = reason;
    }

    public void revertToAssessment(String reason) {
        if (!STATUS_PASSED.equals(this.status)) {
            throw new IllegalStateException("当前申请状态不允许退回需求评估");
        }
        this.status = STATUS_ASSESSED;
        this.assessmentConclusion = reason;
    }

    public boolean isEligible() {
        return STATUS_PASSED.equals(this.status);
    }
}
