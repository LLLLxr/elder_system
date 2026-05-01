import { useEffect, useMemo, useState } from 'react';
import { Alert, Card, Form, Input, InputNumber, Space, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';
import { reviewAndFinalize } from '../api/careOrchestrationApi';
import { extractApiErrorMessage } from '../api/client';
import { getLatestHealthCheckForm } from '../api/healthApi';
import { ROUTE_PATHS } from '../constants/routes';
import type { ReviewAndFinalizeRequest } from '../types/care';

export default function JourneyReviewPage() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [healthChecked, setHealthChecked] = useState(false);
  const [assessmentReady, setAssessmentReady] = useState(false);

  const context = useMemo<
    { elderId?: number; applicationId?: number; agreementId?: number; elderName?: string } | null
  >(() => {
    const raw = sessionStorage.getItem('journeyContext');
    if (!raw) {
      return null;
    }

    try {
      return JSON.parse(raw) as {
        elderId?: number;
        applicationId?: number;
        agreementId?: number;
        elderName?: string;
      };
    } catch {
      return null;
    }
  }, []);

  useEffect(() => {
    const verify = async () => {
      if (!context?.elderId) {
        setHealthChecked(true);
        setAssessmentReady(true);
        return;
      }

      try {
        await getLatestHealthCheckForm(context.elderId, context.agreementId);
        setHealthChecked(true);
      } catch {
        setHealthChecked(false);
      }

      if (context?.agreementId) {
        setAssessmentReady(true);
      }
    };

    void verify();
  }, [context, navigate]);

  const handleSubmit = async (values: ReviewAndFinalizeRequest) => {
    if (!healthChecked) {
      setErrorMessage('请先完成健康体检表');
      return;
    }

    if (!assessmentReady) {
      setErrorMessage('需求评估未完成，暂不能进入签约后环节');
      return;
    }

    setLoading(true);
    setErrorMessage(null);

    try {
      const result = await reviewAndFinalize(values);
      sessionStorage.setItem('journeyResult', JSON.stringify(result));
      navigate(ROUTE_PATHS.JOURNEY_RESULT, { replace: true });
    } catch (error) {
      const message = extractApiErrorMessage(error, '提交评价失败');
      setErrorMessage(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Typography.Title level={4} style={{ margin: 0 }}>
        评价收尾
      </Typography.Title>

      <Card>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          {errorMessage ? <Alert type="error" message={errorMessage} showIcon /> : null}
          {!healthChecked ? (
            <Alert
              type="warning"
              showIcon
              message="管理端尚未录入健康体检表，暂不能进行评价收尾。"
              description="请等待护士或责任医生在管理端完成体检表录入。"
            />
          ) : null}
          {!assessmentReady ? <Alert type="info" showIcon message="需求评估完成后，才可进入签约与后续流程。" /> : null}

          <Form<ReviewAndFinalizeRequest> layout="vertical" onFinish={handleSubmit}>
            <Form.Item
              label="协议ID"
              name="agreementId"
              rules={[{ required: true, message: '请输入协议ID' }]}
            >
              <InputNumber style={{ width: '100%' }} min={1} placeholder="例如：30001" />
            </Form.Item>

            <Form.Item label="老人ID" name="elderId" rules={[{ required: true, message: '请输入老人ID' }]}>
              <InputNumber style={{ width: '100%' }} min={1} placeholder="例如：10001" />
            </Form.Item>

            <Form.Item
              label="满意度评分"
              name="satisfactionScore"
              rules={[{ required: true, message: '请输入满意度评分' }]}
            >
              <InputNumber style={{ width: '100%' }} min={0} max={100} placeholder="0-100" />
            </Form.Item>

            <Form.Item label="评价备注（可选）" name="reviewComment">
              <Input.TextArea rows={4} placeholder="请输入评价备注" />
            </Form.Item>

            <Button type="primary" htmlType="submit" loading={loading} disabled={!healthChecked || !assessmentReady}>
              提交评价并收尾
            </Button>
          </Form>
        </Space>
      </Card>
    </Space>
  );
}
