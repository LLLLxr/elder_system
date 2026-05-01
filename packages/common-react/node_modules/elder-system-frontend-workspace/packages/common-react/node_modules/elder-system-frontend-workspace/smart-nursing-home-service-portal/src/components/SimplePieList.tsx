import { Space, Typography } from 'antd';
import { Pie } from '@ant-design/plots';
import type { PieDatum } from '../types/analytics';

interface SimplePieListProps {
  title: string;
  data: PieDatum[];
}

export default function SimplePieList({ title, data }: SimplePieListProps) {
  return (
    <Space direction="vertical" size="small" style={{ width: '100%' }}>
      <Typography.Text strong>{title}</Typography.Text>
      <Pie
        data={data}
        angleField="value"
        colorField="name"
        height={260}
        innerRadius={0.55}
        label={{ text: 'name', position: 'spider' }}
        legend={{ color: { position: 'bottom' } }}
        tooltip={{
          items: [{ channel: 'y', name: '数量' }],
        }}
      />
    </Space>
  );
}
