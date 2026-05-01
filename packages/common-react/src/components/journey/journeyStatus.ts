import type { TagProps } from 'antd';

export interface JourneyStatusMeta {
  color: TagProps['color'];
  label: string;
  hint: string;
}

const DEFAULT_STATUS_META: JourneyStatusMeta = {
  color: 'default',
  label: '未知状态',
  hint: '当前结果状态未被识别，请联系管理员排查后端状态映射。',
};

const STATUS_META: Record<string, JourneyStatusMeta> = {
  PENDING_ASSESSMENT: {
    color: 'gold',
    label: '待需求评估',
    hint: '申请与体检已提交，等待管理端完成需求评估。',
  },
  PENDING_HEALTH_ASSESSMENT: {
    color: 'geekblue',
    label: '待健康评估',
    hint: '需求评估已通过，等待管理端完成签约前健康评估。',
  },
  IN_SERVICE: {
    color: 'green',
    label: '服务中',
    hint: '服务旅程已建立，护理服务正在执行。',
  },
  RENEW_RECOMMENDED: {
    color: 'cyan',
    label: '建议续约',
    hint: '本次评价结果良好，建议继续续约服务。',
  },
  RENEW: {
    color: 'cyan',
    label: '建议续约',
    hint: '评价结论为续约，建议进入续约流程。',
  },
  IMPROVEMENT_REQUIRED: {
    color: 'orange',
    label: '需改进',
    hint: '需要对服务质量进行改进后继续跟进。',
  },
  IMPROVE: {
    color: 'orange',
    label: '需改进',
    hint: '评价结论为改进，建议制定改进计划。',
  },
  TERMINATED: {
    color: 'red',
    label: '已终止',
    hint: '当前服务旅程已终止，请评估后续安置方案。',
  },
  TERMINATE: {
    color: 'red',
    label: '已终止',
    hint: '评价结论为终止，服务将不再继续。',
  },
};

export function getJourneyStatusMeta(status?: string): JourneyStatusMeta {
  if (!status) {
    return DEFAULT_STATUS_META;
  }

  return STATUS_META[status] ?? DEFAULT_STATUS_META;
}
