import { Navigate, Route, Routes } from 'react-router-dom';
import { hasToken } from './api/client';
import AdminLayout from './components/AdminLayout';
import AdminHomePage from './pages/admin/AdminHomePage';
import AlertsPage from './pages/admin/AlertsPage';
import CareAnalyticsPage from './pages/admin/CareAnalyticsPage';
import NeedsAssessmentPage from './pages/admin/NeedsAssessmentPage';
import HealthCheckFormEntryPage from './pages/admin/HealthCheckFormEntryPage';
import HealthAssessmentPage from './pages/admin/HealthAssessmentPage';
import JourneyTaskBoardPage from './pages/admin/JourneyTaskBoardPage';
import DashboardPage from './pages/admin/DashboardPage';
import OpsAnalyticsPage from './pages/admin/OpsAnalyticsPage';
import PermissionManagementPage from './pages/admin/PermissionManagementPage';
import RoleManagementPage from './pages/admin/RoleManagementPage';
import UserAnalyticsPage from './pages/admin/UserAnalyticsPage';
import UserManagementPage from './pages/admin/UserManagementPage';
import LoginPage from './pages/LoginPage';
import { ROUTE_PATHS } from './constants/routes';

function RequireAuth({ children }: { children: JSX.Element }) {
  if (!hasToken()) {
    return <Navigate to={ROUTE_PATHS.LOGIN} replace />;
  }
  return children;
}

export default function App() {
  return (
    <Routes>
      <Route path={ROUTE_PATHS.LOGIN} element={<LoginPage />} />

      <Route
        path="/admin"
        element={
          <RequireAuth>
            <AdminLayout />
          </RequireAuth>
        }
      >
        <Route path="home" element={<AdminHomePage />} />
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="admission" element={<Navigate to={ROUTE_PATHS.ADMIN_NEEDS_ASSESSMENT} replace />} />
        <Route path="needs-assessment" element={<NeedsAssessmentPage />} />
        <Route path="health-check-forms" element={<HealthCheckFormEntryPage />} />
        <Route path="health-assessment" element={<HealthAssessmentPage />} />
        <Route path="journey-tasks" element={<JourneyTaskBoardPage />} />
        <Route path="users-analytics" element={<UserAnalyticsPage />} />
        <Route path="care" element={<CareAnalyticsPage />} />
        <Route path="ops" element={<OpsAnalyticsPage />} />
        <Route path="alerts" element={<AlertsPage />} />
        <Route path="users" element={<UserManagementPage />} />
        <Route path="roles" element={<RoleManagementPage />} />
        <Route path="permissions" element={<PermissionManagementPage />} />
      </Route>

      <Route
        path="*"
        element={<Navigate to={hasToken() ? ROUTE_PATHS.ADMIN_DASHBOARD : ROUTE_PATHS.LOGIN} replace />}
      />
    </Routes>
  );
}
