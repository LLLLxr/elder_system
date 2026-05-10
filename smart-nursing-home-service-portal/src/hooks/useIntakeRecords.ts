import { useState, useEffect } from 'react';
import type { IntakeRecord } from '../types/care';
import { listIntakeRecords, listIntakeRecordsByApplicant } from '../api/careOrchestrationApi';
import { extractApiErrorMessage } from '../api/client';

interface UseIntakeRecordsResult {
  intakeRecords: IntakeRecord[];
  intakeLoading: boolean;
  errorMessage: string | null;
  elderIdHint: string | null;
}

export function useIntakeRecords(
  loginUsername: string | undefined,
  queriedElderId: number | null
): UseIntakeRecordsResult {
  const [intakeRecords, setIntakeRecords] = useState<IntakeRecord[]>([]);
  const [intakeLoading, setIntakeLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [elderIdHint, setElderIdHint] = useState<string | null>(null);

  useEffect(() => {
    if (!loginUsername) {
      setIntakeRecords([]);
      setErrorMessage('未获取到当前登录用户，请重新登录');
      return;
    }

    let canceled = false;

    const loadRecords = async () => {
      setIntakeLoading(true);
      try {
        const records = queriedElderId
          ? await listIntakeRecords(queriedElderId)
          : await listIntakeRecordsByApplicant(loginUsername);
        if (!canceled) {
          setIntakeRecords(records);
          setErrorMessage(null);
          if (queriedElderId) {
            if (records.length > 0) {
              setElderIdHint(`已找到老人编号=${queriedElderId} 的受理记录（${records.length}条）`);
            } else {
              setElderIdHint(null);
            }
          }
        }
      } catch (error) {
        if (!canceled) {
          setIntakeRecords([]);
          if (queriedElderId) {
            setElderIdHint(null);
          }
          const message = extractApiErrorMessage(error, '查询受理记录失败');
          setErrorMessage(message);
        }
      } finally {
        if (!canceled) {
          setIntakeLoading(false);
        }
      }
    };

    void loadRecords();

    return () => {
      canceled = true;
    };
  }, [loginUsername, queriedElderId]);

  return {
    intakeRecords,
    intakeLoading,
    errorMessage,
    elderIdHint,
  };
}
