import { useEffect, useMemo, useState } from 'react';
import { continueJourneyAfterAssessment, getLatestJourneyResultByApplicant, getLatestRenewalContextByApplicant, listJourneyTransitionLogsByAgreement } from '../api/careOrchestrationApi';
import { extractApiErrorMessage } from '../api/client';
import { getAgreement, getLatestAgreementByApplicationId, signAgreement } from '../api/contractApi';
import type { JourneyTransitionLogItem, RenewalContext, ServiceAgreement, ServiceJourneyResult } from '../types/care';

export function useAgreementData(loginUsername?: string) {
  const [loading, setLoading] = useState(false);
  const [signing, setSigning] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [cloudSignMessage, setCloudSignMessage] = useState<string | null>(null);
  const [journeyResult, setJourneyResult] = useState<ServiceJourneyResult | null>(null);
  const [agreement, setAgreement] = useState<ServiceAgreement | null>(null);
  const [renewalContext, setRenewalContext] = useState<RenewalContext | null>(null);
  const [logs, setLogs] = useState<JourneyTransitionLogItem[]>([]);

  useEffect(() => {
    const load = async () => {
      if (!loginUsername) {
        setJourneyResult(null);
        setAgreement(null);
        setLogs([]);
        setErrorMessage('未获取到当前登录用户，请重新登录。');
        return;
      }

      setLoading(true);
      setErrorMessage(null);
      try {
        const [latest, latestRenewal] = await Promise.all([
          getLatestJourneyResultByApplicant(loginUsername),
          getLatestRenewalContextByApplicant(loginUsername),
        ]);
        setJourneyResult(latest);
        setRenewalContext(latestRenewal);

        let resolvedAgreement: ServiceAgreement | null = null;
        if (latest?.agreementId) {
          const [agreementData, agreementLogs] = await Promise.all([
            getAgreement(latest.agreementId),
            listJourneyTransitionLogsByAgreement(latest.agreementId),
          ]);
          resolvedAgreement = agreementData;
          setLogs(agreementLogs);
        } else if (latest?.applicationId) {
          try {
            resolvedAgreement = await getLatestAgreementByApplicationId(latest.applicationId);
          } catch {
            resolvedAgreement = null;
          }
          setLogs([]);
        } else {
          setLogs([]);
        }

        setAgreement(resolvedAgreement);
      } catch (error) {
        setJourneyResult(null);
        setAgreement(null);
        setLogs([]);
        setErrorMessage(extractApiErrorMessage(error, '加载签约信息失败'));
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, [loginUsername]);

  const latestAgreementLog = useMemo(
    () => logs.find((item) => item.journeyEvent === 'AGREEMENT_SIGNED') ?? null,
    [logs],
  );

  const waitingAgreement = journeyResult?.finalStatus === 'PENDING_AGREEMENT';
  const agreementCompleted = agreement?.status === 'ACTIVE' || journeyResult?.finalStatus === 'IN_SERVICE';

  const handleCloudSignPlaceholder = async () => {
    if (!agreement?.agreementId) {
      setErrorMessage('当前缺少协议信息，暂不能完成签约。');
      return;
    }

    setSigning(true);
    setErrorMessage(null);
    setCloudSignMessage(null);
    try {
      const signedAgreement = await signAgreement({
        agreementId: agreement.agreementId,
        signedBy: loginUsername ?? 'family-user',
      });
      setAgreement(signedAgreement);

      const [agreementLogs, latestResult, latestRenewal] = await Promise.all([
        signedAgreement.agreementId
          ? listJourneyTransitionLogsByAgreement(signedAgreement.agreementId)
          : Promise.resolve(null),
        journeyResult?.applicationId
          ? continueJourneyAfterAssessment({ applicationId: journeyResult.applicationId })
          : Promise.resolve(null),
        loginUsername
          ? getLatestRenewalContextByApplicant(loginUsername)
          : Promise.resolve(null),
      ]);

      if (agreementLogs) {
        setLogs(agreementLogs);
      }
      if (latestResult) {
        setJourneyResult(latestResult);
      }
      if (latestRenewal) {
        setRenewalContext(latestRenewal);
      }

      setCloudSignMessage('当前按钮已临时视为签约完成；后续接入第三方云签字后再替换为真实流程。');
    } catch (error) {
      setErrorMessage(extractApiErrorMessage(error, '临时签约完成失败'));
    } finally {
      setSigning(false);
    }
  };

  return {
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
  };
}
