export const AUTH_LOGIN_PATH = '/auth/login';
export const AUTH_LOGOUT_PATH = '/auth/logout';

export const USER_LIST_PATH = '/api/users';
export const USER_ME_PATH = '/api/users/me';
export const USER_ANALYTICS_OVERVIEW_PATH = '/api/users/analytics/overview';
export const USER_DETAIL_PATH = (userId: number) => `/api/users/${userId}`;
export const USER_STATUS_PATH = (userId: number) => `/api/users/${userId}/status`;
export const USER_RESET_PASSWORD_PATH = (userId: number) => `/api/users/${userId}/password/reset`;

export const ROLE_LIST_PATH = '/api/roles';
export const ROLE_WITH_PERMISSIONS_PATH = '/api/roles/with-permissions';
export const ROLE_USER_PATH = (userId: number) => `/api/roles/user/${userId}`;
export const ROLE_ASSIGN_USER_PATH = (userId: number, roleId: number) => `/api/roles/user/${userId}/assign/${roleId}`;
export const ROLE_REMOVE_USER_PATH = (userId: number, roleId: number) => `/api/roles/user/${userId}/remove/${roleId}`;
export const ROLE_ASSIGN_PERMISSION_PATH = (roleId: number, permissionId: number) => `/api/roles/${roleId}/permission/${permissionId}`;
export const ROLE_REMOVE_PERMISSION_PATH = (roleId: number, permissionId: number) => `/api/roles/${roleId}/permission/${permissionId}`;

export const PERMISSION_LIST_PATH = '/api/permissions';
export const PERMISSION_TREE_PATH = '/api/permissions/tree';
export const PERMISSION_BY_ROLE_PATH = (roleId: number) => `/api/permissions/role/${roleId}`;
export const PERMISSION_DETAIL_PATH = (permissionId: number) => `/api/permissions/${permissionId}`;
export const PERMISSION_STATUS_PATH = (permissionId: number) => `/api/permissions/${permissionId}/status`;

export const CARE_JOURNEY_OVERVIEW_PATH = '/care-orchestration/journey-overview';
export const CARE_JOURNEY_START_PATH = '/care-orchestration/journeys/start';
export const CARE_JOURNEY_CONTINUE_PATH = '/care-orchestration/journeys/continue';
export const CARE_JOURNEY_REVIEW_PATH = '/care-orchestration/journeys/review';
export const CARE_JOURNEY_RETURN_PATH = '/care-orchestration/journeys/return';
export const CARE_JOURNEY_INTAKE_RECORDS_PATH = '/care-orchestration/journeys/intake-records';
export const CARE_JOURNEY_INTAKE_RECORDS_BY_APPLICANT_PATH = '/care-orchestration/journeys/intake-records/by-applicant';
export const CARE_JOURNEY_LATEST_RESULT_BY_APPLICANT_PATH = '/care-orchestration/journeys/latest-result/by-applicant';
export const CARE_JOURNEY_TASKS_PATH = '/care-orchestration/journey-tasks';
export const CARE_JOURNEY_TASK_TIMELINE_PATH = '/care-orchestration/journey-tasks/timeline';
export const CARE_JOURNEY_TASK_OVERVIEW_PATH = '/care-orchestration/journey-tasks/overview';
export const CARE_JOURNEY_TRANSITION_LOGS_BY_APPLICATION_PATH = '/care-orchestration/journey-transition-logs/by-application';
export const CARE_JOURNEY_TRANSITION_LOGS_BY_AGREEMENT_PATH = '/care-orchestration/journey-transition-logs/by-agreement';

export const ADMISSION_SCOPE_PATH = '/admission/module-scope';
export const ADMISSION_APPLICATIONS_PATH = '/admission/applications';
export const ADMISSION_ASSESSMENTS_PATH = '/admission/assessments';
export const CONTRACT_SCOPE_PATH = '/contract/module-scope';
export const HEALTH_SCOPE_PATH = '/health/module-scope';
export const HEALTH_CHECK_FORMS_PATH = '/health/check-forms';
export const HEALTH_CHECK_FORM_DETAIL_PATH = (formId: number) => `/health/check-forms/${formId}`;
export const HEALTH_CHECK_FORM_LATEST_PATH = '/health/check-forms/latest';
export const CARE_DELIVERY_SCOPE_PATH = '/care-delivery/module-scope';
export const QUALITY_SCOPE_PATH = '/quality/module-scope';
export const BILLING_SCOPE_PATH = '/billing/module-scope';
export const RESOURCE_SCHEDULING_SCOPE_PATH = '/resource-scheduling/module-scope';
export const SAFETY_EMERGENCY_SCOPE_PATH = '/safety-emergency/module-scope';
export const OPS_BUSINESS_INFO_PATH = '/business/info';
export const CARE_ANALYTICS_OVERVIEW_PATH = '/care-orchestration/analytics/overview';
export const CARE_ANALYTICS_TRENDS_PATH = '/care-orchestration/analytics/trends';
