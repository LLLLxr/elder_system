import { Button, Card, InputNumber, Space, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';
import { ROUTE_PATHS } from '../constants/routes';
import type { RenewalContext } from '../types/care';

interface RenewalActionCardProps {
  renewalContext: RenewalContext | null;
  renewMonths: number;
  projectedNextExpiryDate: string;
  submittingDecision: boolean;
  onRenewMonthsChange: (value: number) => void;
  onConfirmRenewal: () => void;
  onDeclineRenewal: () => void;
}

export default function RenewalActionCard({
  renewalContext,
  renewMonths,
  projectedNextExpiryDate,
  submittingDecision,
  onRenewMonthsChange,
  onConfirmRenewal,
  onDeclineRenewal,
}: RenewalActionCardProps) {
  const navigate = useNavigate();

  return (
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
              onChange={(value) => onRenewMonthsChange(typeof value === 'number' ? value : 1)}
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
            onClick={onConfirmRenewal}
            loading={submittingDecision}
            disabled={!renewalContext?.canRenew}
          >
            续约下一服务周期
          </Button>
          <Button danger onClick={onDeclineRenewal} loading={submittingDecision} disabled={!renewalContext?.canTerminate}>
            结束本期服务
          </Button>
          <Button onClick={() => navigate(ROUTE_PATHS.JOURNEY_AGREEMENT)}>查看签约信息</Button>
        </Space>
      </Space>
    </Card>
  );
}
