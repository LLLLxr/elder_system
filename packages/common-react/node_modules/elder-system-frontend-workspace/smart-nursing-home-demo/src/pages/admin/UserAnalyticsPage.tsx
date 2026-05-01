import { useEffect, useState } from 'react';
import { Alert, Card, Col, Row, Space, Typography } from 'antd';
import MetricCards from '../../components/MetricCards';
import SimpleBarChart from '../../components/SimpleBarChart';
import SimplePieList from '../../components/SimplePieList';
import { getUserAnalyticsData } from '../../api/analyticsApi';
import { extractApiErrorMessage } from '../../api/client';
import type { UserAnalyticsData } from '../../types/analytics';

export default function UserAnalyticsPage() {
  const [data, setData] = useState<UserAnalyticsData | null>(null);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setErrorMessage(null);
      try {
        const result = await getUserAnalyticsData();
        setData(result);
      } catch (error) {
        setErrorMessage(extractApiErrorMessage(error, '加载用户分析失败'));
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, []);

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Typography.Title level={4} style={{ margin: 0 }}>
        用户域可视化
      </Typography.Title>

      {errorMessage ? <Alert type="error" showIcon message={errorMessage} /> : null}

      <Card loading={loading}>
        <MetricCards data={data?.cards ?? []} />
      </Card>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card loading={loading}>
            <SimpleBarChart title="近6月用户增长" data={data?.growth ?? []} color="#722ed1" />
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card loading={loading}>
            <SimplePieList title="角色分布" data={data?.roleDistribution ?? []} />
          </Card>
        </Col>
      </Row>
    </Space>
  );
}
