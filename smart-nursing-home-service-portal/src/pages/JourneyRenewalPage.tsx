import { Alert, Button } from 'antd';
import { useNavigate } from 'react-router-dom';
import JourneyPageScaffold from '../components/JourneyPageScaffold';
import RenewalInfoCard from '../components/RenewalInfoCard';
import RenewalReviewForm from '../components/RenewalReviewForm';
import RenewalActionCard from '../components/RenewalActionCard';
import { ROUTE_PATHS } from '../constants/routes';
import { useUserStore } from '../stores/userStore';
import { useRenewalContext } from '../hooks/useRenewalContext';

export default function JourneyRenewalPage() {
  const navigate = useNavigate();
  const { username: loginUsername } = useUserStore();

  const {
    loading,
    submittingReview,
    submittingDecision,
    errorMessage,
    renewMonths,
    setRenewMonths,
    renewalContext,
    projectedNextExpiryDate,
    handleSubmitReview,
    handleConfirmRenewal,
    handleDeclineRenewal,
  } = useRenewalContext(loginUsername);

  return (
    <JourneyPageScaffold
      title="续约办理"
      description="查看当前协议到期情况，补充满意度评价，并直接办理续约或结束本期服务。"
      actions={<Button onClick={() => navigate(ROUTE_PATHS.JOURNEY_TASKS)}>返回申请进度</Button>}
    >
      {errorMessage ? <Alert type="error" showIcon message={errorMessage} /> : null}
      {renewalContext?.message ? <Alert type="info" showIcon message={renewalContext.message} /> : null}

      <RenewalInfoCard loading={loading} renewalContext={renewalContext} />

      <RenewalReviewForm
        renewalContext={renewalContext}
        submittingReview={submittingReview}
        onSubmit={handleSubmitReview}
      />

      <RenewalActionCard
        renewalContext={renewalContext}
        renewMonths={renewMonths}
        projectedNextExpiryDate={projectedNextExpiryDate}
        submittingDecision={submittingDecision}
        onRenewMonthsChange={setRenewMonths}
        onConfirmRenewal={handleConfirmRenewal}
        onDeclineRenewal={handleDeclineRenewal}
      />
    </JourneyPageScaffold>
  );
}
