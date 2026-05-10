import { useEffect, useMemo, useState } from 'react';
import type { Dayjs } from 'dayjs';
import { getFamilyVisitReservationRules, listFamilyVisitSlots } from '../api/familyVisitApi';
import { extractApiErrorMessage } from '../api/client';
import { sortSlots } from '../utils/familyVisitSlotUtils';
import type { FamilyVisitReservationRule, FamilyVisitSlot } from '../types/care';

export function useFamilyVisitSlots() {
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [reservationRule, setReservationRule] = useState<FamilyVisitReservationRule | null>(null);
  const [availableDates, setAvailableDates] = useState<string[]>([]);
  const [dateSlotMap, setDateSlotMap] = useState<Record<string, FamilyVisitSlot[]>>({});
  const [slotsForSelectedDate, setSlotsForSelectedDate] = useState<FamilyVisitSlot[]>([]);
  const [selectedDate, setSelectedDate] = useState<string | null>(null);
  const [selectedSlotId, setSelectedSlotId] = useState<number | null>(null);

  const selectedSlot = useMemo(() => {
    return slotsForSelectedDate.find((slot) => slot.slotId === selectedSlotId) ?? null;
  }, [selectedSlotId, slotsForSelectedDate]);

  const loadAvailableDates = async () => {
    const data = await listFamilyVisitSlots();
    const groupedSlots = data.reduce<Record<string, FamilyVisitSlot[]>>((accumulator, slot) => {
      if (!slot.slotDate) {
        return accumulator;
      }
      const currentSlots = accumulator[slot.slotDate] ?? [];
      currentSlots.push(slot);
      accumulator[slot.slotDate] = currentSlots;
      return accumulator;
    }, {});
    Object.keys(groupedSlots).forEach((slotDate) => {
      groupedSlots[slotDate] = sortSlots(groupedSlots[slotDate]);
    });
    const nextDates = Object.keys(groupedSlots).sort();
    setDateSlotMap(groupedSlots);
    setAvailableDates(nextDates);
    setSelectedDate((currentDate) => (currentDate && nextDates.includes(currentDate) ? currentDate : null));
    return { nextDates, groupedSlots };
  };

  const loadSlotsByDate = async (slotDate: string) => {
    const data = await listFamilyVisitSlots(slotDate);
    const nextSlots = sortSlots(data);
    setSlotsForSelectedDate(nextSlots);
    setSelectedSlotId((currentSlotId) => (currentSlotId && nextSlots.some((slot) => slot.slotId === currentSlotId) ? currentSlotId : null));
  };

  const refreshSlots = async (slotDate?: string | null) => {
    setLoading(true);
    setErrorMessage(null);
    try {
      const [rules, dateSummary] = await Promise.all([
        reservationRule ? Promise.resolve(reservationRule) : getFamilyVisitReservationRules(),
        loadAvailableDates(),
      ]);
      setReservationRule(rules);
      const { nextDates, groupedSlots } = dateSummary;
      const dateToLoad = slotDate ?? selectedDate;
      if (dateToLoad && nextDates.includes(dateToLoad)) {
        const nextSlots = groupedSlots[dateToLoad] ?? [];
        setSlotsForSelectedDate(nextSlots);
        setSelectedSlotId((currentSlotId) => (currentSlotId && nextSlots.some((slot) => slot.slotId === currentSlotId) ? currentSlotId : null));
      } else if (!dateToLoad && nextDates.length > 0) {
        const firstDate = nextDates[0];
        setSelectedDate(firstDate);
        setSlotsForSelectedDate(groupedSlots[firstDate] ?? []);
        setSelectedSlotId(null);
      } else {
        setSlotsForSelectedDate([]);
        setSelectedSlotId(null);
      }
    } catch (error) {
      setReservationRule(null);
      setAvailableDates([]);
      setDateSlotMap({});
      setSlotsForSelectedDate([]);
      setSelectedDate(null);
      setSelectedSlotId(null);
      setErrorMessage(extractApiErrorMessage(error, '加载可预约时段失败'));
    } finally {
      setLoading(false);
    }
  };

  const handleDateChange = async (value: Dayjs | null) => {
    const nextDate = value ? value.format('YYYY-MM-DD') : null;
    setSelectedDate(nextDate);
    setSelectedSlotId(null);
    if (!nextDate) {
      setSlotsForSelectedDate([]);
      return;
    }

    setLoading(true);
    setErrorMessage(null);
    try {
      await loadSlotsByDate(nextDate);
    } catch (error) {
      setSlotsForSelectedDate([]);
      setErrorMessage(extractApiErrorMessage(error, '加载所选日期的时段失败'));
    } finally {
      setLoading(false);
    }
  };

  const handleSelectSlot = (slotId: number) => {
    setSelectedSlotId(slotId);
  };

  useEffect(() => {
    void refreshSlots(null);
  }, []);

  return {
    loading,
    errorMessage,
    reservationRule,
    availableDates,
    dateSlotMap,
    slotsForSelectedDate,
    selectedDate,
    selectedSlotId,
    selectedSlot,
    refreshSlots,
    handleDateChange,
    handleSelectSlot,
  };
}
