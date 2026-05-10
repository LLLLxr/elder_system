package org.smart_elder_system.common.dto.health;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckFormCreateRequestDto {

    @NotNull
    @Min(1)
    private Long elderId;

    @NotNull
    @Min(1)
    private Long agreementId;

    @NotBlank
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
}
