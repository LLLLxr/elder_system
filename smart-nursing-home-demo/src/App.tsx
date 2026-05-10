import { Navigate, Route, Routes } from 'react-router-dom';
import { hasToken } from './api/client';
import AdminLayout from './components/AdminLayout';
import { ROUTE_PATHS } from './constants/routes';
import AdminHomePage from './pages/admin/AdminHomePage';
import AlertsPage from './pages/admin/AlertsPage';
import CareAnalyticsPage from './pages/admin/CareAnalyticsPage';
import DashboardPage from './pages/admin/DashboardPage';
import FamilyVisitReviewPage from './pages/admin/FamilyVisitReviewPage';
import ElderBindingReviewPage from './pages/admin/ElderBindingReviewPage';
import HealthAssessmentPage from './pages/admin/HealthAssessmentPage';
import HealthCheckFormEntryPage from './pages/admin/HealthCheckFormEntryPage';
import JourneyTaskBoardPage from './pages/admin/JourneyTaskBoardPage';
import NurseCareRecordsPage from './pages/admin/NurseCareRecordsPage';
import DoctorRoundRecordsPage from './pages/admin/DoctorRoundRecordsPage';
import NeedsAssessmentPage from './pages/admin/NeedsAssessmentPage';
import OpsAnalyticsPage from './pages/admin/OpsAnalyticsPage';
import PermissionManagementPage from './pages/admin/PermissionManagementPage';
import RoleManagementPage from './pages/admin/RoleManagementPage';
import UserAnalyticsPage from './pages/admin/UserAnalyticsPage';
import UserManagementPage from './pages/admin/UserManagementPage';
import CaregiverQualificationReviewPage from './pages/admin/CaregiverQualificationReviewPage';
import LoginPage from './pages/LoginPage';

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
        <Route index element={<Navigate to={ROUTE_PATHS.ADMIN_DASHBOARD} replace />} />
        <Route path="home" element={<AdminHomePage />} />
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="admission" element={<Navigate to={ROUTE_PATHS.ADMIN_NEEDS_ASSESSMENT} replace />} />
        <Route path="needs-assessment" element={<NeedsAssessmentPage />} />
        <Route path="health-check-forms" element={<HealthCheckFormEntryPage />} />
        <Route path="health-assessment" element={<HealthAssessmentPage />} />
        <Route path="nurse-care-records" element={<NurseCareRecordsPage />} />
        <Route path="doctor-round-records" element={<DoctorRoundRecordsPage />} />
        <Route path="family-visit-reviews" element={<FamilyVisitReviewPage />} />
        <Route path="elder-binding-reviews" element={<ElderBindingReviewPage />} />
        <Route path="caregiver-qualification-reviews" element={<CaregiverQualificationReviewPage />} />
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
