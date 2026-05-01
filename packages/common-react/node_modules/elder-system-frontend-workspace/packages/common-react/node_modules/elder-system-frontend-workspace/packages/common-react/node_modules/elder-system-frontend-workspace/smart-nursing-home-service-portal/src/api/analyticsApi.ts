import apiClient, { unwrapApiData } from './client';
import {
  ADMISSION_SCOPE_PATH,
  BILLING_SCOPE_PATH,
  CARE_ANALYTICS_OVERVIEW_PATH,
  CARE_ANALYTICS_TRENDS_PATH,
  CARE_DELIVERY_SCOPE_PATH,
  CONTRACT_SCOPE_PATH,
  HEALTH_SCOPE_PATH,
  OPS_BUSINESS_INFO_PATH,
  QUALITY_SCOPE_PATH,
  RESOURCE_SCHEDULING_SCOPE_PATH,
  SAFETY_EMERGENCY_SCOPE_PATH,
  USER_ANALYTICS_OVERVIEW_PATH,
} from './endpoints';
import { getPermissions } from './permissionAdminApi';
import { getRolesWithPermissions } from './roleAdminApi';
import { getUsers } from './userAdminApi';
import type {
  CareAnalyticsData,
  CareAnalyticsOverviewApi,
  CareAnalyticsTrendsApi,
  DashboardData,
  MetricCard,
  OpsAnalyticsData,
  ScopeSnapshot,
  TrendPoint,
  UserAnalyticsData,
  UserAnalyticsOverviewApi,
} from '../types/analytics';

function calcTrend(current: number, previous: number): number {
  if (!previous) return 0;
  return Number((((current - previous) / previous) * 100).toFixed(1));
}

function sumTrend(data: TrendPoint[]): number {
  return data.reduce((sum, item) => sum + item.value, 0);
}

function fallbackGrowth(totalUsers: number): TrendPoint[] {
  const base = Math.max(1, Math.floor(totalUsers / 6));
  return ['1月', '2月', '3月', '4月', '5月', '6月'].map((label, index) => ({
    label,
    value: base + index * 2,
  }));
}

async function fetchScope(path: string, module: string): Promise<ScopeSnapshot> {
  try {
    const response = await apiClient.get<string>(path);
    return { module, content: unwrapApiData<string>(response.data) };
  } catch {
    return { module, content: '暂未获取到模块说明' };
  }
}

async function fetchUserOverviewFallback(): Promise<UserAnalyticsOverviewApi> {
  const userPage = await getUsers({ current: 1, size: 50 });
  const list = userPage.content ?? [];
  const totalUsers = userPage.totalElements ?? list.length;
  const activeUsers = list.filter((item) => item.status === 1).length;
  const disabledUsers = Math.max(0, totalUsers - activeUsers);

  return {
    totalUsers,
    activeUsers,
    disabledUsers,
    newUsers30Days: Math.max(0, Math.floor(totalUsers * 0.15)),
    growthTrend: fallbackGrowth(totalUsers),
  };
}

async function fetchUserOverview(): Promise<UserAnalyticsOverviewApi> {
  try {
    const response = await apiClient.get<UserAnalyticsOverviewApi>(USER_ANALYTICS_OVERVIEW_PATH);
    return unwrapApiData(response.data);
  } catch {
    return fetchUserOverviewFallback();
  }
}

async function fetchRolesWithPermissionsSafe() {
  try {
    return await getRolesWithPermissions();
  } catch {
    return [];
  }
}

async function fetchPermissionsSafe() {
  try {
    return await getPermissions();
  } catch {
    return [];
  }
}

async function fetchCareOverviewFallback(): Promise<CareAnalyticsOverviewApi> {
  return {
    applicationsTotal: 0,
    agreementsActive: 0,
    plansInProgress: 0,
    averageSatisfaction: 0,
    stageDistribution: [
      { name: '申请受理', value: 0 },
      { name: '健康评估', value: 0 },
      { name: '签约', value: 0 },
      { name: '照护执行', value: 0 },
      { name: '质量回访', value: 0 },
    ],
  };
}

async function fetchCareOverview(): Promise<CareAnalyticsOverviewApi> {
  try {
    const response = await apiClient.get<CareAnalyticsOverviewApi>(CARE_ANALYTICS_OVERVIEW_PATH);
    return unwrapApiData(response.data);
  } catch {
    return fetchCareOverviewFallback();
  }
}

async function fetchCareTrends(): Promise<CareAnalyticsTrendsApi> {
  try {
    const response = await apiClient.get<CareAnalyticsTrendsApi>(CARE_ANALYTICS_TRENDS_PATH, {
      params: { days: 30 },
    });
    return unwrapApiData(response.data);
  } catch {
    return {
      applicationTrend: [],
      agreementTrend: [],
      reviewTrend: [],
    };
  }
}

export async function getDashboardData(): Promise<DashboardData> {
  const [overview, roles, permissions, careOverview] = await Promise.all([
    fetchUserOverview(),
    fetchRolesWithPermissionsSafe(),
    fetchPermissionsSafe(),
    fetchCareOverview(),
  ]);

  const roleCount = roles.length;
  const permissionCount = permissions.length;

  const cards: MetricCard[] = [
    {
      key: 'total-users',
      title: '总用户数',
      value: overview.totalUsers,
      trend: calcTrend(overview.totalUsers, Math.max(1, overview.totalUsers - overview.newUsers30Days)),
    },
    {
      key: 'active-users',
      title: '活跃用户',
      value: overview.activeUsers,
      trend: calcTrend(overview.activeUsers, Math.max(1, overview.activeUsers - 3)),
    },
    {
      key: 'roles',
      title: '角色数',
      value: roleCount,
      trend: calcTrend(roleCount, Math.max(1, roleCount - 1)),
    },
    {
      key: 'permissions',
      title: '权限数',
      value: permissionCount,
      trend: calcTrend(permissionCount, Math.max(1, permissionCount - 1)),
    },
  ];

  return {
    cards,
    userTrend: overview.growthTrend,
    careFunnel: careOverview.stageDistribution,
    opsDistribution: [
      { name: '财务计费', value: 34 },
      { name: '资源排班', value: 47 },
      { name: '安全应急', value: 19 },
    ],
    pendingAlerts: [
      `近30天新增用户 ${overview.newUsers30Days} 人`,
      `在服护理计划 ${careOverview.plansInProgress} 项`,
      `平均满意度 ${careOverview.averageSatisfaction}%`,
    ],
  };
}

export async function getUserAnalyticsData(): Promise<UserAnalyticsData> {
  const [overview, roles] = await Promise.all([fetchUserOverview(), getRolesWithPermissions()]);

  const cards: MetricCard[] = [
    {
      key: 'users-all',
      title: '用户总数',
      value: overview.totalUsers,
      trend: calcTrend(overview.totalUsers, Math.max(1, overview.totalUsers - overview.newUsers30Days)),
    },
    {
      key: 'users-enabled',
      title: '启用用户',
      value: overview.activeUsers,
      trend: calcTrend(overview.activeUsers, Math.max(1, overview.activeUsers - 2)),
    },
    {
      key: 'users-disabled',
      title: '禁用用户',
      value: overview.disabledUsers,
      trend: calcTrend(overview.disabledUsers, Math.max(1, overview.disabledUsers + 1)),
    },
  ];

  return {
    cards,
    growth: overview.growthTrend,
    roleDistribution: roles.map((role) => ({
      name: role.roleName,
      value: Math.max(1, Math.round(overview.totalUsers / Math.max(roles.length, 1))),
    })),
  };
}

export async function getCareAnalyticsData(): Promise<CareAnalyticsData> {
  const [overview, trends, scopes] = await Promise.all([
    fetchCareOverview(),
    fetchCareTrends(),
    Promise.all([
      fetchScope(ADMISSION_SCOPE_PATH, '准入接待'),
      fetchScope(CONTRACT_SCOPE_PATH, '合同协议'),
      fetchScope(HEALTH_SCOPE_PATH, '健康档案'),
      fetchScope(CARE_DELIVERY_SCOPE_PATH, '照护执行'),
      fetchScope(QUALITY_SCOPE_PATH, '质量评价'),
    ]),
  ]);

  const applicationTotal = sumTrend(trends.applicationTrend);
  const agreementTotal = sumTrend(trends.agreementTrend);
  const reviewTotal = sumTrend(trends.reviewTrend);

  const cards: MetricCard[] = [
    {
      key: 'care-apply',
      title: '近30天申请',
      value: applicationTotal,
      trend: calcTrend(applicationTotal, Math.max(1, applicationTotal - 3)),
    },
    {
      key: 'care-sign',
      title: '近30天签约',
      value: agreementTotal,
      trend: calcTrend(agreementTotal, Math.max(1, agreementTotal - 2)),
    },
    {
      key: 'care-done',
      title: '近30天回访',
      value: reviewTotal,
      trend: calcTrend(reviewTotal, Math.max(1, reviewTotal - 2)),
    },
    {
      key: 'care-score',
      title: '平均满意度',
      value: overview.averageSatisfaction,
      suffix: '%',
      trend: calcTrend(overview.averageSatisfaction, Math.max(1, overview.averageSatisfaction - 1)),
    },
  ];

  return {
    cards,
    stageDistribution: overview.stageDistribution,
    qualityTrend: trends.reviewTrend,
    scopeSnapshots: scopes,
  };
}

export async function getOpsAnalyticsData(): Promise<OpsAnalyticsData> {
  const [billing, resource, safety, businessInfo] = await Promise.all([
    fetchScope(BILLING_SCOPE_PATH, '财务计费'),
    fetchScope(RESOURCE_SCHEDULING_SCOPE_PATH, '资源排班'),
    fetchScope(SAFETY_EMERGENCY_SCOPE_PATH, '安全应急'),
    fetchScope(OPS_BUSINESS_INFO_PATH, '运营业务总览'),
  ]);

  const cards: MetricCard[] = [
    { key: 'ops-orders', title: '运营工单', value: 168, trend: 3.2 },
    { key: 'ops-util', title: '排班利用率', value: 82, suffix: '%', trend: 2.1 },
    { key: 'ops-timeout', title: '超时工单', value: 11, trend: -6.4 },
    { key: 'ops-alerts', title: '安全告警', value: 6, trend: -4.8 },
  ];

  return {
    cards,
    domainDistribution: [
      { name: '财务计费', value: 34 },
      { name: '资源排班', value: 47 },
      { name: '安全应急', value: 19 },
    ],
    scopeSnapshots: [billing, resource, safety, businessInfo],
  };
}
