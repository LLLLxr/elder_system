package org.smart_elder_system.health.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.smart_elder_system.common.dto.care.HealthCheckFormCreateRequestDTO;
import org.smart_elder_system.common.dto.care.HealthCheckFormDTO;
import org.smart_elder_system.health.po.HealthCheckFormPo;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckForm {

    private Long formId;
    private Long elderId;
    private Long authorUserId;
    private Long agreementId;
    private String elderName;
    private String formCode;
    private LocalDate checkDate;
    private String responsibleDoctor;
    private String formVersion;
    private String symptomSection;
    private String vitalSignSection;
    private String selfEvaluationSection;
    private String cognitiveEmotionSection;
    private String lifestyleSection;
    private String nursingConclusionSection;
    private String chronicDiseaseSummary;
    private String allergySummary;
    private String riskLevel;
    private Integer score;
    private String conclusion;

    public static HealthCheckForm fromCreateRequest(HealthCheckFormCreateRequestDTO dto) {
        return HealthCheckForm.builder()
                .elderId(dto.getElderId())
                .agreementId(dto.getAgreementId())
                .elderName(dto.getElderName())
                .formCode(dto.getFormCode())
                .checkDate(dto.getCheckDate())
                .responsibleDoctor(dto.getResponsibleDoctor())
                .formVersion(dto.getFormVersion())
                .symptomSection(dto.getSymptomSection())
                .vitalSignSection(dto.getVitalSignSection())
                .selfEvaluationSection(dto.getSelfEvaluationSection())
                .cognitiveEmotionSection(dto.getCognitiveEmotionSection())
                .lifestyleSection(dto.getLifestyleSection())
                .chronicDiseaseSummary(dto.getChronicDiseaseSummary())
                .allergySummary(dto.getAllergySummary())
                .build();
    }

    public static HealthCheckForm fromPo(HealthCheckFormPo po) {
        return HealthCheckForm.builder()
                .formId(po.getId())
                .elderId(po.getElderId())
                .authorUserId(po.getAuthorUserId())
                .agreementId(po.getAgreementId())
                .elderName(po.getElderName())
                .formCode(po.getFormCode())
                .checkDate(po.getCheckDate())
                .responsibleDoctor(po.getResponsibleDoctor())
                .formVersion(po.getFormVersion())
                .symptomSection(po.getSymptomSection())
                .vitalSignSection(po.getVitalSignSection())
                .selfEvaluationSection(po.getSelfEvaluationSection())
                .cognitiveEmotionSection(po.getCognitiveEmotionSection())
                .lifestyleSection(po.getLifestyleSection())
                .nursingConclusionSection(po.getNursingConclusionSection())
                .chronicDiseaseSummary(po.getChronicDiseaseSummary())
                .allergySummary(po.getAllergySummary())
                .riskLevel(po.getRiskLevel())
                .score(po.getScore())
                .conclusion(po.getConclusion())
                .build();
    }

    public HealthCheckFormDTO toDTO() {
        return HealthCheckFormDTO.builder()
                .formId(formId)
                .elderId(elderId)
                .authorUserId(authorUserId)
                .agreementId(agreementId)
                .elderName(elderName)
                .formCode(formCode)
                .checkDate(checkDate)
                .responsibleDoctor(responsibleDoctor)
                .formVersion(formVersion)
                .symptomSection(symptomSection)
                .vitalSignSection(vitalSignSection)
                .selfEvaluationSection(selfEvaluationSection)
                .cognitiveEmotionSection(cognitiveEmotionSection)
                .lifestyleSection(lifestyleSection)
                .chronicDiseaseSummary(chronicDiseaseSummary)
                .allergySummary(allergySummary)
                .build();
    }

    public HealthCheckFormPo toPo() {
        return HealthCheckFormPo.builder()
                .id(formId)
                .elderId(elderId)
                .authorUserId(authorUserId)
                .agreementId(agreementId)
                .elderName(elderName)
                .formCode(formCode)
                .checkDate(checkDate)
                .responsibleDoctor(responsibleDoctor)
                .formVersion(formVersion)
                .symptomSection(symptomSection)
                .vitalSignSection(vitalSignSection)
                .selfEvaluationSection(selfEvaluationSection)
                .cognitiveEmotionSection(cognitiveEmotionSection)
                .lifestyleSection(lifestyleSection)
                .nursingConclusionSection(nursingConclusionSection)
                .chronicDiseaseSummary(chronicDiseaseSummary)
                .allergySummary(allergySummary)
                .riskLevel(riskLevel)
                .score(score)
                .conclusion(conclusion)
                .build();
    }

    public void applyTo(HealthCheckFormPo po) {
        po.setId(formId);
        po.setElderId(elderId);
        po.setAuthorUserId(authorUserId);
        po.setAgreementId(agreementId);
        po.setElderName(elderName);
        po.setFormCode(formCode);
        po.setCheckDate(checkDate);
        po.setResponsibleDoctor(responsibleDoctor);
        po.setFormVersion(formVersion);
        po.setSymptomSection(symptomSection);
        po.setVitalSignSection(vitalSignSection);
        po.setSelfEvaluationSection(selfEvaluationSection);
        po.setCognitiveEmotionSection(cognitiveEmotionSection);
        po.setLifestyleSection(lifestyleSection);
        po.setNursingConclusionSection(nursingConclusionSection);
        po.setChronicDiseaseSummary(chronicDiseaseSummary);
        po.setAllergySummary(allergySummary);
        po.setRiskLevel(riskLevel);
        po.setScore(score);
        po.setConclusion(conclusion);
    }

    public void initialize() {
        if (this.checkDate == null) {
            this.checkDate = LocalDate.now();
        }
        if (this.formVersion == null || this.formVersion.isBlank()) {
            this.formVersion = "PAPER_V1";
        }
    }
}
