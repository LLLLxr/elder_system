import { useEffect, useState } from 'react';
import { getHealthCheckForm, getLatestHealthCheckForm, listHealthCheckForms } from '../api/healthApi';
import { extractApiErrorMessage } from '../api/client';
import type { HealthCheckForm } from '../types/care';

export function useHealthCheckData(elderId?: number, agreementId?: number) {
  const [loading, setLoading] = useState(false);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [latestForm, setLatestForm] = useState<HealthCheckForm | null>(null);
  const [historyList, setHistoryList] = useState<HealthCheckForm[]>([]);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [historyError, setHistoryError] = useState<string | null>(null);
  const [historyDetailOpen, setHistoryDetailOpen] = useState(false);
  const [historyDetailLoading, setHistoryDetailLoading] = useState(false);
  const [historyDetailError, setHistoryDetailError] = useState<string | null>(null);
  const [historyDetail, setHistoryDetail] = useState<HealthCheckForm | null>(null);

  useEffect(() => {
    if (!elderId) {
      setLatestForm(null);
      setHistoryList([]);
      setErrorMessage('未获取到老人编号，无法查看健康体检结果');
      return;
    }

    let canceled = false;

    const loadLatest = async () => {
      setLoading(true);
      setErrorMessage(null);
      try {
        const detail = await getLatestHealthCheckForm(elderId, agreementId);
        if (!canceled) {
          setLatestForm(detail);
        }
      } catch (error) {
        if (!canceled) {
          setLatestForm(null);
          setErrorMessage(extractApiErrorMessage(error, '管理端暂未录入健康体检表'));
        }
      } finally {
        if (!canceled) {
          setLoading(false);
        }
      }
    };

    void loadLatest();

    return () => {
      canceled = true;
    };
  }, [agreementId, elderId]);

  useEffect(() => {
    if (!elderId) {
      setHistoryList([]);
      setHistoryError(null);
      return;
    }

    let canceled = false;

    const loadHistory = async () => {
      setHistoryLoading(true);
      setHistoryError(null);
      try {
        const forms = await listHealthCheckForms(elderId, agreementId);
        if (!canceled) {
          setHistoryList(forms);
        }
      } catch (error) {
        if (!canceled) {
          setHistoryList([]);
          setHistoryError(extractApiErrorMessage(error, '加载历史体检表失败'));
        }
      } finally {
        if (!canceled) {
          setHistoryLoading(false);
        }
      }
    };

    void loadHistory();

    return () => {
      canceled = true;
    };
  }, [agreementId, elderId]);

  const handleOpenHistoryDetail = async (record: HealthCheckForm) => {
    if (!record.formId) {
      setHistoryDetailLoading(false);
      setHistoryDetail(null);
      setHistoryDetailError('该历史记录缺少表单编号，无法查看详情');
      setHistoryDetailOpen(true);
      return;
    }

    setHistoryDetailOpen(true);
    setHistoryDetailLoading(true);
    setHistoryDetailError(null);
    setHistoryDetail(null);

    try {
      const detail = await getHealthCheckForm(record.formId);
      setHistoryDetail(detail);
    } catch (error) {
      setHistoryDetailError(extractApiErrorMessage(error, '加载体检表详情失败'));
    } finally {
      setHistoryDetailLoading(false);
    }
  };

  return {
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
  };
}
