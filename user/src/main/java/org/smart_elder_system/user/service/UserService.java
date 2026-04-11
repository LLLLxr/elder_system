package org.smart_elder_system.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.smart_elder_system.user.dto.ChangePasswordDTO;
import org.smart_elder_system.user.dto.RegisterDTO;
import org.smart_elder_system.user.dto.ResetPasswordDTO;
import org.smart_elder_system.user.dto.UserDTO;
import org.smart_elder_system.user.entity.User;
import org.smart_elder_system.user.vo.UserVO;

public interface UserService {
    
    /**
     * 根据用户名查询用户
     */
    User getUserByUsername(String username);
    
    /**
     * 根据邮箱查询用户
     */
    User getUserByEmail(String email);
    
    /**
     * 根据手机号查询用户
     */
    User getUserByPhone(String phone);
    
    /**
     * 根据身份证号查询用户
     */
    User getUserByIdCardNo(String idCardNo);
    
    /**
     * 创建用户
     */
    User createUser(UserDTO userDTO);
    
    /**
     * 更新用户
     */
    User updateUser(UserDTO userDTO);
    
    /**
     * 删除用户
     */
    boolean deleteUser(Long userId);
    
    /**
     * 分页查询用户列表
     */
    Page<UserVO> getUserPage(Pageable pageable, UserDTO userDTO);
    
    /**
     * 获取用户详情
     */
    UserVO getUserDetail(Long userId);
    
    /**
     * 更新用户状态
     */
    boolean updateUserStatus(Long userId, Integer status);
    
    /**
     * 更新用户密码
     */
    void updatePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 重置用户密码
     */
    void resetPassword(Long userId, String newPassword);
    
    /**
     * 更新用户登录信息
     */
    void updateLoginInfo(Long userId, String loginIp);
    
    /**
     * 检查用户名是否已存在
     */
    boolean checkUsernameExists(String username);
    
    /**
     * 检查邮箱是否已存在
     */
    boolean checkEmailExists(String email);
    
    /**
     * 检查手机号是否已存在
     */
    boolean checkPhoneExists(String phone);
    
    /**
     * 检查身份证号是否已存在
     */
    boolean checkIdCardNoExists(String idCardNo);
    
    /**
     * 根据用户名查找用户VO
     *
     * @param username 用户名
     * @return 用户VO
     */
    UserVO findByUsername(String username);
    
    /**
     * 根据手机号查找用户VO
     *
     * @param phone 手机号
     * @return 用户VO
     */
    UserVO findByPhone(String phone);
    
    /**
     * 根据邮箱查找用户VO
     *
     * @param email 邮箱
     * @return 用户VO
     */
    UserVO findByEmail(String email);
    
    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 用户VO
     */
    UserVO register(RegisterDTO registerDTO);
    
    /**
     * 修改密码
     *
     * @param changePasswordDTO 修改密码信息
     * @return 是否成功
     */
    boolean changePassword(ChangePasswordDTO changePasswordDTO);
    
    /**
     * 重置密码
     *
     * @param resetPasswordDTO 重置密码信息
     * @return 是否成功
     */
    boolean resetPassword(ResetPasswordDTO resetPasswordDTO);
    
    /**
     * 获取用户列表
     *
     * @param pageable 分页信息
     * @param keyword 关键词
     * @return 用户VO分页列表
     */
    Page<UserVO> getUserList(Pageable pageable, String keyword);
}