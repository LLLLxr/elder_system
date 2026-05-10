package org.smart_elder_system.common.dto.caredelivery;

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
public class FamilyServicePlanDto {

    private Long servicePlanId;
    private Long elderId;
    private String planName;
    private List<DailyCareTaskItemDto> planItems;
    private LocalDate effectiveDate;
    private LocalDate expireDate;
    private String status;
    private Long assignedCaregiverId;
    private String assignedCaregiverName;
}
