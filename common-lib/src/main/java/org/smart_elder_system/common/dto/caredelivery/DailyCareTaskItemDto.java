package org.smart_elder_system.common.dto.caredelivery;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyCareTaskItemDto {

    @NotBlank
    private String itemCode;

    @NotBlank
    private String itemName;

    private Boolean completed;
}
