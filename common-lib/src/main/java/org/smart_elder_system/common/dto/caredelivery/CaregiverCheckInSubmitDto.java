package org.smart_elder_system.common.dto.caredelivery;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaregiverCheckInSubmitDto {

    @NotNull
    @Min(1)
    private Long elderId;

    @NotNull
    private LocalDate taskDate;

    @NotNull
    @Valid
    private List<DailyCareTaskItemDto> taskItems;
}
