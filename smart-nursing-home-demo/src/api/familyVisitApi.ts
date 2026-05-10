import apiClient, { unwrapApiData } from './client';
import type { FamilyVisitReservation, FamilyVisitReservationReviewPayload } from '../types/care';
import {
  ADMISSION_FAMILY_VISIT_RESERVATIONS_PATH,
  ADMISSION_FAMILY_VISIT_RESERVATION_APPROVE_PATH,
  ADMISSION_FAMILY_VISIT_RESERVATION_DETAIL_PATH,
  ADMISSION_FAMILY_VISIT_RESERVATION_REJECT_PATH,
} from './endpoints';

export async function listFamilyVisitReservations(status?: string): Promise<FamilyVisitReservation[]> {
  const response = await apiClient.get(ADMISSION_FAMILY_VISIT_RESERVATIONS_PATH, {
    params: { status },
  });
  return unwrapApiData<FamilyVisitReservation[]>(response.data);
}

export async function getFamilyVisitReservationDetail(reservationId: number): Promise<FamilyVisitReservation> {
  const response = await apiClient.get(ADMISSION_FAMILY_VISIT_RESERVATION_DETAIL_PATH(reservationId));
  return unwrapApiData<FamilyVisitReservation>(response.data);
}

export async function approveFamilyVisitReservation(
  reservationId: number,
  payload?: FamilyVisitReservationReviewPayload,
): Promise<FamilyVisitReservation> {
  const response = await apiClient.post(ADMISSION_FAMILY_VISIT_RESERVATION_APPROVE_PATH(reservationId), payload ?? {});
  return unwrapApiData<FamilyVisitReservation>(response.data);
}

export async function rejectFamilyVisitReservation(
  reservationId: number,
  payload?: FamilyVisitReservationReviewPayload,
): Promise<FamilyVisitReservation> {
  const response = await apiClient.post(ADMISSION_FAMILY_VISIT_RESERVATION_REJECT_PATH(reservationId), payload ?? {});
  return unwrapApiData<FamilyVisitReservation>(response.data);
}
