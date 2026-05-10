import apiClient, { unwrapApiData } from './client';
import type {
  CaregiverCheckInRecord,
  CaregiverCheckInSubmitRequest,
  DailyCareTask,
  FamilyServicePlan,
  NurseCareRecord,
} from '../types/care';
import {
  CARE_DELIVERY_FAMILY_CHECK_INS_PATH,
  CARE_DELIVERY_FAMILY_NURSE_CARE_RECORDS_PATH,
  CARE_DELIVERY_FAMILY_SERVICE_PLANS_PATH,
  CARE_DELIVERY_MY_CHECK_INS_PATH,
  CARE_DELIVERY_MY_TASK_CHECK_IN_PATH,
  CARE_DELIVERY_MY_TASKS_PATH,
} from './endpoints';

export async function listMyDailyTasks(taskDate: string, elderId?: number): Promise<DailyCareTask[]> {
  const response = await apiClient.get(CARE_DELIVERY_MY_TASKS_PATH, {
    params: { taskDate, elderId },
  });
  return unwrapApiData<DailyCareTask[]>(response.data);
}

export async function submitDailyTaskCheckIn(
  servicePlanId: number,
  payload: CaregiverCheckInSubmitRequest,
): Promise<CaregiverCheckInRecord> {
  const response = await apiClient.post(CARE_DELIVERY_MY_TASK_CHECK_IN_PATH(servicePlanId), payload);
  return unwrapApiData<CaregiverCheckInRecord>(response.data);
}

export async function listMyCheckIns(elderId?: number, taskDate?: string): Promise<CaregiverCheckInRecord[]> {
  const response = await apiClient.get(CARE_DELIVERY_MY_CHECK_INS_PATH, {
    params: { elderId, taskDate },
  });
  return unwrapApiData<CaregiverCheckInRecord[]>(response.data);
}

export async function listFamilyServicePlans(elderId: number): Promise<FamilyServicePlan[]> {
  const response = await apiClient.get(CARE_DELIVERY_FAMILY_SERVICE_PLANS_PATH(elderId));
  return unwrapApiData<FamilyServicePlan[]>(response.data);
}

export async function listFamilyCheckIns(elderId: number, taskDate?: string): Promise<CaregiverCheckInRecord[]> {
  const response = await apiClient.get(CARE_DELIVERY_FAMILY_CHECK_INS_PATH(elderId), {
    params: { taskDate },
  });
  return unwrapApiData<CaregiverCheckInRecord[]>(response.data);
}

export async function listFamilyNurseCareRecords(elderId: number, recordDate?: string): Promise<NurseCareRecord[]> {
  const response = await apiClient.get(CARE_DELIVERY_FAMILY_NURSE_CARE_RECORDS_PATH(elderId), {
    params: { recordDate },
  });
  return unwrapApiData<NurseCareRecord[]>(response.data);
}
