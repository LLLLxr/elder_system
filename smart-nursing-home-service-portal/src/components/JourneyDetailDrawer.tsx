import { Alert, Card, Col, Drawer, Empty, Row, Space, Steps, Typography } from 'antd';
import type { IntakeRecord } from '../types/care';
import type { JourneyProgressData } from '../utils/domain/journeyTypes';
import { STEP_TITLES, getStateLabel, renderStepStatusTag } from '../utils/domain/journeyTypes';
import { formatDateTime } from '../utils/dateFormat';

interface JourneyDetailDrawerProps {
  open: boolean;
  onClose: () => void;
  selectedRecord: IntakeRecord | undefined;
  progress: JourneyProgressData;
  loading: boolean;
}

export default function JourneyDetailDrawer({
  open,
  onClose,
  selectedRecord,
  progress,
  loading,
}: JourneyDetailDrawerProps) {
  return (
    <Drawer title="我的进度详情" width={900} open={open} onClose={onClose} destroyOnClose>
      {!selectedRecord ? (
        <Empty description="当前暂无申请记录" />
      ) : (
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <Alert type={progress.overallAlert.type} showIcon message={progress.overallAlert.message} />

          <Card size="small" title="当前阶段摘要">
            <Row gutter={[16, 16]}>
              <Col span={12}>
                <Typography.Text>当前阶段：{STEP_TITLES[progress.currentStepKey]}</Typography.Text>
              </Col>
              <Col span={12}>
                <Typography.Text>当前状态：{progress.currentSummary}</Typography.Text>
              </Col>
              <Col span={12}>
                <Typography.Text>旅程状态：{getStateLabel(selectedRecord.journeyStatus)}</Typography.Text>
              </Col>
              <Col span={12}>
                <Typography.Text>提交时间：{formatDateTime(selectedRecord.submittedAt)}</Typography.Text>
              </Col>
            </Row>
          </Card>

          <Card size="small" title="旅程步骤">
            {progress.steps.length ? (
              <Steps
                direction="vertical"
                items={progress.steps.map((item) => ({
                  title: item.title,
                  description: `${item.summary} · ${item.timeText}`,
                  status: item.status,
                }))}
              />
            ) : (
              <Typography.Text type="secondary">
                {loading ? '正在加载旅程步骤...' : '暂无旅程步骤'}
              </Typography.Text>
            )}
          </Card>

          <Card size="small" title="阶段说明">
            <Space direction="vertical" size="middle" style={{ width: '100%' }}>
              {progress.steps.map((item) => (
                <Card key={item.key} size="small">
                  <Space direction="vertical" size="small" style={{ width: '100%' }}>
                    <Space style={{ justifyContent: 'space-between', width: '100%' }} wrap>
                      <Typography.Text strong>{item.title}</Typography.Text>
                      {renderStepStatusTag(item.status)}
                    </Space>
                    <Typography.Text>{item.summary}</Typography.Text>
                    <Typography.Text type="secondary">{item.hint}</Typography.Text>
                    <Typography.Text type="secondary">{item.timeText}</Typography.Text>
                  </Space>
                </Card>
              ))}
            </Space>
          </Card>
        </Space>
      )}
    </Drawer>
  );
}
