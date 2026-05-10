package org.smart_elder_system.common.dto.admission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyVisitSlotDto {

    private Long slotId;

    private LocalDate slotDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer capacity;

    private Integer reservedCount;

    private String status;
}
