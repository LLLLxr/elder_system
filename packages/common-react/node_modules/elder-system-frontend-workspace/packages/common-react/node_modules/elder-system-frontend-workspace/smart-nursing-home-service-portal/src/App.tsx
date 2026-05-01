import { Spin } from 'antd';
import { useEffect, useState } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { hasToken } from './api/client';
import JourneyLayout from './components/JourneyLayout';
import HealthCheckFormPage from './pages/HealthCheckFormPage';
import JourneyOverviewPage from './pages/JourneyOverviewPage';
import JourneyResultPage from './pages/JourneyResultPage';
import JourneyReviewPage from './pages/JourneyReviewPage';
import JourneyStartPage from './pages/JourneyStartPage';
import JourneyTaskBoardPage from './pages/JourneyTaskBoardPage';
import LoginPage from './pages/LoginPage';
import { ROUTE_PATHS } from './constants/routes';
import { ASSESSMENT_REVIEW_PERMISSIONS, ensureAssessmentReviewPermissions, hasAnyPermission } from './stores/userStore';

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

      const resolvedPermissions = await ensureAssessmentReviewPermissions();
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

export default function App() {
  return (
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
        <Route path="overview" element={<JourneyOverviewPage />} />
        <Route path="start" element={<JourneyStartPage />} />
        <Route path="tasks" element={<JourneyTaskBoardPage />} />
        <Route
          path="review"
          element={
            <RequirePermission permissions={ASSESSMENT_REVIEW_PERMISSIONS}>
              <JourneyReviewPage />
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
  );
}
