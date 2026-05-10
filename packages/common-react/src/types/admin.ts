export interface ElderBindingItem {
  bindingId?: number;
  userId?: number;
  elderId?: number;
  elderName?: string;
  elderIdCard?: string;
  elderPhone?: string;
  gender?: string;
  birthDate?: string;
  elderStatus?: string;
  bindingType?: string;
  relationToElder?: string;
  createdAt?: string;
}

export interface ElderBindingRequestItem {
  requestId?: number;
  applicantUserId?: number;
  elderId?: number;
  elderName: string;
  elderIdCard: string;
  elderPhone?: string;
  bindingType?: string;
  relationToElder?: string;
  status?: string;
  reviewedBy?: string;
  reviewComment?: string;
  reviewedAt?: string;
  createdAt?: string;
}

export interface FamilyElderBindingRequestCreatePayload {
  elderName: string;
  elderIdCard: string;
  elderPhone?: string;
  relationToElder: string;
}

export interface ElderBindingReviewPayload {
  reviewComment?: string;
}

export interface UserItem {
  id: number;
  username: string;
  realName?: string;
  email?: string;
  phone?: string;
  idCardNo?: string;
  idCardVerified?: number;
  status?: number;
  statusLabel?: string;
  userTypeLabel?: string;
  createTime?: string;
  roles?: string[];
  elderBindings?: ElderBindingItem[];
}

export interface UserPageResponse {
  content: UserItem[];
  totalElements: number;
  size: number;
  number: number;
}

export interface UserQueryParams {
  current: number;
  size: number;
  username?: string;
  realName?: string;
  phone?: string;
  status?: number;
}

export interface UserCreatePayload {
  username: string;
  password: string;
  realName?: string;
  email?: string;
  phone?: string;
  idCard?: string;
}

export interface UserUpdatePayload {
  realName?: string;
  email?: string;
  phone?: string;
  idCard?: string;
}

export interface RoleItem {
  id: number;
  roleName: string;
  roleCode: string;
  description?: string;
  status?: number;
  permissions?: PermissionItem[];
}

export interface PermissionItem {
  id: number;
  permissionName: string;
  permissionCode: string;
  description?: string;
  status?: number;
}

export interface PermissionCreatePayload {
  permissionName: string;
  permissionCode: string;
  description?: string;
  status?: number;
}

export interface PermissionUpdatePayload {
  permissionName?: string;
  permissionCode?: string;
  description?: string;
  status?: number;
}
