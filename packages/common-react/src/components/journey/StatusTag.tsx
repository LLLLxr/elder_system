import { Tag, Tooltip } from 'antd';
import { getJourneyStatusMeta } from './journeyStatus';

export interface StatusTagProps {
  status?: string;
}

export default function StatusTag({ status }: StatusTagProps) {
  const meta = getJourneyStatusMeta(status);

  return (
    <Tooltip title={status || 'UNKNOWN'}>
      <Tag color={meta.color}>{meta.label}</Tag>
    </Tooltip>
  );
}
