import type { ReactNode } from 'react';
import { Space, Typography } from 'antd';

interface AdminPageScaffoldProps {
  title: string;
  description?: ReactNode;
  extra?: ReactNode;
  children: ReactNode;
}

export default function AdminPageScaffold({ title, description, extra, children }: AdminPageScaffoldProps) {
  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Space style={{ width: '100%', justifyContent: 'space-between' }} align="start" wrap>
        <div>
          <Typography.Title level={4} style={{ margin: 0 }}>
            {title}
          </Typography.Title>
          {description ? <Typography.Text type="secondary">{description}</Typography.Text> : null}
        </div>
        {extra}
      </Space>
      {children}
    </Space>
  );
}
