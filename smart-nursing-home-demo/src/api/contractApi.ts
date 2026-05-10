import apiClient, { unwrapApiData } from './client';
import { CONTRACT_AGREEMENT_BY_APPLICATION_PATH, CONTRACT_DRAFT_AGREEMENT_PATH } from './endpoints';
import type { ServiceAgreement } from '../types/care';

export async function getAgreementByApplication(applicationId: number): Promise<ServiceAgreement> {
  const response = await apiClient.get(CONTRACT_AGREEMENT_BY_APPLICATION_PATH(applicationId));
  return unwrapApiData<ServiceAgreement>(response.data);
}

export async function createDraftAgreement(payload: ServiceAgreement): Promise<ServiceAgreement> {
  const response = await apiClient.post(CONTRACT_DRAFT_AGREEMENT_PATH, payload);
  return unwrapApiData<ServiceAgreement>(response.data);
}
