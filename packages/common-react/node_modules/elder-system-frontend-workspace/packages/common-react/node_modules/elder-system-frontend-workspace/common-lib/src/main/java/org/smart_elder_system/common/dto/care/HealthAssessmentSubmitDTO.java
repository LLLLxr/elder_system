package org.smart_elder_system.common.dto.care;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HealthAssessmentSubmitDTO {

    @NotNull
    @Min(1)
    private Long applicationId;

    @NotNull
    private Boolean passed;

    @NotBlank
    private String assessmentConclusion;

    @NotBlank
    private String assessor;

    @NotBlank
    private String responsibleDoctor;

    @NotNull
    private Integer score;
}
