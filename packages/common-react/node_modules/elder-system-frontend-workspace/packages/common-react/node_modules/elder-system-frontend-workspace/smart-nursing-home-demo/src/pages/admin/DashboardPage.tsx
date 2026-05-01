import { useEffect, useState } from 'react';
import { Alert, Card, Col, List, Row, Space, Typography } from 'antd';
import MetricCards from '../../components/MetricCards';
import SimpleBarChart from '../../components/SimpleBarChart';
import SimplePieList from '../../components/SimplePieList';
import { getDashboardData } from '../../api/analyticsApi';
import { extractApiErrorMessage } from '../../api/client';
import type { DashboardData } from '../../types/analytics';

export default function DashboardPage() {
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setErrorMessage(null);
      try {
        const result = await getDashboardData();
        setData(result);
      } catch (error) {
        setErrorMessage(extractApiErrorMessage(error, '加载总览数据失败'));
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, []);

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Typography.Title level={4} style={{ margin: 0 }}>
        全局总览看板
      </Typography.Title>

      {errorMessage ? <Alert type="error" showIcon message={errorMessage} /> : null}

      <Card loading={loading}>
        <MetricCards data={data?.cards ?? []} />
      </Card>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card loading={loading}>
            <SimpleBarChart title="用户增长趋势" data={data?.userTrend ?? []} />
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card loading={loading}>
            <SimplePieList title="护理旅程漏斗" data={data?.careFunnel ?? []} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card loading={loading}>
            <SimplePieList title="运营域分布" data={data?.opsDistribution ?? []} />
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="待处理告警" loading={loading}>
            <List
              bordered
              dataSource={data?.pendingAlerts ?? []}
              renderItem={(item) => <List.Item>{item}</List.Item>}
              locale={{ emptyText: '暂无告警' }}
            />
          </Card>
        </Col>
      </Row>
    </Space>
  );
}
