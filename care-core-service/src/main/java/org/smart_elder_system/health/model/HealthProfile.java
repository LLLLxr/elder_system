package org.smart_elder_system.health.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.smart_elder_system.common.dto.care.HealthProfileDTO;
import org.smart_elder_system.health.po.HealthProfilePo;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthProfile {

    private Long profileId;
    private Long elderId;
    private Long agreementId;
    private String bloodType;
    private String chronicDiseaseSummary;
    private String allergySummary;
    private String riskLevel;
    private LocalDate profileDate;

    public static HealthProfile fromDTO(HealthProfileDTO dto) {
        return HealthProfile.builder()
                .profileId(dto.getProfileId())
                .elderId(dto.getElderId())
                .agreementId(dto.getAgreementId())
                .bloodType(dto.getBloodType())
                .chronicDiseaseSummary(dto.getChronicDiseaseSummary())
                .allergySummary(dto.getAllergySummary())
                .riskLevel(dto.getRiskLevel())
                .profileDate(dto.getProfileDate())
                .build();
    }

    public static HealthProfile fromPo(HealthProfilePo po) {
        return HealthProfile.builder()
                .profileId(po.getId())
                .elderId(po.getElderId())
                .agreementId(po.getAgreementId())
                .bloodType(po.getBloodType())
                .chronicDiseaseSummary(po.getChronicDiseaseSummary())
                .allergySummary(po.getAllergySummary())
                .riskLevel(po.getRiskLevel())
                .profileDate(po.getProfileDate())
                .build();
    }

    public HealthProfileDTO toDTO() {
        return HealthProfileDTO.builder()
                .profileId(profileId)
                .elderId(elderId)
                .agreementId(agreementId)
                .bloodType(bloodType)
                .chronicDiseaseSummary(chronicDiseaseSummary)
                .allergySummary(allergySummary)
                .riskLevel(riskLevel)
                .profileDate(profileDate)
                .build();
    }

    public HealthProfilePo toPo() {
        return HealthProfilePo.builder()
                .id(profileId)
                .elderId(elderId)
                .agreementId(agreementId)
                .bloodType(bloodType)
                .chronicDiseaseSummary(chronicDiseaseSummary)
                .allergySummary(allergySummary)
                .riskLevel(riskLevel)
                .profileDate(profileDate)
                .build();
    }

    public void applyTo(HealthProfilePo po) {
        po.setId(profileId);
        po.setElderId(elderId);
        po.setAgreementId(agreementId);
        po.setBloodType(bloodType);
        po.setChronicDiseaseSummary(chronicDiseaseSummary);
        po.setAllergySummary(allergySummary);
        po.setRiskLevel(riskLevel);
        po.setProfileDate(profileDate);
    }

    public void initialize() {
        this.profileDate = LocalDate.now();
    }
}
