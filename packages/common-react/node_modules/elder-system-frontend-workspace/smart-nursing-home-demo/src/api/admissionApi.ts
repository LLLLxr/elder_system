import apiClient, { unwrapApiData } from './client';
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
  const response = await apiClient.post(ADMISSION_APPLICATIONS_PATH, payload);
  return unwrapApiData<ServiceApplication>(response.data);
}

export async function assessServiceApplication(
  payload: EligibilityAssessmentRequest,
): Promise<ServiceApplication> {
  const response = await apiClient.post(ADMISSION_ASSESSMENTS_PATH, payload);
  return unwrapApiData<ServiceApplication>(response.data);
}

export async function listPendingAssessments(): Promise<ServiceApplication[]> {
  const response = await apiClient.get(ADMISSION_APPLICATIONS_PATH, {
    params: { status: 'SUBMITTED' },
  });
  return unwrapApiData<ServiceApplication[]>(response.data);
}

export async function continueJourneyAfterAssessment(
  applicationId: number,
): Promise<ServiceJourneyResult> {
  const response = await apiClient.post(CARE_JOURNEY_CONTINUE_PATH, null, {
    params: { applicationId },
  });
  return unwrapApiData<ServiceJourneyResult>(response.data);
}
