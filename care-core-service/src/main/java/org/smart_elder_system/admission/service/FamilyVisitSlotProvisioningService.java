package org.smart_elder_system.admission.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smart_elder_system.admission.vo.FamilyVisitSlot;
import org.smart_elder_system.admission.po.FamilyVisitSlotPo;
import org.smart_elder_system.admission.repository.FamilyVisitSlotRepository;
import org.smart_elder_system.admission.rules.FamilyVisitReservationRules;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FamilyVisitSlotProvisioningService {

    private final FamilyVisitSlotRepository familyVisitSlotRepository;
    private final FamilyVisitReservationRules familyVisitReservationRules;

    @EventListener(ApplicationReadyEvent.class)
    public void provisionOnStartup() {
        provisionMissingSlots(LocalDate.now());
    }

    @Scheduled(cron = "${admission.family-visit-slot.provision-cron:0 5 0 * * *}")
    public void provisionDailySlots() {
        provisionMissingSlots(LocalDate.now());
    }

    @Transactional(rollbackFor = Exception.class)
    public int provisionMissingSlots(LocalDate today) {
        List<LocalDate> bookableDates = familyVisitReservationRules.computeBookableDates(today);
        if (bookableDates.isEmpty()) {
            return 0;
        }

        LocalDate startDate = bookableDates.getFirst();
        LocalDate endDate = bookableDates.getLast();
        Set<SlotKey> existingSlotKeys = new HashSet<>(familyVisitSlotRepository.findBySlotDateBetween(startDate, endDate).stream()
                .map(slot -> new SlotKey(slot.getSlotDate(), slot.getStartTime(), slot.getEndTime()))
                .toList());

        List<FamilyVisitSlotPo> missingSlots = bookableDates.stream()
                .flatMap(slotDate -> familyVisitReservationRules.buildDefaultDailySlots(slotDate).stream())
                .filter(slot -> existingSlotKeys.add(new SlotKey(slot.getSlotDate(), slot.getStartTime(), slot.getEndTime())))
                .map(this::toPo)
                .toList();

        if (missingSlots.isEmpty()) {
            return 0;
        }

        familyVisitSlotRepository.saveAll(missingSlots);
        log.info("已补齐 {} 个家属参观时段", missingSlots.size());
        return missingSlots.size();
    }

    private FamilyVisitSlotPo toPo(FamilyVisitSlot slot) {
        FamilyVisitSlotPo po = new FamilyVisitSlotPo();
        slot.applyTo(po);
        return po;
    }

    private record SlotKey(LocalDate slotDate, LocalTime startTime, LocalTime endTime) {
    }
}
