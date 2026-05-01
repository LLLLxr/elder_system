package org.smart_elder_system.common.dto.care;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthAssessmentDTO {

    private Long assessmentId;

    @NotNull
    @Min(1)
    private Long elderId;

    @NotNull
    @Min(1)
    private Long agreementId;

    @NotBlank
    private String assessmentType;

    private String conclusion;

    @NotNull
    private Integer score;

    private LocalDateTime assessedAt;
}
