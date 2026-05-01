package org.smart_elder_system.common.dto.care;

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
public class HealthCheckFormCreateRequestDTO {

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

    private String chronicDiseaseSummary;

    private String allergySummary;
}
