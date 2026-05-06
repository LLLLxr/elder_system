import { useEffect, useMemo, useState } from 'react';
import { Alert, Button, Card, Descriptions, Form, Input, InputNumber, Space, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';
import {
  confirmRenewal,
  declineRenewal,
  getLatestRenewalContextByApplicant,
  submitRenewalReview,
} from '../api/careOrchestrationApi';
import { extractApiErrorMessage } from '../api/client';
import { ROUTE_PATHS } from '../constants/routes';
import { useUserStore } from '../stores/userStore';
import type { RenewalContext, SubmitRenewalReviewRequest } from '../types/care';

type RenewalReviewFormValues = SubmitRenewalReviewRequest;

function formatDate(value?: string) {
  if (!value) {
    return '-';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleDateString();
}

function renderStageLabel(stage?: string) {
  switch (stage) {
    case 'UPCOMING_EXPIRY':
      return '即将到期';
    case 'PENDING_RENEWAL':
      return '待确认续约';
    case 'PENDING_REVIEW':
      return '已评价';
    case 'RENEWED':
      return '已续约';
    case 'TERMINATED':
      return '已结束';
    default:
      return '服务中';
  }
}

export default function JourneyRenewalPage() {
  const navigate = useNavigate();
  const { username: loginUsername } = useUserStore();
  const [form] = Form.useForm<RenewalReviewFormValues>();
  const [loading, setLoading] = useState(false);
  const [submittingReview, setSubmittingReview] = useState(false);
  const [submittingDecision, setSubmittingDecision] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [renewMonths, setRenewMonths] = useState(1);
  const [renewalContext, setRenewalContext] = useState<RenewalContext | null>(null);

  const projectedNextExpiryDate = useMemo(() => {
    if (!renewalContext?.expiryDate) {
      return '-';
    }
    const expiryDate = new Date(renewalContext.expiryDate);
    if (Number.isNaN(expiryDate.getTime())) {
      return renewalContext.expiryDate;
    }
    expiryDate.setMonth(expiryDate.getMonth() + renewMonths);
    return expiryDate.toLocaleDateString();
  }, [renewMonths, renewalContext?.expiryDate]);

  useEffect(() => {
    const load = async () => {
      if (!loginUsername) {
        setErrorMessage('未获取到当前登录用户，请重新登录。');
        setRenewalContext(null);
        return;
      }

      setLoading(true);
      setErrorMessage(null);
      try {
        const result = await getLatestRenewalContextByApplicant(loginUsername);
        setRenewalContext(result);
        form.setFieldsValue({
          agreementId: result.agreementId ?? 0,
          elderId: result.elderId ?? 0,
        });
      } catch (error) {
        setRenewalContext(null);
        setErrorMessage(extractApiErrorMessage(error, '加载续约信息失败'));
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, [form, loginUsername]);

  const handleSubmitReview = async (values: RenewalReviewFormValues) => {
    setSubmittingReview(true);
    setErrorMessage(null);
    try {
      const result = await submitRenewalReview(values);
      setRenewalContext(result);
    } catch (error) {
      setErrorMessage(extractApiErrorMessage(error, '提交满意度评价失败'));
    } finally {
      setSubmittingReview(false);
    }
  };

  const handleConfirmRenewal = async () => {
    if (!renewalContext?.agreementId) {
      return;
    }
    setSubmittingDecision(true);
    setErrorMessage(null);
    try {
      const result = await confirmRenewal(renewalContext.agreementId, renewMonths);
      setRenewalContext(result);
    } catch (error) {
      setErrorMessage(extractApiErrorMessage(error, '确认续约失败'));
    } finally {
      setSubmittingDecision(false);
    }
  };

  const handleDeclineRenewal = async () => {
    if (!renewalContext?.agreementId) {
      return;
    }
    setSubmittingDecision(true);
    setErrorMessage(null);
    try {
      const result = await declineRenewal({
        agreementId: renewalContext.agreementId,
        reason: '家属选择结束本期服务',
      });
      setRenewalContext(result);
    } catch (error) {
      setErrorMessage(extractApiErrorMessage(error, '结束本期服务失败'));
    } finally {
      setSubmittingDecision(false);
    }
  };

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Space style={{ width: '100%', justifyContent: 'space-between' }}>
        <Typography.Title level={4} style={{ margin: 0 }}>
          续约办理
        </Typography.Title>
        <Button onClick={() => navigate(ROUTE_PATHS.JOURNEY_TASKS)}>返回申请进度</Button>
      </Space>

      {errorMessage ? <Alert type="error" showIcon message={errorMessage} /> : null}
      {renewalContext?.message ? <Alert type="info" showIcon message={renewalContext.message} /> : null}

      <Card loading={loading} title="当前续约信息">
        <Typography.Paragraph type="secondary" style={{ marginBottom: 16 }}>
          当前页面可直接办理续约或结束本期服务；满意度评价作为可选信息补充，不再作为前置条件。
        </Typography.Paragraph>
        {renewalContext ? (
          <Descriptions bordered column={2} size="small">
            <Descriptions.Item label="协议ID">{renewalContext.agreementId ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="老人ID">{renewalContext.elderId ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="服务状态">{renewalContext.agreementStatus ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="续约阶段">{renderStageLabel(renewalContext.renewalStage)}</Descriptions.Item>
            <Descriptions.Item label="生效日期">{formatDate(renewalContext.effectiveDate)}</Descriptions.Item>
            <Descriptions.Item label="到期日期">{formatDate(renewalContext.expiryDate)}</Descriptions.Item>
            <Descriptions.Item label="距到期天数">{renewalContext.daysUntilExpiry ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="建议续期至">{formatDate(renewalContext.suggestedNextExpiryDate)}</Descriptions.Item>
            <Descriptions.Item label="最新评分">{renewalContext.latestReviewScore ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="评价结论">{renewalContext.latestReviewConclusion ?? '-'}</Descriptions.Item>
          </Descriptions>
        ) : (
          <Typography.Text type="secondary">暂无续约数据。</Typography.Text>
        )}
      </Card>

      <Card title="满意度评价（可选）">
        <Form<RenewalReviewFormValues>
          form={form}
          layout="vertical"
          onFinish={handleSubmitReview}
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

      <Card title="直接办理">
        <Typography.Paragraph type="secondary" style={{ marginBottom: 16 }}>
          如已确认本期服务安排，可先选择续约月数，再办理下一服务周期或结束本期服务。
        </Typography.Paragraph>
        <Space direction="vertical" size="middle" style={{ width: '100%' }}>
          <Space wrap align="end">
            <div>
              <Typography.Text>续约月数</Typography.Text>
              <InputNumber
                style={{ display: 'block', width: 160, marginTop: 8 }}
                min={1}
                max={12}
                precision={0}
                value={renewMonths}
                onChange={(value) => setRenewMonths(typeof value === 'number' ? value : 1)}
                disabled={!renewalContext?.canRenew || submittingDecision}
              />
            </div>
            <div>
              <Typography.Text type="secondary">预计续期至：{projectedNextExpiryDate}</Typography.Text>
            </div>
          </Space>
          <Space wrap>
            <Button
              type="primary"
              onClick={handleConfirmRenewal}
              loading={submittingDecision}
              disabled={!renewalContext?.canRenew}
            >
              续约下一服务周期
            </Button>
            <Button danger onClick={handleDeclineRenewal} loading={submittingDecision} disabled={!renewalContext?.canTerminate}>
              结束本期服务
            </Button>
            <Button onClick={() => navigate(ROUTE_PATHS.JOURNEY_AGREEMENT)}>查看签约信息</Button>
          </Space>
        </Space>
      </Card>
    </Space>
  );
}
