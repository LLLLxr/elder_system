import { Card, Col, Empty, Radio, Row, Space, Tag } from 'antd';
import { formatSlotRange, isSlotUnavailable, renderSlotStatus } from '../utils/familyVisitSlotUtils';
import type { FamilyVisitSlot } from '../types/care';

interface SlotCardListProps {
  selectedDate: string | null;
  slotsForSelectedDate: FamilyVisitSlot[];
  selectedSlotId: number | null;
  onSelectSlot: (slot: FamilyVisitSlot) => void;
}

export default function SlotCardList({ selectedDate, slotsForSelectedDate, selectedSlotId, onSelectSlot }: SlotCardListProps) {
  if (!selectedDate) {
    return <Empty description="请先选择预约日期" />;
  }

  if (slotsForSelectedDate.length === 0) {
    return <Empty description="所选日期暂无可预约时段" />;
  }

  return (
    <Radio.Group value={selectedSlotId} style={{ width: '100%' }}>
      <Row gutter={[16, 16]}>
        {slotsForSelectedDate.map((slot) => {
          const slotId = slot.slotId ?? 0;
          const disabled = isSlotUnavailable(slot);
          const isSelected = selectedSlotId === slot.slotId;
          return (
            <Col xs={24} md={12} xl={8} key={slotId}>
              <Card
                hoverable={!disabled}
                onClick={() => onSelectSlot(slot)}
                style={{
                  height: '100%',
                  cursor: disabled ? 'not-allowed' : 'pointer',
                  opacity: disabled ? 0.55 : 1,
                  borderColor: isSelected ? '#1677ff' : undefined,
                  boxShadow: isSelected ? '0 0 0 2px rgba(22, 119, 255, 0.12)' : undefined,
                }}
              >
                <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                  <Space align="center" style={{ width: '100%', justifyContent: 'space-between' }}>
                    <Radio value={slot.slotId} checked={isSelected} disabled={disabled}>
                      {formatSlotRange(slot)}
                    </Radio>
                    {renderSlotStatus(slot)}
                  </Space>
                  <Space wrap>
                    <Tag color="blue">容量 {slot.capacity ?? 0}</Tag>
                    <Tag color={disabled ? 'error' : 'processing'}>已预约 {slot.reservedCount ?? 0}</Tag>
                    {disabled ? <Tag color="default">已约满</Tag> : null}
                  </Space>
                </Space>
              </Card>
            </Col>
          );
        })}
      </Row>
    </Radio.Group>
  );
}
