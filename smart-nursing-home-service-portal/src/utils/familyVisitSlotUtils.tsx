import { Tag } from 'antd';
import type { FamilyVisitSlot } from '../types/care';

export function isSlotUnavailable(slot: FamilyVisitSlot) {
  const reservedCount = slot.reservedCount ?? 0;
  const capacity = slot.capacity ?? 0;
  return slot.status !== 'OPEN' || reservedCount >= capacity;
}

export function renderSlotStatus(slot: FamilyVisitSlot) {
  if (isSlotUnavailable(slot)) {
    return <Tag color="default">不可预约</Tag>;
  }
  return <Tag color="success">可预约</Tag>;
}

export function formatSlotRange(slot: FamilyVisitSlot) {
  return [slot.startTime, slot.endTime ? `- ${slot.endTime}` : undefined].filter(Boolean).join(' ');
}

export function sortSlots(data: FamilyVisitSlot[]) {
  return [...data].sort((left, right) => `${left.startTime ?? ''}${left.endTime ?? ''}`.localeCompare(`${right.startTime ?? ''}${right.endTime ?? ''}`));
}

export function getDateAvailabilityTag(slots: FamilyVisitSlot[]) {
  if (slots.length === 0) {
    return <Tag color="default">无时段</Tag>;
  }
  const hasReservableSlot = slots.some((slot) => !isSlotUnavailable(slot));
  return hasReservableSlot ? <Tag color="success">可预约</Tag> : <Tag color="default">已满</Tag>;
}
