import { Alert, Card, Descriptions, Typography } from 'antd';
import { AgreementJourneyStatusTag } from './CareStatusTag';
import { formatDateTime } from '../utils/dateFormat';
import type { JourneyTransitionLogItem, RenewalContext, ServiceAgreement, ServiceJourneyResult } from '../types/care';

interface AgreementStatusCardProps {
  loading: boolean;
  journeyResult: ServiceJourneyResult | null;
  agreement: ServiceAgreement | null;
  renewalContext: RenewalContext | null;
  latestAgreementLog: JourneyTransitionLogItem | null;
  waitingAgreement: boolean;
}

export default function AgreementStatusCard({
  loading,
  journeyResult,
  agreement,
  renewalContext,
  latestAgreementLog,
  waitingAgreement,
}: AgreementStatusCardProps) {
  if (!loading && !journeyResult) {
    return <Alert type="warning" showIcon message="暂无签约信息" description="当前登录用户暂无可查看的申请结果。" />;
  }

  return (
    <Card loading={loading} title="当前签约状态">
      {journeyResult ? (
        <Descriptions bordered column={2} size="small">
          <Descriptions.Item label="申请单编号">{journeyResult.applicationId ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="协议编号">{agreement?.agreementId ?? journeyResult.agreementId ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="老人编号">{agreement?.elderId ?? journeyResult.elderId ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="当前状态"><AgreementJourneyStatusTag status={journeyResult.finalStatus} /></Descriptions.Item>
          <Descriptions.Item label="服务场景">{agreement?.serviceScene ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="协议状态">{agreement?.status ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="签约人">{agreement?.signedBy ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="生效日期">{agreement?.effectiveDate ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="到期日期">{agreement?.expiryDate ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="距到期天数">{renewalContext?.daysUntilExpiry ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="续约阶段">{renewalContext?.renewalStage ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="签约结果" span={2}>
            {latestAgreementLog
              ? `已完成签约，时间：${formatDateTime(latestAgreementLog.transitionTime)}`
              : waitingAgreement
                ? '等待平台安排签约'
                : agreement?.status === 'ACTIVE'
                  ? '协议已生效，请留意后续服务安排'
                  : (journeyResult.message ?? '请以最新旅程结果为准')}
          </Descriptions.Item>
        </Descriptions>
      ) : (
        <Typography.Text type="secondary">暂无签约状态数据。</Typography.Text>
      )}
    </Card>
  );
}
