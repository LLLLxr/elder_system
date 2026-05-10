package org.smart_elder_system.admission.rules;

import org.smart_elder_system.admission.vo.FamilyVisitSlot;
import org.smart_elder_system.common.dto.admission.FamilyVisitReservationRuleDto;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class FamilyVisitReservationRules {

    private static final int MIN_ADVANCE_DAYS = 1;
    private static final int MAX_WORKING_DAYS_AHEAD = 7;
    private static final boolean WORKING_DAYS_ONLY = true;
    private static final LocalTime BOOKING_START_TIME = LocalTime.of(8, 0);
    private static final LocalTime BOOKING_END_TIME = LocalTime.of(17, 0);
    private static final int SLOT_DURATION_MINUTES = 60;
    private static final int DEFAULT_SLOT_CAPACITY = 3;
    private static final LocalTime LUNCH_BREAK_START = LocalTime.of(12, 0);
    private static final LocalTime LUNCH_BREAK_END = LocalTime.of(13, 0);
    private static final Set<DayOfWeek> WORKING_DAYS = Set.of(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
    );

    public FamilyVisitReservationRuleDto describe() {
        return new FamilyVisitReservationRuleDto(
                MIN_ADVANCE_DAYS,
                MAX_WORKING_DAYS_AHEAD,
                WORKING_DAYS_ONLY,
                BOOKING_START_TIME.toString(),
                BOOKING_END_TIME.toString(),
                SLOT_DURATION_MINUTES,
                List.of(LUNCH_BREAK_START + "-" + LUNCH_BREAK_END),
                WORKING_DAYS.stream()
                        .map(DayOfWeek::getValue)
                        .sorted()
                        .toList()
        );
    }

    public List<LocalDate> computeBookableDates(LocalDate today) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate candidate = today.plusDays(MIN_ADVANCE_DAYS);
        while (dates.size() < MAX_WORKING_DAYS_AHEAD) {
            if (isWorkingDay(candidate)) {
                dates.add(candidate);
            }
            candidate = candidate.plusDays(1);
        }
        return dates;
    }

    public boolean isWorkingDay(LocalDate date) {
        return date != null && WORKING_DAYS.contains(date.getDayOfWeek());
    }

    public boolean isDateWithinBookingWindow(LocalDate date, LocalDate today) {
        if (date == null || today == null) {
            return false;
        }
        return computeBookableDates(today).contains(date);
    }

    public boolean isSlotTimeAllowed(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            return false;
        }
        if (startTime.getMinute() != 0 || endTime.getMinute() != 0
                || startTime.getSecond() != 0 || startTime.getNano() != 0
                || endTime.getSecond() != 0 || endTime.getNano() != 0) {
            return false;
        }
        if (!Duration.between(startTime, endTime).equals(Duration.ofMinutes(SLOT_DURATION_MINUTES))) {
            return false;
        }
        if (startTime.isBefore(BOOKING_START_TIME) || endTime.isAfter(BOOKING_END_TIME)) {
            return false;
        }
        return !(startTime.isBefore(LUNCH_BREAK_END) && endTime.isAfter(LUNCH_BREAK_START));
    }

    public List<FamilyVisitSlot> buildDefaultDailySlots(LocalDate slotDate) {
        if (!isWorkingDay(slotDate)) {
            return List.of();
        }
        List<FamilyVisitSlot> slots = new ArrayList<>();
        LocalTime startTime = BOOKING_START_TIME;
        while (true) {
            LocalTime endTime = startTime.plusMinutes(SLOT_DURATION_MINUTES);
            if (endTime.isAfter(BOOKING_END_TIME)) {
                break;
            }
            if (isSlotTimeAllowed(startTime, endTime)) {
                slots.add(FamilyVisitSlot.builder()
                        .slotDate(slotDate)
                        .startTime(startTime)
                        .endTime(endTime)
                        .capacity(DEFAULT_SLOT_CAPACITY)
                        .reservedCount(0)
                        .status(FamilyVisitSlot.STATUS_OPEN)
                        .build());
            }
            startTime = endTime;
        }
        return slots;
    }

    public boolean isSlotReservable(FamilyVisitSlot slot, LocalDateTime now) {
        if (slot == null || now == null || slot.getSlotDate() == null) {
            return false;
        }
        if (!isDateWithinBookingWindow(slot.getSlotDate(), now.toLocalDate())) {
            return false;
        }
        if (!isSlotTimeAllowed(slot.getStartTime(), slot.getEndTime())) {
            return false;
        }
        if (slot.getSlotDate().isEqual(now.toLocalDate())) {
            return slot.getStartTime() != null && slot.getStartTime().isAfter(now.toLocalTime());
        }
        return !slot.getSlotDate().isBefore(now.toLocalDate());
    }

    public void validateSlotReservable(FamilyVisitSlot slot, LocalDateTime now) {
        if (slot == null || slot.getSlotDate() == null) {
            throw new IllegalStateException("当前时段不可预约");
        }
        LocalDate today = now.toLocalDate();
        if (!isDateWithinBookingWindow(slot.getSlotDate(), today)) {
            throw new IllegalStateException("当前时段不在可预约日期范围内");
        }
        if (!isSlotTimeAllowed(slot.getStartTime(), slot.getEndTime())) {
            throw new IllegalStateException("当前时段不符合预约规则");
        }
        if (slot.getSlotDate().isEqual(today) && (slot.getStartTime() == null || !slot.getStartTime().isAfter(now.toLocalTime()))) {
            throw new IllegalStateException("当前时段已过可预约时间");
        }
    }
}
