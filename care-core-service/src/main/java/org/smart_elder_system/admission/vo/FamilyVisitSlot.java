package org.smart_elder_system.admission.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.smart_elder_system.admission.po.FamilyVisitSlotPo;
import org.smart_elder_system.common.dto.admission.FamilyVisitSlotDto;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyVisitSlot {

    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_CLOSED = "CLOSED";

    private Long slotId;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer capacity;
    private Integer reservedCount;
    private String status;

    public static FamilyVisitSlot fromPo(FamilyVisitSlotPo po) {
        return FamilyVisitSlot.builder()
                .slotId(po.getId())
                .slotDate(po.getSlotDate())
                .startTime(po.getStartTime())
                .endTime(po.getEndTime())
                .capacity(po.getCapacity())
                .reservedCount(po.getReservedCount())
                .status(po.getStatus())
                .build();
    }

    public FamilyVisitSlotDto toDto() {
        return FamilyVisitSlotDto.builder()
                .slotId(slotId)
                .slotDate(slotDate)
                .startTime(startTime)
                .endTime(endTime)
                .capacity(capacity)
                .reservedCount(reservedCount)
                .status(status)
                .build();
    }

    public void reserve() {
        if (!STATUS_OPEN.equals(status)) {
            throw new IllegalArgumentException("当前时段不可预约");
        }
        int currentReserved = reservedCount == null ? 0 : reservedCount;
        int currentCapacity = capacity == null ? 0 : capacity;
        if (currentReserved >= currentCapacity) {
            throw new IllegalArgumentException("当前时段已约满");
        }
        reservedCount = currentReserved + 1;
    }

    public void release() {
        int currentReserved = reservedCount == null ? 0 : reservedCount;
        reservedCount = Math.max(0, currentReserved - 1);
    }

    public void applyTo(FamilyVisitSlotPo po) {
        po.setId(slotId);
        po.setSlotDate(slotDate);
        po.setStartTime(startTime);
        po.setEndTime(endTime);
        po.setCapacity(capacity);
        po.setReservedCount(reservedCount);
        po.setStatus(status);
    }
}
