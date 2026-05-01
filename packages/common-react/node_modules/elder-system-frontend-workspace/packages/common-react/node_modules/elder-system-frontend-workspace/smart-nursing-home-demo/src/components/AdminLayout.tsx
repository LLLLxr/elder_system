import { Button, Layout, Menu, Typography } from 'antd';
import type { MenuProps } from 'antd';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { logout } from '../api/authApi';
import { ROUTE_PATHS } from '../constants/routes';

const { Header, Content } = Layout;

const menuItems: MenuProps['items'] = [
  { key: ROUTE_PATHS.ADMIN_DASHBOARD, label: '全局看板' },
  { key: ROUTE_PATHS.ADMIN_NEEDS_ASSESSMENT, label: '需求评估' },
  { key: ROUTE_PATHS.ADMIN_HEALTH_CHECK_FORMS, label: '体检表录入' },
  { key: ROUTE_PATHS.ADMIN_HEALTH_ASSESSMENT, label: '健康评估' },
  { key: ROUTE_PATHS.ADMIN_JOURNEY_TASKS, label: '任务看板' },
  { key: ROUTE_PATHS.ADMIN_USERS_ANALYTICS, label: '用户分析' },
  { key: ROUTE_PATHS.ADMIN_CARE, label: '护理分析' },
  { key: ROUTE_PATHS.ADMIN_OPS, label: '运营分析' },
  { key: ROUTE_PATHS.ADMIN_ALERTS, label: '告警中心' },
  { key: ROUTE_PATHS.ADMIN_USERS, label: '用户管理' },
  { key: ROUTE_PATHS.ADMIN_ROLES, label: '角色管理' },
  { key: ROUTE_PATHS.ADMIN_PERMISSIONS, label: '权限管理' },
];

export default function AdminLayout() {
  const navigate = useNavigate();
  const location = useLocation();

  const selectedMenuKey = menuItems?.some((item) => item?.key === location.pathname)
    ? location.pathname
    : ROUTE_PATHS.ADMIN_DASHBOARD;

  const handleLogout = async () => {
    await logout();
    navigate(ROUTE_PATHS.LOGIN, { replace: true });
  };

  return (
    <Layout style={{ minHeight: '100%' }}>
      <Header
        style={{
          background: '#fff',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          borderBottom: '1px solid #f0f0f0',
          padding: '0 16px',
          gap: 16,
        }}
      >
        <Typography.Text strong style={{ whiteSpace: 'nowrap' }}>
          智慧养老后台管理
        </Typography.Text>

        <Menu
          mode="horizontal"
          selectedKeys={[selectedMenuKey]}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
          style={{ flex: 1, minWidth: 0, borderBottom: 'none' }}
        />

        <Button onClick={handleLogout}>退出登录</Button>
      </Header>

      <Content style={{ padding: 24 }}>
        <Outlet />
      </Content>
    </Layout>
  );
}
