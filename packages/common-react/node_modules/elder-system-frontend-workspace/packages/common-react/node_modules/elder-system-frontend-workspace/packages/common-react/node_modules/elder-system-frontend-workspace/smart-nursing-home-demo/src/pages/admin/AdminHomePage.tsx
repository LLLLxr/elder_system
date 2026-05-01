import { Card, Space, Typography } from 'antd';

export default function AdminHomePage() {
  return (
    <Card>
      <Space direction="vertical" size="middle">
        <Typography.Title level={4} style={{ margin: 0 }}>
          后台数据管理
        </Typography.Title>
        <Typography.Text type="secondary">
          请从顶部菜单进入用户、角色、权限管理页面。
        </Typography.Text>
      </Space>
    </Card>
  );
}
