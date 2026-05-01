import apiClient, { unwrapApiData } from './client';
import type {
  JourneyTaskItem,
  JourneyTaskOverview,
  JourneyTaskOverviewParams,
  JourneyTransitionLogItem,
  ListJourneyTasksParams,
  PagedResult,
  ReturnJourneyStepRequest,
  ReviewAndFinalizeRequest,
  ServiceJourneyResult,
  StartServiceJourneyRequest,
} from '../types/care';
import {
  CARE_JOURNEY_OVERVIEW_PATH,
  CARE_JOURNEY_REVIEW_PATH,
  CARE_JOURNEY_RETURN_PATH,
  CARE_JOURNEY_START_PATH,
  CARE_JOURNEY_TASK_OVERVIEW_PATH,
  CARE_JOURNEY_TASK_TIMELINE_PATH,
  CARE_JOURNEY_TASKS_PATH,
  CARE_JOURNEY_TRANSITION_LOGS_BY_AGREEMENT_PATH,
  CARE_JOURNEY_TRANSITION_LOGS_BY_APPLICATION_PATH,
} from './endpoints';
import { buildPostUrl } from '../utils/postWithQueryParams';

export async function getJourneyOverview(): Promise<string> {
  const response = await apiClient.get(CARE_JOURNEY_OVERVIEW_PATH);
  return unwrapApiData<string>(response.data);
}

export async function startServiceJourney(
  request: StartServiceJourneyRequest,
): Promise<ServiceJourneyResult> {
  const url = buildPostUrl(CARE_JOURNEY_START_PATH, {
    elderId: request.elderId,
    guardianId: request.guardianId,
    applicantName: request.applicantName,
    contactPhone: request.contactPhone,
    serviceScene: request.serviceScene,
    serviceRequest: request.serviceRequest,
  });

  const response = await apiClient.post(url, null);
  return unwrapApiData<ServiceJourneyResult>(response.data);
}

export async function reviewAndFinalize(
  request: ReviewAndFinalizeRequest,
): Promise<ServiceJourneyResult> {
  const url = buildPostUrl(CARE_JOURNEY_REVIEW_PATH, {
    agreementId: request.agreementId,
    elderId: request.elderId,
    satisfactionScore: request.satisfactionScore,
    reviewComment: request.reviewComment,
  });

  const response = await apiClient.post(url, null);
  return unwrapApiData<ServiceJourneyResult>(response.data);
}

type JourneyTaskPageResponse = {
  content?: JourneyTaskItem[];
  totalElements?: number;
  size?: number;
  number?: number;
};

function toJourneyTaskPagedResult(payload: JourneyTaskPageResponse | JourneyTaskItem[]): PagedResult<JourneyTaskItem> {
  if (Array.isArray(payload)) {
    return {
      items: payload,
      total: payload.length,
      page: 0,
      size: payload.length,
    };
  }

  return {
    items: payload.content ?? [],
    total: payload.totalElements ?? 0,
    page: payload.number ?? 0,
    size: payload.size ?? 0,
  };
}

function joinStatuses(statuses?: string[]): string[] | undefined {
  if (!statuses?.length) {
    return undefined;
  }
  return statuses;
}

export async function listJourneyTasks(params: ListJourneyTasksParams = {}): Promise<PagedResult<JourneyTaskItem>> {
  const response = await apiClient.get<JourneyTaskPageResponse | JourneyTaskItem[]>(CARE_JOURNEY_TASKS_PATH, {
    params: {
      applicationId: params.applicationId,
      elderId: params.elderId,
      agreementId: params.agreementId,
      taskType: params.taskType,
      statuses: joinStatuses(params.statuses ?? ['PENDING', 'OVERDUE']),
      assigneeRole: params.assigneeRole,
      page: params.page ?? 0,
      size: params.size ?? 20,
      sortBy: params.sortBy ?? 'dueAt',
      sortOrder: params.sortOrder ?? 'asc',
    },
  });
  return toJourneyTaskPagedResult(unwrapApiData<JourneyTaskPageResponse | JourneyTaskItem[]>(response.data));
}

export async function getJourneyTaskOverview(
  params: JourneyTaskOverviewParams = {},
): Promise<JourneyTaskOverview> {
  const response = await apiClient.get<JourneyTaskOverview>(CARE_JOURNEY_TASK_OVERVIEW_PATH, {
    params: {
      applicationId: params.applicationId,
      elderId: params.elderId,
      agreementId: params.agreementId,
      taskType: params.taskType,
      statuses: joinStatuses(params.statuses ?? ['PENDING', 'OVERDUE']),
      assigneeRole: params.assigneeRole,
    },
  });
  return unwrapApiData<JourneyTaskOverview>(response.data);
}

export async function listJourneyTaskTimeline(applicationId: number): Promise<JourneyTaskItem[]> {
  const response = await apiClient.get<JourneyTaskItem[]>(CARE_JOURNEY_TASK_TIMELINE_PATH, {
    params: { applicationId },
  });
  return unwrapApiData<JourneyTaskItem[]>(response.data);
}

export async function listJourneyTransitionLogsByApplication(
  applicationId: number,
): Promise<JourneyTransitionLogItem[]> {
  const response = await apiClient.get<JourneyTransitionLogItem[]>(CARE_JOURNEY_TRANSITION_LOGS_BY_APPLICATION_PATH, {
    params: { applicationId },
  });
  return unwrapApiData<JourneyTransitionLogItem[]>(response.data);
}

export async function listJourneyTransitionLogsByAgreement(
  agreementId: number,
): Promise<JourneyTransitionLogItem[]> {
  const response = await apiClient.get<JourneyTransitionLogItem[]>(CARE_JOURNEY_TRANSITION_LOGS_BY_AGREEMENT_PATH, {
    params: { agreementId },
  });
  return unwrapApiData<JourneyTransitionLogItem[]>(response.data);
}

export async function returnJourneyStep(
  request: ReturnJourneyStepRequest,
): Promise<ServiceJourneyResult> {
  const url = buildPostUrl(CARE_JOURNEY_RETURN_PATH, {
    applicationId: request.applicationId,
    targetState: request.targetState,
    reason: request.reason,
  });

  const response = await apiClient.post(url, null);
  return unwrapApiData<ServiceJourneyResult>(response.data);
}
