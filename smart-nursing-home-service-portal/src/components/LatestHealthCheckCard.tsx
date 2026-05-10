import { Alert, Button, Card, Space, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';
import { ROUTE_PATHS } from '../constants/routes';
import type { HealthCheckForm } from '../types/care';

interface LatestHealthCheckCardProps {
  loading: boolean;
  latestForm: HealthCheckForm | null;
  errorMessage: string | null;
  fromJourney: boolean;
  elderId?: number;
  agreementId?: number;
  elderName?: string;
  onViewDetail: (form: HealthCheckForm) => void;
}

export default function LatestHealthCheckCard({
  loading,
  latestForm,
  errorMessage,
  fromJourney,
  elderId,
  agreementId,
  elderName,
  onViewDetail,
}: LatestHealthCheckCardProps) {
  const navigate = useNavigate();

  return (
    <>
      {fromJourney ? (
        <Alert
          type="warning"
          showIcon
          message="当前服务申请需要等待管理端录入健康体检表后，才能继续后续评估流程。"
        />
      ) : null}

      {errorMessage ? <Alert type="warning" message={errorMessage} showIcon /> : null}

      {loading ? <Alert type="info" message="正在加载最新健康体检结果..." showIcon /> : null}

      {!loading && latestForm ? (
        <Card size="small" title="最新健康体检表">
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            <Typography.Text>表单编号：{latestForm.formId ?? '-'}</Typography.Text>
            <Typography.Text>老人姓名：{latestForm.elderName ?? elderName ?? '-'}</Typography.Text>
            <Typography.Text>老人编号：{latestForm.elderId ?? elderId ?? '-'}</Typography.Text>
            <Typography.Text>协议编号：{latestForm.agreementId ?? agreementId ?? '-'}</Typography.Text>
            <Typography.Text>体检日期：{latestForm.checkDate ?? '-'}</Typography.Text>
            <Typography.Text>责任医生：{latestForm.responsibleDoctor ?? '-'}</Typography.Text>
            <Space>
              <Button type="primary" onClick={() => onViewDetail(latestForm)}>
                查看详情
              </Button>
              {fromJourney ? (
                <Button onClick={() => navigate(ROUTE_PATHS.JOURNEY_RESULT, { replace: true })}>返回旅程结果</Button>
              ) : null}
            </Space>
          </Space>
        </Card>
      ) : null}
    </>
  );
}
