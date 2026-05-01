import { Card, Descriptions, Typography } from 'antd';
import StatusTag from './StatusTag';

export interface JourneyResultLike {
  applicationId?: number;
  agreementId?: number;
  carePlanId?: number;
  healthProfileId?: number;
  finalStatus?: string;
  message?: string;
}

export interface ResultCardProps {
  title?: string;
  result: JourneyResultLike;
}

export default function ResultCard({ title = '服务旅程结果', result }: ResultCardProps) {
  return (
    <Card title={title}>
      <Descriptions column={1} size="small" bordered>
        <Descriptions.Item label="申请ID">{result.applicationId ?? '-'}</Descriptions.Item>
        <Descriptions.Item label="协议ID">{result.agreementId ?? '-'}</Descriptions.Item>
        <Descriptions.Item label="护理计划ID">{result.carePlanId ?? '-'}</Descriptions.Item>
        <Descriptions.Item label="健康档案ID">{result.healthProfileId ?? '-'}</Descriptions.Item>
        <Descriptions.Item label="最终状态">
          <StatusTag status={result.finalStatus} />
        </Descriptions.Item>
        <Descriptions.Item label="消息">
          <Typography.Text>{result.message || '-'}</Typography.Text>
        </Descriptions.Item>
      </Descriptions>
    </Card>
  );
}
