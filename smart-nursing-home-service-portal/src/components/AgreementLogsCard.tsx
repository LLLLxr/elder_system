import { Card, List, Typography } from 'antd';
import { formatDateTime } from '../utils/dateFormat';
import type { JourneyTransitionLogItem } from '../types/care';

interface AgreementLogsCardProps {
  loading: boolean;
  logs: JourneyTransitionLogItem[];
}

export default function AgreementLogsCard({ loading, logs }: AgreementLogsCardProps) {
  return (
    <Card loading={loading} title="签约流转记录">
      {logs.length ? (
        <List
          dataSource={logs}
          renderItem={(item) => (
            <List.Item>
              <List.Item.Meta
                title={item.reason || item.journeyEvent || '签约流转'}
                description={`时间：${formatDateTime(item.transitionTime)}；状态：${item.fromState ?? '-'} → ${item.toState ?? '-'}`}
              />
            </List.Item>
          )}
        />
      ) : (
        <Typography.Text type="secondary">
          {loading ? '正在加载签约流转记录...' : '当前暂无签约流转记录。'}
        </Typography.Text>
      )}
    </Card>
  );
}
