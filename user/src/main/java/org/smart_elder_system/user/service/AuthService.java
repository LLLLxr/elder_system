package org.smart_elder_system.user.service;

import org.smart_elder_system.user.dto.FaceVerifyDTO;
import org.smart_elder_system.user.dto.IdCardVerifyDTO;
import org.smart_elder_system.user.dto.LoginDTO;
import org.smart_elder_system.user.vo.LoginVO;
import org.smart_elder_system.user.vo.VerifyResultVO;

public interface AuthService {
    
    /**
     * 用户登录
     */
    LoginVO login(LoginDTO loginDTO);
    
    /**
     * 用户退出
     */
    void logout(String token);
    
    /**
     * 刷新令牌
     */
    LoginVO refreshToken(String refreshToken);
    
    /**
     * 身份证验证
     */
    VerifyResultVO verifyIdCard(IdCardVerifyDTO verifyDTO);
    
    /**
     * 人脸验证
     */
    VerifyResultVO verifyFace(FaceVerifyDTO verifyDTO);
    
    /**
     * 检查用户权限
     */
    boolean hasPermission(String permission);
    
    /**
     * 获取用户权限列表
     */
    String[] getUserPermissions();
    
    /**
     * 获取用户角色列表
     */
    String[] getUserRoles();
}