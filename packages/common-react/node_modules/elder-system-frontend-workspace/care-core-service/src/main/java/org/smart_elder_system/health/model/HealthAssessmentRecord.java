package org.smart_elder_system.health.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.smart_elder_system.common.dto.care.HealthAssessmentDTO;
import org.smart_elder_system.health.po.HealthAssessmentRecordPo;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthAssessmentRecord {

    private Long assessmentId;
    private Long applicationId;
    private Long elderId;
    private Long agreementId;
    private String assessmentType;
    private String conclusion;
    private Integer score;
    private LocalDateTime assessedAt;

    public static HealthAssessmentRecord fromDTO(HealthAssessmentDTO dto) {
        return HealthAssessmentRecord.builder()
                .assessmentId(dto.getAssessmentId())
                .elderId(dto.getElderId())
                .agreementId(dto.getAgreementId())
                .assessmentType(dto.getAssessmentType())
                .conclusion(dto.getConclusion())
                .score(dto.getScore())
                .assessedAt(dto.getAssessedAt())
                .build();
    }

    public static HealthAssessmentRecord fromPo(HealthAssessmentRecordPo po) {
        return HealthAssessmentRecord.builder()
                .assessmentId(po.getId())
                .applicationId(po.getApplicationId())
                .elderId(po.getElderId())
                .agreementId(po.getAgreementId())
                .assessmentType(po.getAssessmentType())
                .conclusion(po.getConclusion())
                .score(po.getScore())
                .assessedAt(po.getAssessedAt())
                .build();
    }

    public HealthAssessmentDTO toDTO() {
        return HealthAssessmentDTO.builder()
                .assessmentId(assessmentId)
                .elderId(elderId)
                .agreementId(agreementId)
                .assessmentType(assessmentType)
                .conclusion(conclusion)
                .score(score)
                .assessedAt(assessedAt)
                .build();
    }

    public HealthAssessmentRecordPo toPo() {
        return HealthAssessmentRecordPo.builder()
                .id(assessmentId)
                .applicationId(applicationId)
                .elderId(elderId)
                .agreementId(agreementId)
                .assessmentType(assessmentType)
                .conclusion(conclusion)
                .score(score)
                .assessedAt(assessedAt)
                .build();
    }

    public void applyTo(HealthAssessmentRecordPo po) {
        po.setId(assessmentId);
        po.setApplicationId(applicationId);
        po.setElderId(elderId);
        po.setAgreementId(agreementId);
        po.setAssessmentType(assessmentType);
        po.setConclusion(conclusion);
        po.setScore(score);
        po.setAssessedAt(assessedAt);
    }

    public void assess() {
        this.assessedAt = LocalDateTime.now();
    }
}
