import apiClient, { unwrapApiData } from './client';
import type { ApiResponse } from '../types';
import type { FamilyVisitReservation, FamilyVisitReservationRule, FamilyVisitSlot } from '../types/care';
import {
  ADMISSION_FAMILY_VISIT_MY_RESERVATIONS_PATH,
  ADMISSION_FAMILY_VISIT_RESERVATION_RULES_PATH,
  ADMISSION_FAMILY_VISIT_RESERVATIONS_PATH,
  ADMISSION_FAMILY_VISIT_SLOTS_PATH,
} from './endpoints';

export async function getFamilyVisitReservationRules(): Promise<FamilyVisitReservationRule> {
  const response = await apiClient.get<ApiResponse<FamilyVisitReservationRule>>(ADMISSION_FAMILY_VISIT_RESERVATION_RULES_PATH);
  return unwrapApiData<FamilyVisitReservationRule>(response.data);
}

export async function listFamilyVisitSlots(slotDate?: string): Promise<FamilyVisitSlot[]> {
  const response = await apiClient.get<ApiResponse<FamilyVisitSlot[]>>(ADMISSION_FAMILY_VISIT_SLOTS_PATH, {
    params: { slotDate },
  });
  return unwrapApiData<FamilyVisitSlot[]>(response.data);
}

export async function createFamilyVisitReservation(payload: FamilyVisitReservation): Promise<FamilyVisitReservation> {
  const response = await apiClient.post<ApiResponse<FamilyVisitReservation>>(ADMISSION_FAMILY_VISIT_RESERVATIONS_PATH, payload);
  return unwrapApiData<FamilyVisitReservation>(response.data);
}

export async function listMyFamilyVisitReservations(): Promise<FamilyVisitReservation[]> {
  const response = await apiClient.get<ApiResponse<FamilyVisitReservation[]>>(ADMISSION_FAMILY_VISIT_MY_RESERVATIONS_PATH);
  return unwrapApiData<FamilyVisitReservation[]>(response.data);
}
