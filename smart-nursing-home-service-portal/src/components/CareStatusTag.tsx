import { Tag } from 'antd';

type StatusConfig = {
  label: string;
  color?: string;
};

const caregiverCheckInStatusMap: Record<string, StatusConfig> = {
  COMPLETED: { label: '已完成', color: 'success' },
  PARTIAL: { label: '部分完成', color: 'warning' },
  PENDING: { label: '待完成' },
};

const familyServicePlanStatusMap: Record<string, StatusConfig> = {
  ACTIVE: { label: '执行中', color: 'success' },
  INACTIVE: { label: '已结束' },
};

const familyVisitReservationStatusMap: Record<string, StatusConfig> = {
  PENDING: { label: '待审核', color: 'processing' },
  APPROVED: { label: '已通过', color: 'success' },
  REJECTED: { label: '已驳回', color: 'error' },
};

const caregiverQualificationStatusMap: Record<string, StatusConfig> = {
  PENDING: { label: '待审核', color: 'processing' },
  APPROVED: { label: '已通过', color: 'success' },
  REJECTED: { label: '已驳回', color: 'error' },
};

const agreementJourneyStatusMap: Record<string, StatusConfig> = {
  IN_SERVICE: { label: '已签约并进入服务', color: 'success' },
  PENDING_AGREEMENT: { label: '待签约', color: 'processing' },
  TERMINATED: { label: '已结束', color: 'error' },
};

function renderMappedStatus(status: string | undefined, statusMap: Record<string, StatusConfig>) {
  const config = status ? statusMap[status] : undefined;
  if (!config) {
    return <Tag>{status ?? '-'}</Tag>;
  }
  return <Tag color={config.color}>{config.label}</Tag>;
}

export function CaregiverCheckInStatusTag({ status }: { status?: string }) {
  return renderMappedStatus(status, caregiverCheckInStatusMap);
}

export function FamilyServicePlanStatusTag({ status }: { status?: string }) {
  return renderMappedStatus(status, familyServicePlanStatusMap);
}

export function FamilyVisitReservationStatusTag({ status }: { status?: string }) {
  return renderMappedStatus(status, familyVisitReservationStatusMap);
}

export function CaregiverQualificationStatusTag({ status }: { status?: string }) {
  return renderMappedStatus(status, caregiverQualificationStatusMap);
}

export function AgreementJourneyStatusTag({ status }: { status?: string }) {
  return renderMappedStatus(status, agreementJourneyStatusMap);
}
