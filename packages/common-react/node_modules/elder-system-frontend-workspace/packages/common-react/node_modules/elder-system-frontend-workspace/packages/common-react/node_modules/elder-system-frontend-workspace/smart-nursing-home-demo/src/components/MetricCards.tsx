import { Card, Col, Row, Statistic, Typography } from 'antd';
import type { MetricCard } from '../types/analytics';

interface MetricCardsProps {
  data: MetricCard[];
}

function trendColor(trend: number): string {
  if (trend > 0) return '#3f8600';
  if (trend < 0) return '#cf1322';
  return '#8c8c8c';
}

export default function MetricCards({ data }: MetricCardsProps) {
  return (
    <Row gutter={[16, 16]}>
      {data.map((item) => (
        <Col key={item.key} xs={24} sm={12} lg={6}>
          <Card>
            <Statistic title={item.title} value={item.value} suffix={item.suffix} />
            <Typography.Text style={{ color: trendColor(item.trend) }}>
              {item.trend >= 0 ? `+${item.trend}` : item.trend}%
            </Typography.Text>
          </Card>
        </Col>
      ))}
    </Row>
  );
}
