export interface ServiceJourneyResult {
  applicationId?: number;
  elderId?: number;
  agreementId?: number;
  carePlanId?: number;
  healthProfileId?: number;
  healthAssessmentId?: number;
  finalStatus?: string;
  message?: string;
}

export interface ContinueJourneyRequest {
  applicationId: number;
}

export interface HealthCheckForm {
  formId?: number;
  elderId: number;
  agreementId: number;
  elderName: string;
  formCode?: string;
  checkDate?: string;
  responsibleDoctor?: string;
  formVersion?: string;
  symptomSection?: string;
  vitalSignSection?: string;
  selfEvaluationSection?: string;
  cognitiveEmotionSection?: string;
  lifestyleSection?: string;
  chronicDiseaseSummary?: string;
  allergySummary?: string;
}

export interface StartServiceJourneyRequest {
  elderId: number;
  guardianId?: number;
  applicantName: string;
  contactPhone: string;
  serviceScene: string;
  serviceRequest: string;
}

export interface ReviewAndFinalizeRequest {
  agreementId: number;
  elderId: number;
  satisfactionScore: number;
  reviewComment?: string;
}

export interface IntakeRecord {
  applicationId?: number;
  elderId?: number;
  applicantName?: string;
  submittedAt?: string;
  admissionStatus?: string;
  journeyStatus?: string;
  message?: string;
}

export interface StagePoint {
  name: string;
  value: number;
}

export interface PagedResult<T> {
  items: T[];
  total: number;
  page: number;
  size: number;
}

export interface JourneyTaskItem {
  taskId?: number;
  applicationId?: number;
  agreementId?: number;
  elderId?: number;
  taskType?: string;
  currentState?: string;
  assigneeRole?: string;
  status?: string;
  dueAt?: string;
  completedAt?: string;
  createdAt?: string;
}

export interface JourneyTaskOverview {
  pendingCount: number;
  overdueCount: number;
  completedCount: number;
  cancelledCount: number;
  taskTypeDistribution: StagePoint[];
  statusDistribution: StagePoint[];
}

export interface JourneyTransitionLogItem {
  logId?: number;
  applicationId?: number;
  agreementId?: number;
  elderId?: number;
  fromState?: string;
  journeyEvent?: string;
  toState?: string;
  reason?: string;
  requestSnapshot?: string;
  transitionTime?: string;
  createdBy?: string;
}

export interface ListJourneyTasksParams {
  applicationId?: number;
  elderId?: number;
  agreementId?: number;
  taskType?: string;
  statuses?: string[];
  assigneeRole?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}

export interface JourneyTaskOverviewParams {
  applicationId?: number;
  elderId?: number;
  agreementId?: number;
  taskType?: string;
  statuses?: string[];
  assigneeRole?: string;
}

export interface ReturnJourneyStepRequest {
  applicationId: number;
  targetState: string;
  reason?: string;
}
