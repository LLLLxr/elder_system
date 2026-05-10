package org.smart_elder_system.common.dto.health;

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
public class DoctorRoundRecordSaveDto {

    @NotNull
    @Min(1)
    private Long elderId;

    @NotBlank
    private String content;

    @NotNull
    private Boolean riskFlag;

    @NotNull
    private LocalDateTime roundTime;
}
