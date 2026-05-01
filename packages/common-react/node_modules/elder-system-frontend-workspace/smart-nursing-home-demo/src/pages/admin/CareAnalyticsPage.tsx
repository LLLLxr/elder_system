import { useEffect, useState } from 'react';
import { Alert, Card, Col, List, Row, Space, Typography } from 'antd';
import MetricCards from '../../components/MetricCards';
import SimpleBarChart from '../../components/SimpleBarChart';
import SimplePieList from '../../components/SimplePieList';
import { getCareAnalyticsData } from '../../api/analyticsApi';
import { extractApiErrorMessage } from '../../api/client';
import type { CareAnalyticsData } from '../../types/analytics';

export default function CareAnalyticsPage() {
  const [data, setData] = useState<CareAnalyticsData | null>(null);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setErrorMessage(null);
      try {
        const result = await getCareAnalyticsData();
        setData(result);
      } catch (error) {
        setErrorMessage(extractApiErrorMessage(error, '加载护理分析失败'));
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, []);

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Typography.Title level={4} style={{ margin: 0 }}>
        护理域可视化
      </Typography.Title>

      {errorMessage ? <Alert type="error" showIcon message={errorMessage} /> : null}

      <Card loading={loading}>
        <MetricCards data={data?.cards ?? []} />
      </Card>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card loading={loading}>
            <SimplePieList title="护理阶段分布" data={data?.stageDistribution ?? []} />
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card loading={loading}>
            <SimpleBarChart title="质量趋势" data={data?.qualityTrend ?? []} color="#13c2c2" />
          </Card>
        </Col>
      </Row>

      <Card title="模块状态摘要" loading={loading}>
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
