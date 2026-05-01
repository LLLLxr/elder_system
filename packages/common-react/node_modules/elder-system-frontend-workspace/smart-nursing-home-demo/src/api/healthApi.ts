import apiClient, { unwrapApiData } from './client';
import type {
  HealthAssessmentRequest,
  HealthAssessmentSubmitRequest,
  HealthCheckForm,
  HealthCheckFormCreateRequest,
} from '../types/care';
import {
  ADMIN_HEALTH_CHECK_FORM_DETAIL_PATH,
  ADMIN_HEALTH_CHECK_FORM_LATEST_PATH,
  ADMIN_HEALTH_CHECK_FORMS_PATH,
  HEALTH_ASSESSMENT_REQUESTS_HISTORY_PATH,
  HEALTH_ASSESSMENT_REQUESTS_PATH,
  HEALTH_ASSESSMENT_REQUESTS_PENDING_PATH,
} from './endpoints';

export async function createAdminHealthCheckForm(payload: HealthCheckFormCreateRequest): Promise<HealthCheckForm> {
  const response = await apiClient.post(ADMIN_HEALTH_CHECK_FORMS_PATH, payload);
  return unwrapApiData<HealthCheckForm>(response.data);
}

export async function getAdminHealthCheckForm(formId: number): Promise<HealthCheckForm> {
  const response = await apiClient.get(ADMIN_HEALTH_CHECK_FORM_DETAIL_PATH(formId));
  return unwrapApiData<HealthCheckForm>(response.data);
}

export async function getLatestAdminHealthCheckForm(
  elderId: number,
  agreementId?: number,
): Promise<HealthCheckForm> {
  const response = await apiClient.get(ADMIN_HEALTH_CHECK_FORM_LATEST_PATH, {
    params: { elderId, agreementId },
  });
  return unwrapApiData<HealthCheckForm>(response.data);
}

export async function listPendingHealthAssessments(): Promise<HealthAssessmentRequest[]> {
  const response = await apiClient.get(HEALTH_ASSESSMENT_REQUESTS_PENDING_PATH);
  return unwrapApiData<HealthAssessmentRequest[]>(response.data);
}

export async function listHealthAssessmentHistory(): Promise<HealthAssessmentRequest[]> {
  const response = await apiClient.get(HEALTH_ASSESSMENT_REQUESTS_HISTORY_PATH);
  return unwrapApiData<HealthAssessmentRequest[]>(response.data);
}

export async function submitHealthAssessment(
  payload: HealthAssessmentSubmitRequest,
): Promise<HealthAssessmentRequest> {
  const response = await apiClient.post(HEALTH_ASSESSMENT_REQUESTS_PATH, payload);
  return unwrapApiData<HealthAssessmentRequest>(response.data);
}
