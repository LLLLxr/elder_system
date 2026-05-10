import type { ReactNode } from 'react';
import { Space, Typography } from 'antd';

type JourneyPageScaffoldProps = {
  title: ReactNode;
  description?: ReactNode;
  actions?: ReactNode;
  children: ReactNode;
};

export default function JourneyPageScaffold({ title, description, actions, children }: JourneyPageScaffoldProps) {
  return (
    <Space direction="vertical" size="large" className="journey-page-scaffold">
      <Space className="journey-page-header" align="start" wrap>
        <div className="journey-page-heading">
          <Typography.Title level={4} className="journey-page-title">
            {title}
          </Typography.Title>
          {description ? <Typography.Text type="secondary">{description}</Typography.Text> : null}
        </div>
        {actions ? <div className="journey-page-actions">{actions}</div> : null}
      </Space>
      {children}
    </Space>
  );
}
