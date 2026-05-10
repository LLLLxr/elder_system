import { Alert, Button } from 'antd';
import { useNavigate } from 'react-router-dom';
import JourneyPageScaffold from '../components/JourneyPageScaffold';
import AgreementStatusCard from '../components/AgreementStatusCard';
import AgreementGuideCard from '../components/AgreementGuideCard';
import AgreementLogsCard from '../components/AgreementLogsCard';
import { ROUTE_PATHS } from '../constants/routes';
import { useUserStore } from '../stores/userStore';
import { useAgreementData } from '../hooks/useAgreementData';

export default function JourneyAgreementPage() {
  const navigate = useNavigate();
  const { username: loginUsername } = useUserStore();

  const {
    loading,
    signing,
    errorMessage,
    cloudSignMessage,
    journeyResult,
    agreement,
    renewalContext,
    logs,
    latestAgreementLog,
    waitingAgreement,
    agreementCompleted,
    handleCloudSignPlaceholder,
  } = useAgreementData(loginUsername);

  return (
    <JourneyPageScaffold
      title="签约信息"
      description="查看协议生成、签约办理指引和签约阶段流转记录。"
      actions={<Button onClick={() => navigate(ROUTE_PATHS.JOURNEY_TASKS)}>返回申请进度</Button>}
    >
      {errorMessage ? <Alert type="error" showIcon message={errorMessage} /> : null}
      {cloudSignMessage ? <Alert type="warning" showIcon message={cloudSignMessage} /> : null}

      {waitingAgreement ? (
        <Alert
          type="info"
          showIcon
          message="当前申请已进入待签约阶段，请等待平台安排签约。"
          description="签约完成后，这里会显示签约结果与相关流转记录。"
        />
      ) : null}

      <AgreementStatusCard
        loading={loading}
        journeyResult={journeyResult}
        agreement={agreement}
        renewalContext={renewalContext}
        latestAgreementLog={latestAgreementLog}
        waitingAgreement={waitingAgreement}
      />

      <AgreementGuideCard
        waitingAgreement={waitingAgreement}
        agreementCompleted={agreementCompleted}
        agreement={agreement}
        signing={signing}
        onCloudSign={handleCloudSignPlaceholder}
      />

      <AgreementLogsCard loading={loading} logs={logs} />
    </JourneyPageScaffold>
  );
}
