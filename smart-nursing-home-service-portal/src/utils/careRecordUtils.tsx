import type { NurseCareRecord } from '../types/care';

export function getRecordValue(record: NurseCareRecord | null, key: string) {
  const value = record?.recordFormData?.[key];
  return typeof value === 'string' && value.trim() ? value : '-';
}

function getRecordStatus(record: NurseCareRecord, key: string) {
  const value = record.recordFormData?.[key];
  return typeof value === 'string' ? value : undefined;
}

export function summarizeNurseRecord(record: NurseCareRecord) {
  const fieldPairs = [
    ['dietStatus', '饮食'],
    ['sleepStatus', '睡眠'],
    ['vitalsStatus', '生命体征'],
    ['skinStatus', '皮肤'],
    ['emotionStatus', '情绪'],
    ['medicationStatus', '用药'],
    ['rehabilitationStatus', '康复'],
  ] as const;

  const abnormalFields = fieldPairs
    .filter(([key]) => {
      const value = getRecordStatus(record, key);
      return value === '异常' || value === '需观察';
    })
    .map(([, label]) => label);

  if (abnormalFields.length > 0) {
    return `${abnormalFields.join('、')}需重点关注`;
  }

  const nonNormalFields = fieldPairs
    .filter(([key]) => getRecordStatus(record, key) === '一般')
    .map(([, label]) => label);
  if (nonNormalFields.length > 0) {
    return `${nonNormalFields.join('、')}情况一般`;
  }

  const remark = getRecordStatus(record, 'remark');
  if (remark && remark.trim()) {
    return remark.length > 18 ? `${remark.slice(0, 18)}...` : remark;
  }

  const hasStructuredData = Object.keys(record.recordFormData ?? {}).length > 0;
  return hasStructuredData ? '已记录护理情况' : '情况平稳';
}

export function summarizeDoctorContent(content?: string) {
  if (!content || !content.trim()) {
    return '-';
  }
  const normalized = content.replace(/\s+/g, ' ').trim();
  return normalized.length > 24 ? `${normalized.slice(0, 24)}...` : normalized;
}

export function renderRiskFlag(value?: boolean) {
  return value ? '有风险' : '无风险';
}
