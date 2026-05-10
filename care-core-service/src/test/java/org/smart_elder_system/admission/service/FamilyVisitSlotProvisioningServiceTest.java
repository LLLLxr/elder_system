package org.smart_elder_system.admission.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.smart_elder_system.admission.vo.FamilyVisitSlot;
import org.smart_elder_system.admission.po.FamilyVisitSlotPo;
import org.smart_elder_system.admission.repository.FamilyVisitSlotRepository;
import org.smart_elder_system.admission.rules.FamilyVisitReservationRules;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FamilyVisitSlotProvisioningServiceTest {

    @Spy
    private FamilyVisitReservationRules familyVisitReservationRules = new FamilyVisitReservationRules();

    @Mock
    private FamilyVisitSlotRepository familyVisitSlotRepository;

    @InjectMocks
    private FamilyVisitSlotProvisioningService familyVisitSlotProvisioningService;

    @Test
    void shouldProvisionMissingSlotsWithinBookingWindow() {
        LocalDate today = LocalDate.of(2026, 5, 8);
        when(familyVisitSlotRepository.findBySlotDateBetween(LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 19)))
                .thenReturn(List.of(
                        existingSlot(LocalDate.of(2026, 5, 11), LocalTime.of(9, 0), LocalTime.of(10, 0)),
                        existingSlot(LocalDate.of(2026, 5, 12), LocalTime.of(14, 0), LocalTime.of(15, 0))
                ));

        int created = familyVisitSlotProvisioningService.provisionMissingSlots(today);

        assertEquals(54, created);
        ArgumentCaptor<List<FamilyVisitSlotPo>> captor = ArgumentCaptor.forClass(List.class);
        verify(familyVisitSlotRepository).saveAll(captor.capture());
        List<FamilyVisitSlotPo> saved = captor.getValue();
        assertEquals(54, saved.size());
        assertTrue(saved.stream().noneMatch(slot -> slot.getStartTime().equals(LocalTime.of(12, 0))));
        assertTrue(saved.stream().anyMatch(slot -> slot.getSlotDate().equals(LocalDate.of(2026, 5, 11))
                && slot.getStartTime().equals(LocalTime.of(8, 0))
                && slot.getEndTime().equals(LocalTime.of(9, 0))));
        assertTrue(saved.stream().anyMatch(slot -> slot.getSlotDate().equals(LocalDate.of(2026, 5, 19))
                && slot.getStartTime().equals(LocalTime.of(16, 0))
                && slot.getEndTime().equals(LocalTime.of(17, 0))));
    }

    @Test
    void shouldSkipSaveWhenAllSlotsAlreadyExist() {
        LocalDate today = LocalDate.of(2026, 5, 8);
        List<FamilyVisitSlotPo> existing = familyVisitReservationRules.computeBookableDates(today).stream()
                .flatMap(slotDate -> familyVisitReservationRules.buildDefaultDailySlots(slotDate).stream())
                .map(this::toPo)
                .toList();
        when(familyVisitSlotRepository.findBySlotDateBetween(LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 19)))
                .thenReturn(existing);

        int created = familyVisitSlotProvisioningService.provisionMissingSlots(today);

        assertEquals(0, created);
        verify(familyVisitSlotRepository, never()).saveAll(anyList());
    }

    private FamilyVisitSlotPo existingSlot(LocalDate slotDate, LocalTime startTime, LocalTime endTime) {
        FamilyVisitSlotPo po = new FamilyVisitSlotPo();
        po.setSlotDate(slotDate);
        po.setStartTime(startTime);
        po.setEndTime(endTime);
        po.setCapacity(3);
        po.setReservedCount(0);
        po.setStatus(FamilyVisitSlot.STATUS_OPEN);
        return po;
    }

    private FamilyVisitSlotPo toPo(FamilyVisitSlot slot) {
        FamilyVisitSlotPo po = new FamilyVisitSlotPo();
        slot.applyTo(po);
        return po;
    }
}
