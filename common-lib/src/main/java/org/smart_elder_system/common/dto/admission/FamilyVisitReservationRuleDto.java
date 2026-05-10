package org.smart_elder_system.common.dto.admission;

import java.util.List;

public record FamilyVisitReservationRuleDto(
        int minAdvanceDays,
        int maxWorkingDaysAhead,
        boolean workingDaysOnly,
        String bookingStartTime,
        String bookingEndTime,
        int slotDurationMinutes,
        List<String> excludedTimeRanges,
        List<Integer> workingDaysOfWeek
) {
}
