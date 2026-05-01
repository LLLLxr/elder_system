export interface MetricCard {
  key: string;
  title: string;
  value: number;
  suffix?: string;
  trend: number;
}

export interface TrendPoint {
  label: string;
  value: number;
}

export interface PieDatum {
  name: string;
  value: number;
}

export interface ScopeSnapshot {
  module: string;
  content: string;
}

export interface DashboardData {
  cards: MetricCard[];
  userTrend: TrendPoint[];
  careFunnel: PieDatum[];
  opsDistribution: PieDatum[];
  pendingAlerts: string[];
}

export interface UserAnalyticsData {
  cards: MetricCard[];
  growth: TrendPoint[];
  roleDistribution: PieDatum[];
}

export interface CareAnalyticsData {
  cards: MetricCard[];
  stageDistribution: PieDatum[];
  qualityTrend: TrendPoint[];
  scopeSnapshots: ScopeSnapshot[];
}

export interface OpsAnalyticsData {
  cards: MetricCard[];
  domainDistribution: PieDatum[];
  scopeSnapshots: ScopeSnapshot[];
}

export interface UserAnalyticsOverviewApi {
  totalUsers: number;
  activeUsers: number;
  disabledUsers: number;
  newUsers30Days: number;
  growthTrend: TrendPoint[];
}

export interface CareAnalyticsOverviewApi {
  applicationsTotal: number;
  agreementsActive: number;
  plansInProgress: number;
  averageSatisfaction: number;
  stageDistribution: PieDatum[];
}

export interface CareAnalyticsTrendsApi {
  applicationTrend: TrendPoint[];
  agreementTrend: TrendPoint[];
  reviewTrend: TrendPoint[];
}
