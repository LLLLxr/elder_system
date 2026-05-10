package org.smart_elder_system.common.dto.caredelivery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaregiverCheckInRecordDto {

    private Long checkInRecordId;
    private Long elderId;
    private String elderName;
    private Long caregiverId;
    private String caregiverName;
    private Long servicePlanId;
    private LocalDate taskDate;
    private List<DailyCareTaskItemDto> taskItems;
    private String completionStatus;
    private LocalDateTime completionTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
