import { Card, Descriptions, Typography } from 'antd';
import { formatDate } from '../utils/dateFormat';
import type { RenewalContext } from '../types/care';

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

interface RenewalInfoCardProps {
  loading: boolean;
  renewalContext: RenewalContext | null;
}

export default function RenewalInfoCard({ loading, renewalContext }: RenewalInfoCardProps) {
  return (
    <Card loading={loading} title="当前续约信息">
      <Typography.Paragraph type="secondary" style={{ marginBottom: 16 }}>
        当前页面可直接办理续约或结束本期服务；满意度评价作为可选信息补充，不再作为前置条件。
      </Typography.Paragraph>
      {renewalContext ? (
        <Descriptions bordered column={2} size="small">
          <Descriptions.Item label="协议编号">{renewalContext.agreementId ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="老人编号">{renewalContext.elderId ?? '-'}</Descriptions.Item>
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
  );
}
