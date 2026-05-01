import { useMemo } from 'react';
import { Card, List, Space, Tag, Typography } from 'antd';

interface AlertItem {
  id: number;
  title: string;
  level: '高' | '中' | '低';
  owner: string;
  status: '待处理' | '处理中' | '已解决';
}

const items: AlertItem[] = [
  { id: 1, title: '权限变更待复核', level: '高', owner: '管理员A', status: '待处理' },
  { id: 2, title: '护理计划即将超时', level: '中', owner: '护理主管', status: '处理中' },
  { id: 3, title: '资源排班冲突', level: '中', owner: '运营值班', status: '待处理' },
  { id: 4, title: '安全应急回执未更新', level: '低', owner: '值班安防', status: '已解决' },
];

function colorByLevel(level: AlertItem['level']): string {
  if (level === '高') return 'red';
  if (level === '中') return 'orange';
  return 'blue';
}

function colorByStatus(status: AlertItem['status']): string {
  if (status === '已解决') return 'green';
  if (status === '处理中') return 'processing';
  return 'default';
}

export default function AlertsPage() {
  const pendingCount = useMemo(() => items.filter((item) => item.status !== '已解决').length, []);

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Typography.Title level={4} style={{ margin: 0 }}>
        告警与待办中心
      </Typography.Title>

      <Card>
        <Typography.Text>当前待处理告警：{pendingCount} 条</Typography.Text>
      </Card>

      <Card title="告警清单">
        <List
          bordered
          dataSource={items}
          renderItem={(item) => (
            <List.Item>
              <Space>
                <Typography.Text strong>{item.title}</Typography.Text>
                <Tag color={colorByLevel(item.level)}>{item.level}</Tag>
                <Tag color={colorByStatus(item.status)}>{item.status}</Tag>
                <Typography.Text type="secondary">负责人：{item.owner}</Typography.Text>
              </Space>
            </List.Item>
          )}
        />
      </Card>
    </Space>
  );
}
