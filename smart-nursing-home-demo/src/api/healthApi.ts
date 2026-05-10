import apiClient, { unwrapApiData } from './client';
import type { ApiResponse } from '../types';
import type {
  DoctorRoundRecord,
  DoctorRoundRecordSaveRequest,
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
  HEALTH_DOCTOR_ROUND_RECORD_DETAIL_PATH,
  HEALTH_DOCTOR_ROUND_RECORDS_PATH,
} from './endpoints';

export async function createAdminHealthCheckForm(payload: HealthCheckFormCreateRequest): Promise<HealthCheckForm> {
  const response = await apiClient.post<ApiResponse<HealthCheckForm>>(ADMIN_HEALTH_CHECK_FORMS_PATH, payload);
  return unwrapApiData<HealthCheckForm>(response.data);
}

export async function getAdminHealthCheckForm(formId: number): Promise<HealthCheckForm> {
  const response = await apiClient.get<ApiResponse<HealthCheckForm>>(ADMIN_HEALTH_CHECK_FORM_DETAIL_PATH(formId));
  return unwrapApiData<HealthCheckForm>(response.data);
}

export async function getLatestAdminHealthCheckForm(
  elderId: number,
  agreementId?: number,
): Promise<HealthCheckForm> {
  const response = await apiClient.get<ApiResponse<HealthCheckForm>>(ADMIN_HEALTH_CHECK_FORM_LATEST_PATH, {
    params: { elderId, agreementId },
  });
  return unwrapApiData<HealthCheckForm>(response.data);
}

export async function listPendingHealthAssessments(): Promise<HealthAssessmentRequest[]> {
  const response = await apiClient.get<ApiResponse<HealthAssessmentRequest[]>>(HEALTH_ASSESSMENT_REQUESTS_PENDING_PATH);
  return unwrapApiData<HealthAssessmentRequest[]>(response.data);
}

export async function listHealthAssessmentHistory(): Promise<HealthAssessmentRequest[]> {
  const response = await apiClient.get<ApiResponse<HealthAssessmentRequest[]>>(HEALTH_ASSESSMENT_REQUESTS_HISTORY_PATH);
  return unwrapApiData<HealthAssessmentRequest[]>(response.data);
}

export async function submitHealthAssessment(
  payload: HealthAssessmentSubmitRequest,
): Promise<HealthAssessmentRequest> {
  const response = await apiClient.post<ApiResponse<HealthAssessmentRequest>>(HEALTH_ASSESSMENT_REQUESTS_PATH, payload);
  return unwrapApiData<HealthAssessmentRequest>(response.data);
}

export async function listDoctorRoundRecords(params?: {
  elderId?: number;
  doctorId?: number;
  roundDate?: string;
}): Promise<DoctorRoundRecord[]> {
  const response = await apiClient.get<ApiResponse<DoctorRoundRecord[]>>(HEALTH_DOCTOR_ROUND_RECORDS_PATH, { params });
  return unwrapApiData<DoctorRoundRecord[]>(response.data);
}

export async function getDoctorRoundRecord(recordId: number): Promise<DoctorRoundRecord> {
  const response = await apiClient.get<ApiResponse<DoctorRoundRecord>>(HEALTH_DOCTOR_ROUND_RECORD_DETAIL_PATH(recordId));
  return unwrapApiData<DoctorRoundRecord>(response.data);
}

export async function createDoctorRoundRecord(
  payload: DoctorRoundRecordSaveRequest,
): Promise<DoctorRoundRecord> {
  const response = await apiClient.post<ApiResponse<DoctorRoundRecord>>(HEALTH_DOCTOR_ROUND_RECORDS_PATH, payload);
  return unwrapApiData<DoctorRoundRecord>(response.data);
}

export async function updateDoctorRoundRecord(
  recordId: number,
  payload: DoctorRoundRecordSaveRequest,
): Promise<DoctorRoundRecord> {
  const response = await apiClient.put<ApiResponse<DoctorRoundRecord>>(HEALTH_DOCTOR_ROUND_RECORD_DETAIL_PATH(recordId), payload);
  return unwrapApiData<DoctorRoundRecord>(response.data);
}
