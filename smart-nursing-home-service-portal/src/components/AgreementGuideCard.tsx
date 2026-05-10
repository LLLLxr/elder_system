import { Button, Card, List, Space, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';
import { ROUTE_PATHS } from '../constants/routes';
import type { ServiceAgreement } from '../types/care';

interface AgreementGuideCardProps {
  waitingAgreement: boolean;
  agreementCompleted: boolean;
  agreement: ServiceAgreement | null;
  signing: boolean;
  onCloudSign: () => void;
}

export default function AgreementGuideCard({
  waitingAgreement,
  agreementCompleted,
  agreement,
  signing,
  onCloudSign,
}: AgreementGuideCardProps) {
  const navigate = useNavigate();

  const agreementGuidance = agreementCompleted
    ? '服务协议已完成签署并进入生效阶段，请留意后续护理服务安排。'
    : waitingAgreement
      ? '当前处于待签约阶段，请按平台通知准备身份信息、服务场景确认和签约时间安排。'
      : '当前尚未进入正式签约阶段，请先完成前置评估与健康检查。';

  const agreementSteps = agreementCompleted
    ? ['平台生成服务协议', '双方确认协议内容', '协议完成签署并生效']
    : ['平台生成服务协议草稿', '家属确认服务内容与期限', '按通知完成线下或线上签署'];

  return (
    <Card title="签约办理指引">
      <Space direction="vertical" size="middle" style={{ width: '100%' }}>
        <Typography.Text>{agreementGuidance}</Typography.Text>
        <List
          size="small"
          bordered
          dataSource={agreementSteps}
          renderItem={(item, index) => <List.Item>{`${index + 1}. ${item}`}</List.Item>}
        />
        <Space wrap>
          <Button type="primary" disabled={!waitingAgreement || !agreement?.agreementId} loading={signing} onClick={onCloudSign}>
            进入云签字
          </Button>
          <Button onClick={() => navigate(ROUTE_PATHS.JOURNEY_RESULT)}>返回申请结果</Button>
        </Space>
        <Typography.Text type="secondary">
          当前版本先提供签约阶段查看与指引，正式电子签署入口后续接入。
        </Typography.Text>
      </Space>
    </Card>
  );
}
