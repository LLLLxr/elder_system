import { Spin } from 'antd';
import { lazy, Suspense, useEffect, useState } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { hasToken } from './api/client';
import JourneyLayout from './components/JourneyLayout';
import { ROUTE_PATHS } from './constants/routes';
const CaregiverCheckInHistoryPage = lazy(() => import('./pages/CaregiverCheckInHistoryPage'));
const CaregiverDailyTasksPage = lazy(() => import('./pages/CaregiverDailyTasksPage'));
const CaregiverQualificationStatusPage = lazy(() => import('./pages/CaregiverQualificationStatusPage'));
const CaregiverQualificationSubmitPage = lazy(() => import('./pages/CaregiverQualificationSubmitPage'));
const FamilyCareRecordsPage = lazy(() => import('./pages/FamilyCareRecordsPage'));
const FamilyCheckInRecordsPage = lazy(() => import('./pages/FamilyCheckInRecordsPage'));
const FamilyServicePlansPage = lazy(() => import('./pages/FamilyServicePlansPage'));
const FamilyVisitReservationCreatePage = lazy(() => import('./pages/FamilyVisitReservationCreatePage'));
const HealthCheckFormPage = lazy(() => import('./pages/HealthCheckFormPage'));
const JourneyAgreementPage = lazy(() => import('./pages/JourneyAgreementPage'));
const JourneyOverviewPage = lazy(() => import('./pages/JourneyOverviewPage'));
const JourneyResultPage = lazy(() => import('./pages/JourneyResultPage'));
const JourneyRenewalPage = lazy(() => import('./pages/JourneyRenewalPage'));
const JourneyReviewPage = lazy(() => import('./pages/JourneyReviewPage'));
const JourneyStartPage = lazy(() => import('./pages/JourneyStartPage'));
const JourneyTaskBoardPage = lazy(() => import('./pages/JourneyTaskBoardPage'));
const LoginPage = lazy(() => import('./pages/LoginPage'));
const MyEldersPage = lazy(() => import('./pages/MyEldersPage'));
const MyFamilyVisitReservationsPage = lazy(() => import('./pages/MyFamilyVisitReservationsPage'));
import {
  ASSESSMENT_REVIEW_PERMISSIONS,
  CAREGIVER_DAILY_TASK_PERMISSIONS,
  CAREGIVER_QUALIFICATION_PERMISSIONS,
  ensurePermissions,
  FAMILY_CARE_DELIVERY_PERMISSIONS,
  FAMILY_VISIT_PERMISSIONS,
  hasAnyPermission,
} from './stores/userStore';

function RequireAuth({ children }: { children: JSX.Element }) {
  if (!hasToken()) {
    return <Navigate to={ROUTE_PATHS.LOGIN} replace />;
  }
  return children;
}

function RequirePermission({ children, permissions }: { children: JSX.Element; permissions: readonly string[] }) {
  const [loading, setLoading] = useState(true);
  const [allowed, setAllowed] = useState(false);

  useEffect(() => {
    let canceled = false;

    const verify = async () => {
      if (!hasToken()) {
        if (!canceled) {
          setAllowed(false);
          setLoading(false);
        }
        return;
      }

      if (hasAnyPermission(permissions)) {
        if (!canceled) {
          setAllowed(true);
          setLoading(false);
        }
        return;
      }

      const resolvedPermissions = await ensurePermissions();
      if (!canceled) {
        setAllowed(permissions.some((permission) => resolvedPermissions.includes(permission)));
        setLoading(false);
      }
    };

    void verify();

    return () => {
      canceled = true;
    };
  }, [permissions]);

  if (!hasToken()) {
    return <Navigate to={ROUTE_PATHS.LOGIN} replace />;
  }

  if (loading) {
    return <Spin style={{ display: 'block', margin: '80px auto' }} />;
  }

  if (!allowed) {
    return <Navigate to={ROUTE_PATHS.JOURNEY_OVERVIEW} replace />;
  }

  return children;
}

function PageLoading() {
  return <Spin style={{ display: 'block', margin: '80px auto' }} />;
}

export default function App() {
  return (
    <Suspense fallback={<PageLoading />}>
      <Routes>
      <Route path={ROUTE_PATHS.LOGIN} element={<LoginPage />} />

      <Route
        path="/journey"
        element={
          <RequireAuth>
            <JourneyLayout />
          </RequireAuth>
        }
      >
        <Route index element={<Navigate to={ROUTE_PATHS.JOURNEY_OVERVIEW} replace />} />
        <Route path="overview" element={<JourneyOverviewPage />} />
        <Route
          path="my-elders"
          element={
            <RequirePermission permissions={['elder-binding:list', 'elder-binding:request:create', 'elder-binding:self:bind']}>
              <MyEldersPage />
            </RequirePermission>
          }
        />
        <Route path="start" element={<JourneyStartPage />} />
        <Route path="tasks" element={<JourneyTaskBoardPage />} />
        <Route path="agreement" element={<JourneyAgreementPage />} />
        <Route path="renewal" element={<JourneyRenewalPage />} />
        <Route
          path="review"
          element={
            <RequirePermission permissions={ASSESSMENT_REVIEW_PERMISSIONS}>
              <JourneyReviewPage />
            </RequirePermission>
          }
        />
        <Route
          path="family-visit/create"
          element={
            <RequirePermission permissions={FAMILY_VISIT_PERMISSIONS}>
              <FamilyVisitReservationCreatePage />
            </RequirePermission>
          }
        />
        <Route
          path="family-visit/my"
          element={
            <RequirePermission permissions={FAMILY_VISIT_PERMISSIONS}>
              <MyFamilyVisitReservationsPage />
            </RequirePermission>
          }
        />
        <Route
          path="care-delivery/my-tasks"
          element={
            <RequirePermission permissions={CAREGIVER_DAILY_TASK_PERMISSIONS}>
              <CaregiverDailyTasksPage />
            </RequirePermission>
          }
        />
        <Route
          path="care-delivery/my-check-ins"
          element={
            <RequirePermission permissions={CAREGIVER_DAILY_TASK_PERMISSIONS}>
              <CaregiverCheckInHistoryPage />
            </RequirePermission>
          }
        />
        <Route
          path="family/service-plans"
          element={
            <RequirePermission permissions={FAMILY_CARE_DELIVERY_PERMISSIONS}>
              <FamilyServicePlansPage />
            </RequirePermission>
          }
        />
        <Route
          path="family/check-ins"
          element={
            <RequirePermission permissions={FAMILY_CARE_DELIVERY_PERMISSIONS}>
              <FamilyCheckInRecordsPage />
            </RequirePermission>
          }
        />
        <Route
          path="family/care-records"
          element={
            <RequirePermission permissions={FAMILY_CARE_DELIVERY_PERMISSIONS}>
              <FamilyCareRecordsPage />
            </RequirePermission>
          }
        />
        <Route
          path="caregiver-qualification/submit"
          element={
            <RequirePermission permissions={CAREGIVER_QUALIFICATION_PERMISSIONS}>
              <CaregiverQualificationSubmitPage />
            </RequirePermission>
          }
        />
        <Route
          path="caregiver-qualification/status"
          element={
            <RequirePermission permissions={CAREGIVER_QUALIFICATION_PERMISSIONS}>
              <CaregiverQualificationStatusPage />
            </RequirePermission>
          }
        />
        <Route path="result" element={<JourneyResultPage />} />
      </Route>

      <Route
        path={ROUTE_PATHS.HEALTH_CHECK}
        element={
          <RequireAuth>
            <JourneyLayout />
          </RequireAuth>
        }
      >
        <Route index element={<HealthCheckFormPage />} />
      </Route>

        <Route
          path="*"
          element={<Navigate to={hasToken() ? ROUTE_PATHS.JOURNEY_OVERVIEW : ROUTE_PATHS.LOGIN} replace />}
        />
      </Routes>
    </Suspense>
  );
}
