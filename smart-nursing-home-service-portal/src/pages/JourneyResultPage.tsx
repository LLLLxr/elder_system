import { Alert, Button, Space, Typography } from 'antd';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getLatestJourneyResultByApplicant } from '../api/careOrchestrationApi';
import { getLatestHealthCheckForm } from '../api/healthApi';
import { canAccessAssessmentReview, useUserStore } from '../stores/userStore';
import { JourneyStatusAlert, ResultCard } from 'common-react';
import { ROUTE_PATHS } from '../constants/routes';
import type { ServiceJourneyResult } from '../types/care';

export default function JourneyResultPage() {
  const navigate = useNavigate();
  const { username: loginUsername, userId: currentUserId } = useUserStore();
  const canReviewAssessment = canAccessAssessmentReview();

  const [healthChecked, setHealthChecked] = useState(true);
  const [journeyResult, setJourneyResult] = useState<ServiceJourneyResult | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const loadLatest = async () => {
      if (!loginUsername) {
        setJourneyResult(null);
        return;
      }

      setLoading(true);
      try {
        const latest = await getLatestJourneyResultByApplicant(loginUsername);
        if (!latest?.applicationId) {
          setJourneyResult(null);
          return;
        }

        setJourneyResult(latest);

        if (!latest.elderId || !latest.agreementId) {
          setHealthChecked(true);
          return;
        }

        try {
          await getLatestHealthCheckForm(latest.elderId, latest.agreementId);
          setHealthChecked(true);
        } catch {
          setHealthChecked(false);
        }
      } finally {
        setLoading(false);
      }
    };

    void loadLatest();
  }, [currentUserId, loginUsername]);

  const waitingAssessment = journeyResult?.finalStatus === 'PENDING_ASSESSMENT';

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Typography.Title level={4} style={{ margin: 0 }}>
        旅程结果
      </Typography.Title>

      {journeyResult && !healthChecked ? (
        <Alert
          type="warning"
          showIcon
          message="管理端尚未录入健康体检表，请等待护士或责任医生完成录入。"
          action={
            <Button
              size="small"
              onClick={() =>
                navigate(ROUTE_PATHS.HEALTH_CHECK, {
                  state: {
                    elderId: journeyResult.elderId,
                    agreementId: journeyResult.agreementId,
                    fromJourney: true,
                  },
                })
              }
            >
              查看体检结果页
            </Button>
          }
        />
      ) : null}

      {waitingAssessment ? (
        <Alert type="info" showIcon message="需求评估待处理，管理端评估完成后将自动继续签订服务协议。" />
      ) : null}

      {!loginUsername ? (
        <Alert type="error" showIcon message="未获取到当前登录用户，请重新登录。" />
      ) : null}

      {loading ? <Alert type="info" showIcon message="正在加载最新旅程结果..." /> : null}

      {!loading && !journeyResult ? (
        <Alert
          type="warning"
          showIcon
          message="暂无结果数据"
          description="当前登录用户暂无申请记录。"
        />
      ) : null}

      {!loading && journeyResult ? (
        <>
          <ResultCard result={journeyResult} />
          <JourneyStatusAlert status={journeyResult.finalStatus} />
        </>
      ) : null}

      <Space>
        <Button type="primary" onClick={() => navigate(ROUTE_PATHS.JOURNEY_START)}>
          再次发起旅程
        </Button>
        {canReviewAssessment ? (
          <Button disabled={waitingAssessment} onClick={() => navigate(ROUTE_PATHS.JOURNEY_REVIEW)}>
            继续评价收尾
          </Button>
        ) : null}
      </Space>
    </Space>
  );
}
