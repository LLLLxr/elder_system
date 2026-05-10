import { useEffect, useState } from 'react';
import { Alert, Button, Card, Space, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';
import { getJourneyOverview } from '../api/careOrchestrationApi';
import { extractApiErrorMessage } from '../api/client';
import JourneyPageScaffold from '../components/JourneyPageScaffold';
import { ROUTE_PATHS } from '../constants/routes';
import { canAccessAssessmentReview } from '../stores/userStore';

export default function JourneyOverviewPage() {
  const navigate = useNavigate();
  const canReviewAssessment = canAccessAssessmentReview();
  const [overview, setOverview] = useState('');
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setErrorMessage(null);
      try {
        const text = await getJourneyOverview();
        setOverview(text);
      } catch (error) {
        const message = extractApiErrorMessage(error, '获取旅程概览失败');
        setErrorMessage(message);
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, []);

  return (
    <JourneyPageScaffold
      title="入院服务总览"
      description="从申请发起、评估、签约到在院服务，按旅程查看当前可办理事项。"
      actions={
        <Space>
          <Button type="primary" onClick={() => navigate(ROUTE_PATHS.JOURNEY_START)}>
            发起申请
          </Button>
          {canReviewAssessment ? <Button onClick={() => navigate(ROUTE_PATHS.JOURNEY_REVIEW)}>提交评价收尾</Button> : null}
        </Space>
      }
    >
      {errorMessage ? <Alert type="error" message={errorMessage} showIcon /> : null}

      <Card loading={loading} title="旅程说明">
        <Typography.Paragraph style={{ marginBottom: 0 }}>
          {overview || '正在加载旅程说明...'}
        </Typography.Paragraph>
      </Card>
    </JourneyPageScaffold>
  );
}
