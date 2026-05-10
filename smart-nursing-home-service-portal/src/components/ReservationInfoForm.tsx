import { Button, Form, Input, InputNumber } from 'antd';
import { formatSlotRange } from '../utils/familyVisitSlotUtils';
import type { FamilyVisitReservation, FamilyVisitSlot } from '../types/care';

interface ReservationInfoFormProps {
  form: ReturnType<typeof Form.useForm<FamilyVisitReservation>>[0];
  selectedSlot: FamilyVisitSlot | null;
  submitting: boolean;
  onSubmit: (values: FamilyVisitReservation) => void;
}

export default function ReservationInfoForm({ form, selectedSlot, submitting, onSubmit }: ReservationInfoFormProps) {
  return (
    <Form<FamilyVisitReservation> form={form} layout="vertical" onFinish={onSubmit}>
      <Form.Item label="当前选中时段">
        <Input
          value={selectedSlot ? `${selectedSlot.slotDate ?? ''} ${formatSlotRange(selectedSlot)}`.trim() : ''}
          disabled
          placeholder="请先选择预约日期和具体时段"
        />
      </Form.Item>
      <Form.Item label="当前服务对象编号" name="elderId" rules={[{ required: true, message: '请先选择当前服务对象' }]}>
        <InputNumber min={1} style={{ width: '100%' }} placeholder='请先在"绑定老人"中选择当前服务对象' disabled />
      </Form.Item>
      <Form.Item label="来访人姓名" name="visitorName" rules={[{ required: true, message: '请输入来访人姓名' }]}>
        <Input placeholder="请输入来访人姓名" />
      </Form.Item>
      <Form.Item label="联系电话" name="visitorPhone" rules={[{ required: true, message: '请输入联系电话' }]}>
        <Input placeholder="请输入联系电话" />
      </Form.Item>
      <Form.Item label="与老人关系" name="relationToElder" rules={[{ required: true, message: '请输入与老人关系' }]}>
        <Input placeholder="例如：儿子、女儿、配偶" />
      </Form.Item>
      <Form.Item label="来访目的" name="visitPurpose" rules={[{ required: true, message: '请输入来访目的' }]}>
        <Input.TextArea rows={4} placeholder="请输入来访目的" />
      </Form.Item>
      <Button type="primary" htmlType="submit" loading={submitting}>
        提交预约
      </Button>
    </Form>
  );
}
