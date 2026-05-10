package org.smart_elder_system.admission.rules;

import org.junit.jupiter.api.Test;
import org.smart_elder_system.admission.vo.FamilyVisitSlot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FamilyVisitReservationRulesTest {

    private final FamilyVisitReservationRules rules = new FamilyVisitReservationRules();

    @Test
    void shouldReturnSevenWorkingDaysStartingFromNextWorkingDay() {
        List<LocalDate> result = rules.computeBookableDates(LocalDate.of(2026, 5, 8));

        assertEquals(7, result.size());
        assertEquals(LocalDate.of(2026, 5, 11), result.get(0));
        assertEquals(LocalDate.of(2026, 5, 19), result.get(6));
        assertTrue(result.stream().noneMatch(date -> date.getDayOfWeek().getValue() > 5));
    }

    @Test
    void shouldAllowStandardHourSlotsButRejectLunchAndHalfHourSlots() {
        assertTrue(rules.isSlotTimeAllowed(LocalTime.of(8, 0), LocalTime.of(9, 0)));
        assertTrue(rules.isSlotTimeAllowed(LocalTime.of(16, 0), LocalTime.of(17, 0)));
        assertFalse(rules.isSlotTimeAllowed(LocalTime.of(12, 0), LocalTime.of(13, 0)));
        assertFalse(rules.isSlotTimeAllowed(LocalTime.of(9, 30), LocalTime.of(10, 30)));
        assertFalse(rules.isSlotTimeAllowed(LocalTime.of(9, 0), LocalTime.of(9, 30)));
    }

    @Test
    void shouldRejectWeekendAndOutOfWindowSlots() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 8, 10, 0);

        FamilyVisitSlot weekendSlot = FamilyVisitSlot.builder()
                .slotId(1L)
                .slotDate(LocalDate.of(2026, 5, 9))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .build();
        FamilyVisitSlot farFutureSlot = FamilyVisitSlot.builder()
                .slotId(2L)
                .slotDate(LocalDate.of(2026, 5, 20))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .build();

        assertFalse(rules.isSlotReservable(weekendSlot, now));
        assertFalse(rules.isSlotReservable(farFutureSlot, now));
    }

    @Test
    void shouldThrowWhenSlotBreaksReservationRules() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 8, 10, 0);
        FamilyVisitSlot lunchSlot = FamilyVisitSlot.builder()
                .slotId(3L)
                .slotDate(LocalDate.of(2026, 5, 11))
                .startTime(LocalTime.of(12, 0))
                .endTime(LocalTime.of(13, 0))
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> rules.validateSlotReservable(lunchSlot, now));

        assertEquals("当前时段不符合预约规则", exception.getMessage());
    }
}
