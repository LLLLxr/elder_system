import { Button, Card, Space, Typography } from 'antd';
import type { IntakeRecord } from '../types/care';
import type { JourneyProgressData } from '../utils/domain/journeyTypes';
import { STEP_TITLES, getStateLabel } from '../utils/domain/journeyTypes';

interface JourneyProgressCardProps {
  selectedRecord: IntakeRecord;
  progress: JourneyProgressData;
  onViewAgreement: () => void;
  onViewRenewal: () => void;
}

export default function JourneyProgressCard({
  selectedRecord,
  progress,
  onViewAgreement,
  onViewRenewal,
}: JourneyProgressCardProps) {
  return (
    <Card size="small" type="inner" title="当前阶段">
      <Space direction="vertical" size="small" style={{ width: '100%' }}>
        <Typography.Text strong>{STEP_TITLES[progress.currentStepKey]}</Typography.Text>
        <Typography.Text>{progress.currentSummary}</Typography.Text>
        <Typography.Text type="secondary">{progress.currentHint}</Typography.Text>
        <Typography.Text type="secondary">
          当前旅程状态：{getStateLabel(selectedRecord.journeyStatus)}
        </Typography.Text>
        {progress.currentStepKey === 'AGREEMENT' &&
        progress.steps.find((item) => item.key === 'AGREEMENT')?.status === 'process' ? (
          <Button onClick={onViewAgreement}>查看签约信息</Button>
        ) : null}
        {selectedRecord.journeyStatus === 'IN_SERVICE' ? (
          <Button onClick={onViewRenewal}>去评价/续约</Button>
        ) : null}
        {selectedRecord.journeyStatus === 'RENEWED' ? (
          <Button onClick={onViewRenewal}>查看续约信息</Button>
        ) : null}
      </Space>
    </Card>
  );
}
