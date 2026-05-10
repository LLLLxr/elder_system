import { Alert, Button, Space } from 'antd';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getLatestJourneyResultByApplicant } from '../api/careOrchestrationApi';
import { listHealthCheckForms } from '../api/healthApi';
import JourneyPageScaffold from '../components/JourneyPageScaffold';
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
          const forms = await listHealthCheckForms(latest.elderId, latest.agreementId);
          setHealthChecked(forms.length > 0);
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
  const waitingAgreement = journeyResult?.finalStatus === 'PENDING_AGREEMENT';
  const inService = journeyResult?.finalStatus === 'IN_SERVICE';
  const renewed = journeyResult?.finalStatus === 'RENEWED';
  const canRenewJourney = inService || renewed;

  return (
    <JourneyPageScaffold
      title="申请结果"
      description="查看最新申请结果，并按当前状态进入签约、续约或评价收尾。"
      actions={
        <Space>
          <Button type="primary" onClick={() => navigate(ROUTE_PATHS.JOURNEY_START)}>
            再次发起申请
          </Button>
          {waitingAgreement ? <Button onClick={() => navigate(ROUTE_PATHS.JOURNEY_AGREEMENT)}>签约信息</Button> : null}
          {inService ? <Button onClick={() => navigate(ROUTE_PATHS.JOURNEY_RENEWAL)}>去评价/续约</Button> : null}
          {renewed ? <Button onClick={() => navigate(ROUTE_PATHS.JOURNEY_RENEWAL)}>查看续约信息</Button> : null}
          {canReviewAssessment ? (
            <Button disabled={waitingAssessment} onClick={() => navigate(ROUTE_PATHS.JOURNEY_REVIEW)}>
              继续评价收尾
            </Button>
          ) : null}
        </Space>
      }
    >

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
              查看健康体检结果
            </Button>
          }
        />
      ) : null}

      {waitingAssessment ? (
        <Alert type="info" showIcon message="需求评估待处理，管理端评估完成后将自动继续签订服务协议。" />
      ) : null}

      {waitingAgreement ? (
        <Alert
          type="info"
          showIcon
          message="健康评估已完成，当前正在等待签约安排。"
          description="当前阶段点击“进入云签字”会临时直接视为签约完成，后续将替换为真实第三方云签流程。"
          action={
            <Button size="small" onClick={() => navigate(ROUTE_PATHS.JOURNEY_AGREEMENT)}>
              查看签约信息
            </Button>
          }
        />
      ) : null}

      {inService ? (
        <Alert
          type="success"
          showIcon
          message="当前服务进行中。"
          description="可前往“评价/续约”页面直接续约、结束本期服务，或按需补充提交满意度评价。"
          action={
            <Button size="small" onClick={() => navigate(ROUTE_PATHS.JOURNEY_RENEWAL)}>
              去评价/续约
            </Button>
          }
        />
      ) : null}

      {renewed ? (
        <Alert
          type="success"
          showIcon
          message="当前服务已完成续约。"
          description="如下一周期临近到期，仍可从“评价/续约”页面继续处理新的续约决策。"
          action={
            <Button size="small" onClick={() => navigate(ROUTE_PATHS.JOURNEY_RENEWAL)}>
              查看续约信息
            </Button>
          }
        />
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

    </JourneyPageScaffold>
  );
}
