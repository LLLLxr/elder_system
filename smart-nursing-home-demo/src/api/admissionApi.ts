import apiClient, { unwrapApiData } from './client';
import type { ApiResponse } from '../types';
import {
  ADMISSION_APPLICATIONS_PATH,
  ADMISSION_ASSESSMENTS_PATH,
  CARE_JOURNEY_CONTINUE_PATH,
} from './endpoints';
import type {
  EligibilityAssessmentRequest,
  ServiceApplication,
  ServiceJourneyResult,
} from '../types/care';

export async function submitServiceApplication(
  payload: ServiceApplication,
): Promise<ServiceApplication> {
  const response = await apiClient.post<ApiResponse<ServiceApplication>>(ADMISSION_APPLICATIONS_PATH, payload);
  return unwrapApiData<ServiceApplication>(response.data);
}

export async function assessServiceApplication(
  payload: EligibilityAssessmentRequest,
): Promise<ServiceApplication> {
  const response = await apiClient.post<ApiResponse<ServiceApplication>>(ADMISSION_ASSESSMENTS_PATH, payload);
  return unwrapApiData<ServiceApplication>(response.data);
}

export async function listApplicationsByStatus(status: string): Promise<ServiceApplication[]> {
  const response = await apiClient.get<ApiResponse<ServiceApplication[]>>(ADMISSION_APPLICATIONS_PATH, {
    params: { status },
  });
  return unwrapApiData<ServiceApplication[]>(response.data);
}

export async function listPendingAssessments(): Promise<ServiceApplication[]> {
  return listApplicationsByStatus('SUBMITTED');
}

export async function continueJourneyAfterAssessment(
  applicationId: number,
): Promise<ServiceJourneyResult> {
  const response = await apiClient.post<ApiResponse<ServiceJourneyResult>>(CARE_JOURNEY_CONTINUE_PATH, null, {
    params: { applicationId },
  });
  return unwrapApiData<ServiceJourneyResult>(response.data);
}
