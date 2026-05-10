import apiClient, { unwrapApiData } from './client';
import type { ApiResponse } from '../types';
import type { CaregiverQualificationApplication, CaregiverQualificationReviewPayload } from '../types/care';
import {
  QUALITY_CAREGIVER_QUALIFICATION_APPLICATIONS_PATH,
  QUALITY_CAREGIVER_QUALIFICATION_APPLICATION_APPROVE_PATH,
  QUALITY_CAREGIVER_QUALIFICATION_APPLICATION_DETAIL_PATH,
  QUALITY_CAREGIVER_QUALIFICATION_APPLICATION_REJECT_PATH,
} from './endpoints';

export async function listCaregiverQualificationApplications(status?: string): Promise<CaregiverQualificationApplication[]> {
  const response = await apiClient.get<ApiResponse<CaregiverQualificationApplication[]>>(QUALITY_CAREGIVER_QUALIFICATION_APPLICATIONS_PATH, {
    params: { status },
  });
  return unwrapApiData<CaregiverQualificationApplication[]>(response.data);
}

export async function getCaregiverQualificationApplicationDetail(applicationId: number): Promise<CaregiverQualificationApplication> {
  const response = await apiClient.get<ApiResponse<CaregiverQualificationApplication>>(QUALITY_CAREGIVER_QUALIFICATION_APPLICATION_DETAIL_PATH(applicationId));
  return unwrapApiData<CaregiverQualificationApplication>(response.data);
}

export async function approveCaregiverQualificationApplication(
  applicationId: number,
  payload?: CaregiverQualificationReviewPayload,
): Promise<CaregiverQualificationApplication> {
  const response = await apiClient.post<ApiResponse<CaregiverQualificationApplication>>(QUALITY_CAREGIVER_QUALIFICATION_APPLICATION_APPROVE_PATH(applicationId), payload ?? {});
  return unwrapApiData<CaregiverQualificationApplication>(response.data);
}

export async function rejectCaregiverQualificationApplication(
  applicationId: number,
  payload?: CaregiverQualificationReviewPayload,
): Promise<CaregiverQualificationApplication> {
  const response = await apiClient.post<ApiResponse<CaregiverQualificationApplication>>(QUALITY_CAREGIVER_QUALIFICATION_APPLICATION_REJECT_PATH(applicationId), payload ?? {});
  return unwrapApiData<CaregiverQualificationApplication>(response.data);
}
