package org.smart_elder_system.common.dto.care;

import jakarta.validation.constraints.Min;
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
public class HealthProfileDTO {

    private Long profileId;

    @NotNull
    @Min(1)
    private Long elderId;

    @NotNull
    @Min(1)
    private Long agreementId;

    private String bloodType;

    private String chronicDiseaseSummary;

    private String allergySummary;

    private String riskLevel;

    private LocalDate profileDate;
}
