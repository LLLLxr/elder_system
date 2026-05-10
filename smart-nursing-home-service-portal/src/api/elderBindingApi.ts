import { apiClient } from './client';

export interface ElderBindingDto {
  id: number;
  userId: number;
  elderId: number;
  elderName: string;
  bindingType: 'SELF' | 'FAMILY';
  relationToElder?: string;
  status: string;
}

export interface SelfBindingRequest {
  elderName: string;
  idCard: string;
  phone?: string;
  gender?: string;
  birthDate?: string;
}

export interface FamilyBindingRequest {
  elderId: number;
  relationToElder: string;
  requestReason?: string;
}

export async function listMyElderBindings(): Promise<ElderBindingDto[]> {
  const response = await apiClient.get('/api/users/me/elder-bindings');
  return response.data;
}

export async function createSelfBinding(request: SelfBindingRequest): Promise<ElderBindingDto> {
  const response = await apiClient.post('/api/users/me/elder-bindings/self', request);
  return response.data;
}

export async function createFamilyBindingRequest(request: FamilyBindingRequest): Promise<void> {
  await apiClient.post('/api/users/me/elder-binding-requests/family', request);
}
