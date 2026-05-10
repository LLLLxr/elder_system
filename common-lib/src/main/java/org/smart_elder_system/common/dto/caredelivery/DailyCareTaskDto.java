package org.smart_elder_system.common.dto.caredelivery;

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
public class DailyCareTaskDto {

    @NotNull
    @Min(1)
    private Long elderId;

    private String elderName;

    @NotNull
    @Min(1)
    private Long servicePlanId;

    @NotNull
    private LocalDate taskDate;

    @NotNull
    private List<DailyCareTaskItemDto> taskItems;
}
