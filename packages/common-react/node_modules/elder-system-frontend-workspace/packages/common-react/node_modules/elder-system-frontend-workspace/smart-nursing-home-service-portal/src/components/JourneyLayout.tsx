import { Button, Layout, Menu, Typography } from 'antd';
import type { MenuProps } from 'antd';
import { useMemo } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { logout } from '../api/authApi';
import { ROUTE_PATHS } from '../constants/routes';
import { canAccessAssessmentReview } from '../stores/userStore';

const { Header, Content } = Layout;

export default function JourneyLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const canReviewAssessment = canAccessAssessmentReview();

  const menuItems = useMemo<MenuProps['items']>(
    () => [
      { key: ROUTE_PATHS.JOURNEY_OVERVIEW, label: '服务总览' },
      { key: ROUTE_PATHS.JOURNEY_START, label: '申请受理登记' },
      { key: ROUTE_PATHS.JOURNEY_TASKS, label: '我的任务' },
      ...(canReviewAssessment ? [{ key: ROUTE_PATHS.JOURNEY_REVIEW, label: '评价收尾' }] : []),
      { key: ROUTE_PATHS.JOURNEY_RESULT, label: '旅程结果' },
      { key: ROUTE_PATHS.HEALTH_CHECK, label: '健康体检结果' },
    ],
    [canReviewAssessment],
  );

  const selectedMenuKey = menuItems?.some((item) => item?.key === location.pathname)
    ? location.pathname
    : ROUTE_PATHS.JOURNEY_OVERVIEW;

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
          智慧养老服务流程端
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
