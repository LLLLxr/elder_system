import { Button, Card, Form, Input, InputNumber } from 'antd';
import type { RenewalContext, SubmitRenewalReviewRequest } from '../types/care';

interface RenewalReviewFormProps {
  renewalContext: RenewalContext | null;
  submittingReview: boolean;
  onSubmit: (values: SubmitRenewalReviewRequest) => void;
}

export default function RenewalReviewForm({ renewalContext, submittingReview, onSubmit }: RenewalReviewFormProps) {
  const [form] = Form.useForm<SubmitRenewalReviewRequest>();

  return (
    <Card title="满意度评价（可选）">
      <Form<SubmitRenewalReviewRequest>
        form={form}
        layout="vertical"
        onFinish={onSubmit}
        initialValues={{
          agreementId: renewalContext?.agreementId,
          elderId: renewalContext?.elderId,
        }}
      >
        <Form.Item name="agreementId" hidden>
          <InputNumber />
        </Form.Item>
        <Form.Item name="elderId" hidden>
          <InputNumber />
        </Form.Item>
        <Form.Item
          label="满意度评分"
          name="satisfactionScore"
          rules={[{ required: true, message: '请输入满意度评分' }]}
        >
          <InputNumber style={{ width: '100%' }} min={0} max={100} placeholder="0-100" />
        </Form.Item>
        <Form.Item label="评价备注" name="reviewComment">
          <Input.TextArea rows={4} placeholder="请输入本周期满意度评价" />
        </Form.Item>
        <Button
          type="primary"
          htmlType="submit"
          loading={submittingReview}
          disabled={!renewalContext?.canReview}
        >
          提交满意度评价
        </Button>
      </Form>
    </Card>
  );
}
