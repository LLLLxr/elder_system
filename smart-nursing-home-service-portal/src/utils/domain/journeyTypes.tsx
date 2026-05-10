import type { IntakeRecord, JourneyTaskItem } from '../../types/care';
import { Tag } from 'antd';

export type ApplicationStepKey =
  | 'APPLICATION_SUBMITTED'
  | 'ADMISSION_ASSESSMENT'
  | 'HEALTH_ASSESSMENT'
  | 'AGREEMENT'
  | 'IN_SERVICE';

export type ApplicationStepStatus = 'wait' | 'process' | 'finish' | 'error';

export type AlertType = 'success' | 'info' | 'warning' | 'error';

export interface ApplicationStepItem {
  key: ApplicationStepKey;
  title: string;
  status: ApplicationStepStatus;
  summary: string;
  timeText: string;
  hint: string;
}

export interface JourneyAlert {
  type: AlertType;
  message: string;
}

export interface JourneyProgressData {
  steps: ApplicationStepItem[];
  currentStepKey: ApplicationStepKey;
  overallAlert: JourneyAlert;
  currentSummary: string;
  currentHint: string;
}

export const STEP_ORDER: ApplicationStepKey[] = [
  'APPLICATION_SUBMITTED',
  'ADMISSION_ASSESSMENT',
  'HEALTH_ASSESSMENT',
  'AGREEMENT',
  'IN_SERVICE',
];

export const STEP_TITLES: Record<ApplicationStepKey, string> = {
  APPLICATION_SUBMITTED: '提交申请',
  ADMISSION_ASSESSMENT: '需求评估',
  HEALTH_ASSESSMENT: '健康评估',
  AGREEMENT: '签约',
  IN_SERVICE: '服务中',
};

export const STEP_STATUS_LABELS: Record<ApplicationStepStatus, string> = {
  wait: '未开始',
  process: '进行中',
  finish: '已完成',
  error: '已结束',
};

export const STEP_STATUS_COLORS: Record<ApplicationStepStatus, string> = {
  wait: 'default',
  process: 'processing',
  finish: 'success',
  error: 'error',
};

const STATE_LABELS: Record<string, string> = {
  PENDING_ASSESSMENT: '等待需求评估',
  PENDING_HEALTH_ASSESSMENT: '等待健康评估',
  PENDING_AGREEMENT: '等待签约',
  IN_SERVICE: '服务中',
  IMPROVEMENT_REQUIRED: '待改进',
  RENEWED: '已续约',
  TERMINATED: '已结束',
};

export const OPEN_TASK_STATUSES = new Set(['PENDING', 'OVERDUE']);

export const STEP_INDEX = STEP_ORDER.reduce<Record<ApplicationStepKey, number>>((acc, key, idx) => {
  acc[key] = idx;
  return acc;
}, {} as Record<ApplicationStepKey, number>);

export function getStateLabel(state?: string): string {
  if (!state) return '-';
  return STATE_LABELS[state] ?? state;
}

export function renderStepStatusTag(status: ApplicationStepStatus) {
  return <Tag color={STEP_STATUS_COLORS[status]}>{STEP_STATUS_LABELS[status]}</Tag>;
}

export function getLatestTaskOfType(timeline: JourneyTaskItem[], taskType: string): JourneyTaskItem | undefined {
  for (let i = timeline.length - 1; i >= 0; i--) {
    if (timeline[i].taskType === taskType) return timeline[i];
  }
  return undefined;
}

export function includesKeyword(text: string | undefined, keyword: string): boolean {
  return Boolean(text && text.includes(keyword));
}

export function isStepBefore(left: ApplicationStepKey, right: ApplicationStepKey): boolean {
  return STEP_INDEX[left] < STEP_INDEX[right];
}
