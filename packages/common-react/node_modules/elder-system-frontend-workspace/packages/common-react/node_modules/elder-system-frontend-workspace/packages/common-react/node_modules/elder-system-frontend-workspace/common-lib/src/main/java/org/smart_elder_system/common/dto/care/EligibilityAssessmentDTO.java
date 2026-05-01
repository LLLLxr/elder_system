package org.smart_elder_system.common.dto.care;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EligibilityAssessmentDTO {

    @NotNull
    @Min(1)
    private Long applicationId;

    @NotNull
    private Boolean eligible;

    @NotBlank
    private String assessmentConclusion;

    @NotBlank
    private String assessor;

    private LocalDateTime assessedAt;
}
