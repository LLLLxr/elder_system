import { useEffect, useMemo, useState } from 'react';
import { Alert, Button, Form, Space, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';
import { createFamilyVisitReservation } from '../api/familyVisitApi';
import { extractApiErrorMessage } from '../api/client';
import { ROUTE_PATHS } from '../constants/routes';
import DefaultCollapsedSection from '../components/DefaultCollapsedSection';
import SlotDatePicker from '../components/SlotDatePicker';
import SlotCardList from '../components/SlotCardList';
import ReservationInfoForm from '../components/ReservationInfoForm';
import { useActiveElderGuard } from '../components/useActiveElderGuard';
import { useFamilyVisitSlots } from '../hooks/useFamilyVisitSlots';
import { isSlotUnavailable } from '../utils/familyVisitSlotUtils';
import type { FamilyVisitReservation } from '../types/care';

export default function FamilyVisitReservationCreatePage() {
  const navigate = useNavigate();
  const [form] = Form.useForm<FamilyVisitReservation>();
  const { activeElderId, activeElder, hasActiveElder, guardMessage } = useActiveElderGuard();
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const {
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
  } = useFamilyVisitSlots();

  const reservationRuleMessage = useMemo(() => {
    if (!reservationRule) {
      return '请先选择预约日期，再选择具体时段；每次刷新和切换日期都会实时获取最新可预约时段。';
    }
    const excludedTimeRanges = reservationRule.excludedTimeRanges.length > 0
      ? `，其中 ${reservationRule.excludedTimeRanges.join('、')} 不开放预约`
      : '';
    return `请先选择预约日期，再选择具体时段；当前仅支持提前 ${reservationRule.minAdvanceDays} 天预约未来 ${reservationRule.maxWorkingDaysAhead} 个工作日内 ${reservationRule.bookingStartTime}-${reservationRule.bookingEndTime} 的 ${reservationRule.slotDurationMinutes} 分钟时段${excludedTimeRanges}。`;
  }, [reservationRule]);

  useEffect(() => {
    if (activeElderId != null) {
      form.setFieldsValue({ elderId: activeElderId });
      return;
    }
    form.setFieldsValue({ elderId: undefined });
  }, [activeElderId, form]);

  const handleSlotSelect = (slot: typeof slotsForSelectedDate[0]) => {
    if (typeof slot.slotId !== 'number' || isSlotUnavailable(slot)) {
      return;
    }
    handleSelectSlot(slot.slotId);
  };

  const handleSubmit = async (values: FamilyVisitReservation) => {
    if (!selectedDate) {
      setSubmitError('请先选择预约日期');
      return;
    }
    if (!selectedSlotId) {
      setSubmitError('请先选择一个可预约时段');
      return;
    }
    if (!activeElderId) {
      setSubmitError(guardMessage);
      return;
    }

    setSubmitting(true);
    setSubmitError(null);
    try {
      await createFamilyVisitReservation({ ...values, elderId: activeElderId, slotId: selectedSlotId });
      navigate(ROUTE_PATHS.FAMILY_VISIT_MY, { replace: true });
    } catch (error) {
      setSubmitError(extractApiErrorMessage(error, '提交家属预约失败'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Typography.Title level={4} style={{ margin: 0 }}>
        家属预约参观
      </Typography.Title>

      <Alert
        type="info"
        showIcon
        message={`${reservationRuleMessage} 当前服务对象：${activeElder?.elderName ?? '-'}。${hasActiveElder ? '' : ` ${guardMessage}`}`}
      />
      {(errorMessage || submitError) ? <Alert type="error" showIcon message={errorMessage || submitError} /> : null}

      <DefaultCollapsedSection
        title="选择预约时段"
        extra={
          <Button onClick={(event) => { event.stopPropagation(); void refreshSlots(selectedDate); }}>
            刷新时段
          </Button>
        }
      >
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <SlotDatePicker
            selectedDate={selectedDate}
            availableDates={availableDates}
            dateSlotMap={dateSlotMap}
            onDateChange={handleDateChange}
          />
          <SlotCardList
            selectedDate={selectedDate}
            slotsForSelectedDate={slotsForSelectedDate}
            selectedSlotId={selectedSlotId}
            onSelectSlot={handleSlotSelect}
          />
        </Space>
      </DefaultCollapsedSection>

      <DefaultCollapsedSection title="预约信息填写">
        <ReservationInfoForm
          form={form}
          selectedSlot={selectedSlot}
          submitting={submitting}
          onSubmit={handleSubmit}
        />
      </DefaultCollapsedSection>
    </Space>
  );
}
