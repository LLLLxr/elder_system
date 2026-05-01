export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginUserInfo {
  id?: number;
  username?: string;
  realName?: string;
  nickname?: string;
  email?: string;
  avatar?: string;
  userType?: number;
  userTypeLabel?: string;
  idCardVerified?: number;
  faceVerified?: number;
}

export interface LoginResponse {
  token: string;
  tokenType?: string;
  expiresIn?: number;
  userInfo?: LoginUserInfo;
  permissions?: string[];
  roles?: string[];
}
