import { Space, Typography } from 'antd';
import { Column } from '@ant-design/plots';
import type { TrendPoint } from '../types/analytics';

interface SimpleBarChartProps {
  title: string;
  data: TrendPoint[];
  color?: string;
}

export default function SimpleBarChart({ title, data, color = '#1677ff' }: SimpleBarChartProps) {
  return (
    <Space direction="vertical" size="small" style={{ width: '100%' }}>
      <Typography.Text strong>{title}</Typography.Text>
      <Column
        data={data}
        xField="label"
        yField="value"
        color={color}
        height={260}
        axis={{
          x: { title: false },
          y: { title: false },
        }}
        tooltip={{
          items: [{ channel: 'y', name: '数量' }],
        }}
        label={{ text: 'value', position: 'top' }}
      />
    </Space>
  );
}
