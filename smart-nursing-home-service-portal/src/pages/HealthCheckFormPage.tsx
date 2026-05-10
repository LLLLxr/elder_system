import { Alert, Card, Space, Typography } from 'antd';
import { useLocation } from 'react-router-dom';
import { HealthCheckFormDetailModal } from 'common-react';
import LatestHealthCheckCard from '../components/LatestHealthCheckCard';
import HealthCheckHistorySection from '../components/HealthCheckHistorySection';
import { useHealthCheckData } from '../hooks/useHealthCheckData';

export default function HealthCheckFormPage() {
  const location = useLocation();

  const journeyState = location.state as
    | { elderId?: number; agreementId?: number; elderName?: string; fromJourney?: boolean }
    | undefined;

  const elderId = journeyState?.elderId;
  const agreementId = journeyState?.agreementId;
  const elderName = journeyState?.elderName;
  const fromJourney = Boolean(journeyState?.fromJourney);

  const {
    loading,
    historyLoading,
    latestForm,
    historyList,
    errorMessage,
    historyError,
    historyDetailOpen,
    historyDetailLoading,
    historyDetailError,
    historyDetail,
    setHistoryDetailOpen,
    handleOpenHistoryDetail,
  } = useHealthCheckData(elderId, agreementId);

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <div>
        <Typography.Title level={4} style={{ margin: 0 }}>
          健康体检结果
        </Typography.Title>
      </div>

      <Card>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <Alert
            type="info"
            showIcon
            message="健康体检表由管理端护士或责任医生填写，当前页面仅供查看结果。"
          />

          <LatestHealthCheckCard
            loading={loading}
            latestForm={latestForm}
            errorMessage={errorMessage}
            fromJourney={fromJourney}
            elderId={elderId}
            agreementId={agreementId}
            elderName={elderName}
            onViewDetail={handleOpenHistoryDetail}
          />

          <HealthCheckHistorySection
            historyLoading={historyLoading}
            historyList={historyList}
            historyError={historyError}
            onViewDetail={handleOpenHistoryDetail}
          />

          <HealthCheckFormDetailModal
            open={historyDetailOpen}
            loading={historyDetailLoading}
            error={historyDetailError}
            detail={historyDetail}
            onCancel={() => setHistoryDetailOpen(false)}
          />
        </Space>
      </Card>
    </Space>
  );
}
