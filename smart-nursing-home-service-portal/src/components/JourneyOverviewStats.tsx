import { Card, Col, Row, Statistic } from 'antd';
import type { JourneyTaskOverview } from '../types/care';

interface JourneyOverviewStatsProps {
  overview: JourneyTaskOverview | null;
  loading: boolean;
}

export default function JourneyOverviewStats({ overview, loading }: JourneyOverviewStatsProps) {
  const stats = [
    { key: 'pending', title: '待处理', value: overview?.pendingCount ?? 0 },
    { key: 'overdue', title: '需关注', value: overview?.overdueCount ?? 0 },
    { key: 'completed', title: '已完成', value: overview?.completedCount ?? 0 },
    { key: 'cancelled', title: '已取消', value: overview?.cancelledCount ?? 0 },
  ];

  return (
    <Row gutter={[16, 16]}>
      {stats.map((item) => (
        <Col key={item.key} xs={24} sm={12} lg={6}>
          <Card loading={loading}>
            <Statistic title={item.title} value={item.value} />
          </Card>
        </Col>
      ))}
    </Row>
  );
}
