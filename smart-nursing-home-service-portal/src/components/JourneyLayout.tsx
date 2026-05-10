import { Alert, Button, Layout, Menu, Space, Tag, Typography } from 'antd';
import type { MenuProps } from 'antd';
import { useEffect, useMemo } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { logout } from '../api/authApi';
import { ROUTE_PATHS } from '../constants/routes';
import { useActiveElderGuard } from './useActiveElderGuard';
import {
  canAccessAssessmentReview,
  canAccessCaregiverDailyTasks,
  canAccessCaregiverQualification,
  canAccessElderBinding,
  canAccessFamilyCareDelivery,
  canAccessFamilyVisit,
  ensurePermissions,
  useUserStore,
} from '../stores/userStore';

const { Header, Content } = Layout;

type NavigationSection = {
  key: string;
  label: string;
  items: { key: string; label: string }[];
};

export default function JourneyLayout() {
  const { permissions } = useUserStore();
  const { activeElder, guardMessage } = useActiveElderGuard();
  const navigate = useNavigate();
  const location = useLocation();
  const canReviewAssessment = canAccessAssessmentReview();
  const canUseFamilyVisit = canAccessFamilyVisit();
  const canUseCaregiverQualification = canAccessCaregiverQualification();
  const canUseCaregiverDailyTasks = canAccessCaregiverDailyTasks();
  const canUseFamilyCareDelivery = canAccessFamilyCareDelivery();
  const canUseElderBinding = canAccessElderBinding();

  useEffect(() => {
    if (permissions.length === 0) {
      void ensurePermissions();
    }
  }, [permissions.length]);

  const navigationSections = useMemo<NavigationSection[]>(
    () => [
      {
        key: 'journey-services',
        label: '服务办理',
        items: [
          { key: ROUTE_PATHS.JOURNEY_OVERVIEW, label: '服务总览' },
          { key: ROUTE_PATHS.JOURNEY_START, label: '发起申请' },
          { key: ROUTE_PATHS.JOURNEY_TASKS, label: '申请进度' },
          { key: ROUTE_PATHS.JOURNEY_RESULT, label: '申请结果' },
          ...(canReviewAssessment ? [{ key: ROUTE_PATHS.JOURNEY_REVIEW, label: '评价收尾' }] : []),
        ],
      },
      {
        key: 'family-elder-visit',
        label: '服务对象与探访',
        items: [
          ...(canUseElderBinding ? [{ key: ROUTE_PATHS.MY_ELDERS, label: '绑定老人' }] : []),
          ...(canUseFamilyVisit
            ? [
                { key: ROUTE_PATHS.FAMILY_VISIT_CREATE, label: '家属预约' },
                { key: ROUTE_PATHS.FAMILY_VISIT_MY, label: '我的预约' },
              ]
            : []),
        ],
      },
      {
        key: 'family-care-delivery',
        label: '照护查看',
        items: [
          ...(canUseFamilyCareDelivery
            ? [
                { key: ROUTE_PATHS.FAMILY_SERVICE_PLANS, label: '服务清单' },
                { key: ROUTE_PATHS.FAMILY_CHECK_IN_RECORDS, label: '打卡记录' },
                { key: ROUTE_PATHS.FAMILY_CARE_RECORDS, label: '护理查房记录' },
              ]
            : []),
        ],
      },
      {
        key: 'caregiver-workspace',
        label: '护理员工作',
        items: [
          ...(canUseCaregiverDailyTasks
            ? [
                { key: ROUTE_PATHS.CAREGIVER_DAILY_TASKS, label: '我的护理任务' },
                { key: ROUTE_PATHS.CAREGIVER_CHECK_IN_HISTORY, label: '打卡历史' },
              ]
            : []),
          ...(canUseCaregiverQualification
            ? [
                { key: ROUTE_PATHS.CAREGIVER_QUALIFICATION_SUBMIT, label: '资质申请' },
                { key: ROUTE_PATHS.CAREGIVER_QUALIFICATION_STATUS, label: '资质状态' },
              ]
            : []),
        ],
      },
    ].filter((section) => section.items.length > 0),
    [
      canReviewAssessment,
      canUseCaregiverDailyTasks,
      canUseCaregiverQualification,
      canUseElderBinding,
      canUseFamilyCareDelivery,
      canUseFamilyVisit,
    ],
  );

  const menuItems = useMemo<MenuProps['items']>(
    () =>
      navigationSections.map((section) => ({
        key: section.key,
        label: section.label,
        children: section.items,
      })),
    [navigationSections],
  );

  const availableRouteKeys = useMemo(
    () => new Set(navigationSections.flatMap((section) => section.items.map((item) => item.key))),
    [navigationSections],
  );

  const selectedMenuKey = availableRouteKeys.has(location.pathname)
    ? location.pathname
    : ROUTE_PATHS.JOURNEY_OVERVIEW;

  const activeElderLabel = activeElder?.elderName?.trim() || (activeElder?.elderId ? `老人 ${activeElder.elderId}` : null);

  const handleLogout = async () => {
    await logout();
    navigate(ROUTE_PATHS.LOGIN, { replace: true });
  };

  return (
    <Layout style={{ minHeight: '100%' }}>
      <Header className="journey-app-header">
        <Space direction="vertical" size={0} className="journey-app-brand">
          <Typography.Text strong className="journey-app-title">
            智慧养老服务流程端
          </Typography.Text>
          <Typography.Text type="secondary" className="journey-app-subtitle">
            围绕老人、申请旅程与在院服务组织入口
          </Typography.Text>
        </Space>

        <Menu
          mode="horizontal"
          selectedKeys={[selectedMenuKey]}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
          className="journey-app-menu"
        />

        <Button className="journey-app-logout" onClick={handleLogout}>退出登录</Button>
      </Header>

      <Content className="journey-app-content">
        <Space direction="vertical" size="large" className="journey-app-content-inner">
          <Alert
            type={activeElder ? 'success' : 'warning'}
            showIcon
            message={
              activeElder
                ? (
                  <Space wrap size={[8, 8]}>
                    <Typography.Text strong>当前服务对象：{activeElderLabel}</Typography.Text>
                    <Tag color="blue">老人编号 {activeElder?.elderId ?? '-'}</Tag>
                    {activeElder?.bindingType ? (
                      <Tag>
                        {(() => {
                          if (activeElder.bindingType === 'SELF') return '本人';

                          const relationMap: Record<string, string> = {
                            'CHILD': '我的父母',
                            'SPOUSE': '我的配偶',
                            'SIBLING': '我的兄弟姐妹',
                            'PARENT': '我的子女',
                            'OTHER': '我的亲属',
                          };

                          return activeElder.relationToElder && relationMap[activeElder.relationToElder]
                            ? relationMap[activeElder.relationToElder]
                            : (activeElder.relationToElder || '家属');
                        })()}
                      </Tag>
                    ) : null}
                    <Button type="link" size="small" onClick={() => navigate(ROUTE_PATHS.MY_ELDERS)} style={{ paddingInline: 0 }}>
                      去切换老人
                    </Button>
                  </Space>
                )
                : (
                  <Space wrap size={[8, 8]}>
                    <Typography.Text strong>{guardMessage}</Typography.Text>
                    <Button type="link" size="small" onClick={() => navigate(ROUTE_PATHS.MY_ELDERS)} style={{ paddingInline: 0 }}>
                      去绑定老人
                    </Button>
                  </Space>
                )
            }
          />

          <Outlet />
        </Space>
      </Content>
    </Layout>
  );
}
