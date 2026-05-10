import dayjs from 'dayjs';
import { DatePicker, Form } from 'antd';
import { getDateAvailabilityTag } from '../utils/familyVisitSlotUtils';
import type { FamilyVisitSlot } from '../types/care';

interface SlotDatePickerProps {
  selectedDate: string | null;
  availableDates: string[];
  dateSlotMap: Record<string, FamilyVisitSlot[]>;
  onDateChange: (value: dayjs.Dayjs | null) => void;
}

export default function SlotDatePicker({ selectedDate, availableDates, dateSlotMap, onDateChange }: SlotDatePickerProps) {
  return (
    <Form layout="vertical">
      <Form.Item label="预约日期" required style={{ marginBottom: 0 }}>
        <DatePicker
          value={selectedDate ? dayjs(selectedDate, 'YYYY-MM-DD') : null}
          onChange={onDateChange}
          format="YYYY-MM-DD"
          allowClear
          style={{ width: 280 }}
          placeholder={availableDates.length > 0 ? '请选择有可预约时段的日期' : '暂无可预约日期'}
          disabled={availableDates.length === 0}
          disabledDate={(current) => !current || !availableDates.includes(current.format('YYYY-MM-DD'))}
          cellRender={(current) => {
            const slotDate = current.format('YYYY-MM-DD');
            const daySlots = dateSlotMap[slotDate] ?? [];
            return (
              <div style={{ minHeight: 44, padding: '2px 0' }}>
                <div>{current.date()}</div>
                {daySlots.length > 0 ? <div style={{ marginTop: 4 }}>{getDateAvailabilityTag(daySlots)}</div> : null}
              </div>
            );
          }}
        />
      </Form.Item>
    </Form>
  );
}
