import apiClient, { unwrapApiData } from './client';
import type { DoctorRoundRecord, HealthCheckForm } from '../types/care';
import {
  HEALTH_CHECK_FORM_DETAIL_PATH,
  HEALTH_CHECK_FORM_LATEST_PATH,
  HEALTH_CHECK_FORMS_PATH,
  HEALTH_FAMILY_DOCTOR_ROUND_RECORDS_PATH,
} from './endpoints';

export async function getHealthCheckForm(formId: number): Promise<HealthCheckForm> {
  const response = await apiClient.get(HEALTH_CHECK_FORM_DETAIL_PATH(formId));
  return unwrapApiData<HealthCheckForm>(response.data);
}

export async function getLatestHealthCheckForm(
  elderId: number,
  agreementId?: number,
): Promise<HealthCheckForm> {
  const response = await apiClient.get(HEALTH_CHECK_FORM_LATEST_PATH, {
    params: { elderId, agreementId },
  });
  return unwrapApiData<HealthCheckForm>(response.data);
}

export async function listHealthCheckForms(
  elderId: number,
  agreementId?: number,
): Promise<HealthCheckForm[]> {
  const response = await apiClient.get(HEALTH_CHECK_FORMS_PATH, {
    params: { elderId, agreementId },
  });
  return unwrapApiData<HealthCheckForm[]>(response.data);
}

export async function listFamilyDoctorRoundRecords(
  elderId: number,
  roundDate?: string,
): Promise<DoctorRoundRecord[]> {
  const response = await apiClient.get(HEALTH_FAMILY_DOCTOR_ROUND_RECORDS_PATH(elderId), {
    params: { roundDate },
  });
  return unwrapApiData<DoctorRoundRecord[]>(response.data);
}
