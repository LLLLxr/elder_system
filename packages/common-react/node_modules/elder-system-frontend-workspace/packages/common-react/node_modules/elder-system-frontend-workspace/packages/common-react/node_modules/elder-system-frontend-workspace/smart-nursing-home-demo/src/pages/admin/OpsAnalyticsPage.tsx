import { useEffect, useState } from 'react';
import { Alert, Card, List, Space, Typography } from 'antd';
import MetricCards from '../../components/MetricCards';
import SimplePieList from '../../components/SimplePieList';
import { getOpsAnalyticsData } from '../../api/analyticsApi';
import { extractApiErrorMessage } from '../../api/client';
import type { OpsAnalyticsData } from '../../types/analytics';

export default function OpsAnalyticsPage() {
  const [data, setData] = useState<OpsAnalyticsData | null>(null);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setErrorMessage(null);
      try {
        const result = await getOpsAnalyticsData();
        setData(result);
      } catch (error) {
        setErrorMessage(extractApiErrorMessage(error, '加载运营分析失败'));
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, []);

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Typography.Title level={4} style={{ margin: 0 }}>
        运营域可视化
      </Typography.Title>

      {errorMessage ? <Alert type="error" showIcon message={errorMessage} /> : null}

      <Card loading={loading}>
        <MetricCards data={data?.cards ?? []} />
      </Card>

      <Card loading={loading}>
        <SimplePieList title="运营域任务分布" data={data?.domainDistribution ?? []} />
      </Card>

      <Card title="运营模块状态摘要" loading={loading}>
        <List
          bordered
          dataSource={data?.scopeSnapshots ?? []}
          renderItem={(item) => (
            <List.Item>
              <Typography.Text strong>{item.module}：</Typography.Text>
              <Typography.Text>{item.content}</Typography.Text>
            </List.Item>
          )}
        />
      </Card>
    </Space>
  );
}
