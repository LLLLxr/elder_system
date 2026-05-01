import { Alert } from 'antd';
import { getJourneyStatusMeta } from './journeyStatus';

export interface JourneyStatusAlertProps {
  status?: string;
}

export default function JourneyStatusAlert({ status }: JourneyStatusAlertProps) {
  const meta = getJourneyStatusMeta(status);

  return <Alert type="info" showIcon message={meta.label} description={meta.hint} />;
}
