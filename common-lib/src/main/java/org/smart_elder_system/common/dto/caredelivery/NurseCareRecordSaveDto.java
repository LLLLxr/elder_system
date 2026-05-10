package org.smart_elder_system.common.dto.caredelivery;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NurseCareRecordSaveDto {

    @NotNull
    @Min(1)
    private Long elderId;

    @Min(1)
    private Long servicePlanId;

    @NotNull
    private LocalDate recordDate;

    @NotNull
    private Map<String, Object> recordFormData;
}
