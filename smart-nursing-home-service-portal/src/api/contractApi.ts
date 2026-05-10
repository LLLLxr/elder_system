import apiClient, { unwrapApiData } from './client';
import type { ServiceAgreement } from '../types/care';
import { CONTRACT_AGREEMENT_BY_APPLICATION_PATH, CONTRACT_AGREEMENT_PATH, CONTRACT_AGREEMENT_SIGN_PATH } from './endpoints';

export async function getAgreement(agreementId: number): Promise<ServiceAgreement> {
  const response = await apiClient.get(CONTRACT_AGREEMENT_PATH(agreementId));
  return unwrapApiData<ServiceAgreement>(response.data);
}

export async function getLatestAgreementByApplicationId(applicationId: number): Promise<ServiceAgreement> {
  const response = await apiClient.get(CONTRACT_AGREEMENT_BY_APPLICATION_PATH(applicationId));
  return unwrapApiData<ServiceAgreement>(response.data);
}

export async function signAgreement(request: ServiceAgreement): Promise<ServiceAgreement> {
  const response = await apiClient.post(CONTRACT_AGREEMENT_SIGN_PATH, request);
  return unwrapApiData<ServiceAgreement>(response.data);
}
