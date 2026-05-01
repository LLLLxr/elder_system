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
public class CarePlanDTO {

    private Long planId;

    @NotNull
    @Min(1)
    private Long agreementId;

    @NotNull
    @Min(1)
    private Long elderId;

    @NotBlank
    private String planName;

    @NotBlank
    private String serviceScene;

    private String personalizationNote;

    /**
     * 状态：CREATED / IN_PROGRESS / CLOSED
     */
    private String status;

    private LocalDate planDate;
}
