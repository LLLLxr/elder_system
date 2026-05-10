import { useEffect, useMemo, useState } from 'react';
import { confirmRenewal, declineRenewal, getLatestRenewalContextByApplicant, submitRenewalReview } from '../api/careOrchestrationApi';
import { extractApiErrorMessage } from '../api/client';
import type { RenewalContext, SubmitRenewalReviewRequest } from '../types/care';

export function useRenewalContext(loginUsername?: string) {
  const [loading, setLoading] = useState(false);
  const [submittingReview, setSubmittingReview] = useState(false);
  const [submittingDecision, setSubmittingDecision] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [renewMonths, setRenewMonths] = useState(1);
  const [renewalContext, setRenewalContext] = useState<RenewalContext | null>(null);

  const projectedNextExpiryDate = useMemo(() => {
    if (!renewalContext?.expiryDate) {
      return '-';
    }
    const expiryDate = new Date(renewalContext.expiryDate);
    if (Number.isNaN(expiryDate.getTime())) {
      return renewalContext.expiryDate;
    }
    expiryDate.setMonth(expiryDate.getMonth() + renewMonths);
    return expiryDate.toLocaleDateString();
  }, [renewMonths, renewalContext?.expiryDate]);

  useEffect(() => {
    const load = async () => {
      if (!loginUsername) {
        setErrorMessage('未获取到当前登录用户，请重新登录。');
        setRenewalContext(null);
        return;
      }

      setLoading(true);
      setErrorMessage(null);
      try {
        const result = await getLatestRenewalContextByApplicant(loginUsername);
        setRenewalContext(result);
      } catch (error) {
        setRenewalContext(null);
        setErrorMessage(extractApiErrorMessage(error, '加载续约信息失败'));
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, [loginUsername]);

  const handleSubmitReview = async (values: SubmitRenewalReviewRequest) => {
    setSubmittingReview(true);
    setErrorMessage(null);
    try {
      const result = await submitRenewalReview(values);
      setRenewalContext(result);
    } catch (error) {
      setErrorMessage(extractApiErrorMessage(error, '提交满意度评价失败'));
    } finally {
      setSubmittingReview(false);
    }
  };

  const handleConfirmRenewal = async () => {
    if (!renewalContext?.agreementId) {
      return;
    }
    setSubmittingDecision(true);
    setErrorMessage(null);
    try {
      const result = await confirmRenewal(renewalContext.agreementId, renewMonths);
      setRenewalContext(result);
    } catch (error) {
      setErrorMessage(extractApiErrorMessage(error, '确认续约失败'));
    } finally {
      setSubmittingDecision(false);
    }
  };

  const handleDeclineRenewal = async () => {
    if (!renewalContext?.agreementId) {
      return;
    }
    setSubmittingDecision(true);
    setErrorMessage(null);
    try {
      const result = await declineRenewal({
        agreementId: renewalContext.agreementId,
        reason: '家属选择结束本期服务',
      });
      setRenewalContext(result);
    } catch (error) {
      setErrorMessage(extractApiErrorMessage(error, '结束本期服务失败'));
    } finally {
      setSubmittingDecision(false);
    }
  };

  return {
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
  };
}
