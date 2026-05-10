import { useState, useCallback, useEffect } from 'react';
import type { IntakeRecord, JourneyTaskItem, JourneyTaskOverview } from '../types/care';
import { getJourneyTaskOverview, listIntakeRecordsByApplicant, listJourneyTaskTimeline } from '../api/careOrchestrationApi';
import { extractApiErrorMessage } from '../api/client';
import { formatDateTime } from '../utils/dateFormat';

interface MineOption {
  label: string;
  value: number;
}

interface UseJourneyDataResult {
  mineOptions: MineOption[];
  mineRecords: IntakeRecord[];
  overview: JourneyTaskOverview | null;
  timeline: JourneyTaskItem[];
  loading: boolean;
  error: string | null;
  refresh: () => void;
}

const ALL_TASK_STATUSES = ['PENDING', 'OVERDUE', 'COMPLETED', 'CANCELLED'];

function getSortTimestamp(value?: string) {
  if (!value) return Number.MAX_SAFE_INTEGER;
  const time = new Date(value).getTime();
  return Number.isNaN(time) ? Number.MAX_SAFE_INTEGER : time;
}

export function useJourneyData(loginUsername?: string, applicationId?: number): UseJourneyDataResult {
  const [mineOptions, setMineOptions] = useState<MineOption[]>([]);
  const [mineRecords, setMineRecords] = useState<IntakeRecord[]>([]);
  const [overview, setOverview] = useState<JourneyTaskOverview | null>(null);
  const [timeline, setTimeline] = useState<JourneyTaskItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadData = useCallback(async () => {
    if (!loginUsername) {
      setMineOptions([]);
      setMineRecords([]);
      setOverview(null);
      setTimeline([]);
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const records = await listIntakeRecordsByApplicant(loginUsername);
      const filteredRecords = records.filter(
        (record): record is IntakeRecord & { applicationId: number } => typeof record.applicationId === 'number',
      );
      const orderedRecords = [...filteredRecords].sort(
        (left, right) => getSortTimestamp(left.submittedAt) - getSortTimestamp(right.submittedAt),
      );
      const options = orderedRecords.map((record, index) => ({
        value: record.applicationId,
        label: `我的第 ${index + 1} 次申请（提交时间：${formatDateTime(record.submittedAt)}）`,
      }));
      setMineRecords(orderedRecords);
      setMineOptions(options);

      if (applicationId) {
        const [overviewData, timelineData] = await Promise.all([
          getJourneyTaskOverview({ applicationId, statuses: ALL_TASK_STATUSES }),
          listJourneyTaskTimeline(applicationId),
        ]);
        setOverview(overviewData);
        setTimeline(timelineData);
      } else {
        setOverview(null);
        setTimeline([]);
      }
    } catch (err) {
      setError(extractApiErrorMessage(err, '加载数据失败'));
      setOverview(null);
      setTimeline([]);
    } finally {
      setLoading(false);
    }
  }, [loginUsername, applicationId]);

  useEffect(() => {
    void loadData();
  }, [loadData]);

  return {
    mineOptions,
    mineRecords,
    overview,
    timeline,
    loading,
    error,
    refresh: loadData,
  };
}
