import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Alert, Button, Card, Form, Select } from 'antd';
import JourneyPageScaffold from '../components/JourneyPageScaffold';
import { useUserStore } from '../stores/userStore';
import { ROUTE_PATHS } from '../constants/routes';
import { JourneyStateMachine } from '../utils/domain/JourneyStateMachine';
import { useJourneyData } from '../hooks/useJourneyData';
import JourneyOverviewStats from '../components/JourneyOverviewStats';
import JourneyProgressCard from '../components/JourneyProgressCard';
import JourneyStepsTable from '../components/JourneyStepsTable';
import JourneyDetailDrawer from '../components/JourneyDetailDrawer';

const AUTO_REFRESH_INTERVAL_MS = 30000;

export default function JourneyTaskBoardPage() {
  const navigate = useNavigate();
  const [filterForm] = Form.useForm<{ applicationId?: number }>();
  const { username: loginUsername } = useUserStore();
  const [applicationId, setApplicationId] = useState<number | undefined>();
  const [detailOpen, setDetailOpen] = useState(false);

  const { mineOptions, mineRecords, overview, timeline, loading, error, refresh } = useJourneyData(
    loginUsername,
    applicationId
  );

  const selectedRecord = useMemo(
    () => mineRecords.find((record) => record.applicationId === applicationId),
    [mineRecords, applicationId]
  );

  const progress = useMemo(
    () => new JourneyStateMachine(selectedRecord, timeline).getProgress(),
    [selectedRecord, timeline]
  );

  useEffect(() => {
    if (mineOptions.length > 0 && !applicationId) {
      const defaultId = mineOptions[mineOptions.length - 1].value;
      setApplicationId(defaultId);
      filterForm.setFieldsValue({ applicationId: defaultId });
    }
  }, [mineOptions, applicationId, filterForm]);

  useEffect(() => {
    if (!applicationId) return;
    const timer = window.setInterval(() => {
      if (document.visibilityState === 'visible') {
        refresh();
      }
    }, AUTO_REFRESH_INTERVAL_MS);
    return () => window.clearInterval(timer);
  }, [applicationId, refresh]);

  const handleApplicationChange = (newApplicationId?: number) => {
    setApplicationId(newApplicationId);
  };

  return (
    <JourneyPageScaffold
      title="申请进度"
      description="按申请查看当前办理进度，重点关注现在走到哪一步、下一步需要等待什么。"
      actions={
        <Button onClick={refresh} disabled={!applicationId}>
          刷新
        </Button>
      }
    >
      {selectedRecord ? <Alert type={progress.overallAlert.type} message={progress.overallAlert.message} showIcon /> : null}
      {error ? <Alert type="error" message={error} showIcon /> : null}

      <JourneyOverviewStats overview={overview} loading={loading} />

      <Card title="当前申请">
        <Form form={filterForm} layout="vertical">
          <Row gutter={16}>
            <Col xs={24} md={16} lg={12}>
              <Form.Item name="applicationId" label="我的申请" rules={[{ required: true, message: '请选择申请' }]}>
                <Select
                  options={mineOptions}
                  placeholder="选择当前登录用户的申请"
                  onChange={handleApplicationChange}
                />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Card>

      {!mineOptions.length ? (
        <Alert type="info" showIcon message="当前登录用户名下暂无申请记录，暂时没有可查看的任务。" />
      ) : null}

      <Card title="申请进度">
        {!selectedRecord ? (
          <Empty description={mineOptions.length ? '请选择一条申请后查看进度' : '当前暂无申请记录'} />
        ) : (
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            <JourneyProgressCard
              selectedRecord={selectedRecord}
              progress={progress}
              onViewAgreement={() => navigate(ROUTE_PATHS.JOURNEY_AGREEMENT)}
              onViewRenewal={() => navigate(ROUTE_PATHS.JOURNEY_RENEWAL)}
            />

            <JourneyStepsTable
              steps={progress.steps}
              loading={loading}
              onViewDetail={() => setDetailOpen(true)}
              onViewAgreement={() => navigate(ROUTE_PATHS.JOURNEY_AGREEMENT)}
            />
          </Space>
        )}
      </Card>

      <JourneyDetailDrawer
        open={detailOpen}
        onClose={() => setDetailOpen(false)}
        selectedRecord={selectedRecord}
        progress={progress}
        loading={loading}
      />
    </JourneyPageScaffold>
  );
}
