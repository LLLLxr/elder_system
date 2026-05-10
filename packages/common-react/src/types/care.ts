export interface ServiceJourneyResult {
  applicationId?: number;
  agreementId?: number;
  carePlanId?: number;
  healthProfileId?: number;
  healthAssessmentId?: number;
  finalStatus?: string;
  message?: string;
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
  nursingConclusionSection?: string;
  chronicDiseaseSummary?: string;
  allergySummary?: string;
  riskLevel?: string;
  score?: number;
  conclusion?: string;
}

export interface HealthCheckFormCreateRequest {
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
  nursingConclusionSection?: string;
  chronicDiseaseSummary?: string;
  allergySummary?: string;
  riskLevel?: string;
  score?: number;
  conclusion?: string;
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

export interface ServiceApplication {
  applicationId?: number;
  elderId: number;
  guardianId?: number;
  applicantName: string;
  contactPhone: string;
  serviceScene: string;
  serviceRequest: string;
  status?: string;
  submittedAt?: string;
  assessedAt?: string;
}

export interface EligibilityAssessmentRequest {
  applicationId: number;
  eligible: boolean;
  assessmentConclusion: string;
  assessor: string;
}

export interface HealthAssessmentRequest {
  applicationId: number;
  elderId: number;
  agreementId: number;
  applicantName?: string;
  serviceScene?: string;
  assessmentStatus?: string;
  assessmentConclusion?: string;
  score?: number;
  submittedAt?: string;
  needsAssessedAt?: string;
  healthAssessedAt?: string;
}

export interface ServiceAgreement {
  agreementId?: number;
  applicationId?: number;
  elderId?: number;
  serviceScene?: string;
  status?: string;
  effectiveDate?: string;
  expiryDate?: string;
  signedBy?: string;
}

export interface HealthAssessmentSubmitRequest {
  applicationId: number;
  passed: boolean;
  assessmentConclusion: string;
  assessor: string;
  responsibleDoctor: string;
  score: number;
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

export interface FamilyVisitSlot {
  slotId?: number;
  slotDate?: string;
  startTime?: string;
  endTime?: string;
  capacity?: number;
  reservedCount?: number;
  status?: string;
}

export interface FamilyVisitReservation {
  reservationId?: number;
  slotId: number;
  elderId: number;
  familyUserId?: number;
  familyUsername?: string;
  visitorName: string;
  visitorPhone: string;
  relationToElder: string;
  visitPurpose: string;
  status?: string;
  reviewedBy?: string;
  reviewComment?: string;
  reviewedAt?: string;
  slotDate?: string;
  startTime?: string;
  endTime?: string;
  createdAt?: string;
}

export interface FamilyVisitReservationReviewPayload {
  reviewComment?: string;
}

export interface NurseCareRecord {
  recordId?: number;
  elderId?: number;
  elderName?: string;
  nurseId?: number;
  nurseName?: string;
  servicePlanId?: number;
  recordDate?: string;
  recordFormData?: Record<string, unknown>;
  createdAt?: string;
  updatedAt?: string;
}

export interface DoctorRoundRecord {
  roundRecordId?: number;
  elderId?: number;
  elderName?: string;
  doctorId?: number;
  doctorName?: string;
  content?: string;
  riskFlag?: boolean;
  roundTime?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface NurseCareRecordSaveRequest {
  elderId: number;
  servicePlanId?: number;
  recordDate: string;
  recordFormData: Record<string, unknown>;
}

export interface DoctorRoundRecordSaveRequest {
  elderId: number;
  content: string;
  riskFlag: boolean;
  roundTime: string;
}

export interface CaregiverQualificationApplication {
  applicationId?: number;
  caregiverUserId?: number;
  caregiverUsername?: string;
  realName: string;
  phone: string;
  idCardNo: string;
  certificateNo: string;
  certificateType: string;
  yearsOfExperience: number;
  skillSummary: string;
  status?: string;
  reviewedBy?: string;
  reviewComment?: string;
  reviewedAt?: string;
  createdAt?: string;
}

export interface CaregiverQualificationReviewPayload {
  reviewComment?: string;
}
