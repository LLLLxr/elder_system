package org.smart_elder_system.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.smart_elder_system.user.util.IdCardUtil;
import org.smart_elder_system.user.constant.UserConstants;
import org.smart_elder_system.user.dto.*;
import org.smart_elder_system.user.entity.Role;
import org.smart_elder_system.user.entity.User;
import org.smart_elder_system.user.exception.BusinessException;
import org.smart_elder_system.user.repository.UserRepository;
import org.smart_elder_system.user.service.RoleService;
import org.smart_elder_system.user.service.UserService;
import org.smart_elder_system.user.vo.UserVO;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    // ================================= 基础查询方法 =================================
    
    @Override
    public User getUserByUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        return userOptional.filter(user -> UserConstants.DELETE_FLAG_NORMAL.equals(user.getDeleteFlag()))
                .orElse(null);
    }
    
    @Override
    public User getUserByEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        return userOptional.filter(user -> UserConstants.DELETE_FLAG_NORMAL.equals(user.getDeleteFlag()))
                .orElse(null);
    }
    
    @Override
    public User getUserByPhone(String phone) {
        Optional<User> userOptional = userRepository.findByPhone(phone);
        return userOptional.filter(user -> UserConstants.DELETE_FLAG_NORMAL.equals(user.getDeleteFlag()))
                .orElse(null);
    }
    
    @Override
    public User getUserByIdCardNo(String idCardNo) {
        Optional<User> userOptional = userRepository.findByIdCard(idCardNo);
        return userOptional.filter(user -> UserConstants.DELETE_FLAG_NORMAL.equals(user.getDeleteFlag()))
                .orElse(null);
    }
    
    // ================================= CRUD操作方法 =================================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User createUser(UserDTO userDTO) {
        // 检查用户名是否存在
        if (checkUsernameExists(userDTO.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        // 检查邮箱是否存在
        if (StringUtils.hasText(userDTO.getEmail()) && checkEmailExists(userDTO.getEmail())) {
            throw new BusinessException("邮箱已被使用");
        }
        
        // 检查手机号是否存在
        if (StringUtils.hasText(userDTO.getPhone()) && checkPhoneExists(userDTO.getPhone())) {
            throw new BusinessException("手机号已被使用");
        }
        
        // 检查身份证号是否存在
        if (StringUtils.hasText(userDTO.getIdCard()) && checkIdCardNoExists(userDTO.getIdCard())) {
            throw new BusinessException("身份证号已被使用");
        }
        
        // 验证身份证号格式
        if (StringUtils.hasText(userDTO.getIdCard()) && !IdCardUtil.validateIdCard(userDTO.getIdCard())) {
            throw new BusinessException("身份证号格式不正确");
        }
        
        // 创建用户
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setStatus(UserConstants.STATUS_NORMAL);
        user.setIdCardVerified(UserConstants.ID_CARD_NOT_VERIFIED);
        user.setFaceVerified(UserConstants.FACE_NOT_VERIFIED);
        user.setDeleteFlag(UserConstants.DELETE_FLAG_NORMAL);
        
        user = userRepository.save(user);
        
        // 分配默认角色
        Role role = roleService.getByRoleCode(UserConstants.ROLE_USER);
        if (role != null) {
            roleService.assignRoleToUser(user.getId(), role.getId());
        }
        
        return user;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateUser(UserDTO userDTO) {
        // 检查用户是否存在
        User existingUser = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (UserConstants.DELETE_FLAG_DELETED.equals(existingUser.getDeleteFlag())) {
            throw new BusinessException("用户不存在");
        }
        
        // 检查邮箱是否被其他用户使用
        if (StringUtils.hasText(userDTO.getEmail()) && 
                !userDTO.getEmail().equals(existingUser.getEmail()) && 
                checkEmailExists(userDTO.getEmail())) {
            throw new BusinessException("邮箱已被使用");
        }
        
        // 检查手机号是否被其他用户使用
        if (StringUtils.hasText(userDTO.getPhone()) && 
                !userDTO.getPhone().equals(existingUser.getPhone()) && 
                checkPhoneExists(userDTO.getPhone())) {
            throw new BusinessException("手机号已被使用");
        }
        
        // 检查身份证号是否被其他用户使用
        if (StringUtils.hasText(userDTO.getIdCard()) && 
                !userDTO.getIdCard().equals(existingUser.getIdCard()) && 
                checkIdCardNoExists(userDTO.getIdCard())) {
            throw new BusinessException("身份证号已被使用");
        }
        
        // 验证身份证号格式
        if (StringUtils.hasText(userDTO.getIdCard()) && !IdCardUtil.validateIdCard(userDTO.getIdCard())) {
            throw new BusinessException("身份证号格式不正确");
        }
        
        // 更新用户信息
        BeanUtils.copyProperties(userDTO, existingUser);
        
        // 不允许修改的字段
        existingUser.setUsername(null);
        existingUser.setPassword(null);
        existingUser.setIdCardVerified(null);
        existingUser.setFaceVerified(null);
        existingUser.setDeleteFlag(null);
        
        return userRepository.save(existingUser);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        // 检查用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (UserConstants.DELETE_FLAG_DELETED.equals(user.getDeleteFlag())) {
            throw new BusinessException("用户不存在");
        }
        
        // 逻辑删除用户
        user.setDeleteFlag(UserConstants.DELETE_FLAG_DELETED);
        userRepository.save(user);
        return true;
    }
    
    @Override
    public Page<UserVO> getUserPage(Pageable pageable, UserDTO userDTO) {
        Page<User> userPage;
        
        // 根据查询条件构建查询
        if ((userDTO.getUsername() != null && StringUtils.hasText(userDTO.getUsername())) ||
            (userDTO.getRealName() != null && StringUtils.hasText(userDTO.getRealName())) ||
            (userDTO.getPhone() != null && StringUtils.hasText(userDTO.getPhone())) ||
            userDTO.getStatus() != null) {

            // 使用Repository中的查询方法
            String keyword = "";
            if (StringUtils.hasText(userDTO.getUsername())) {
                keyword = userDTO.getUsername();
            } else if (StringUtils.hasText(userDTO.getRealName())) {
                keyword = userDTO.getRealName();
            } else if (StringUtils.hasText(userDTO.getPhone())) {
                keyword = userDTO.getPhone();
            }
            
            userPage = userRepository.findByKeywordAndDeleteFlag(keyword, UserConstants.DELETE_FLAG_NORMAL, pageable);
        } else {
            userPage = userRepository.findByDeleteFlagOrderByCreateTimeDesc(UserConstants.DELETE_FLAG_NORMAL, pageable);
        }
        
        // 转换为UserVO
        return userPage.map(this::convertToUserVO);
    }
    
    @Override
    public UserVO getUserDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (UserConstants.DELETE_FLAG_DELETED.equals(user.getDeleteFlag())) {
            throw new BusinessException("用户不存在");
        }
        
        return convertToUserVO(user);
    }
    
    // ================================= 状态和密码管理方法 =================================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserStatus(Long userId, Integer status) {
        // 检查用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (UserConstants.DELETE_FLAG_DELETED.equals(user.getDeleteFlag())) {
            throw new BusinessException("用户不存在");
        }
        
        // 更新用户状态
        user.setStatus(status);
        userRepository.save(user);
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        // 检查用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (UserConstants.DELETE_FLAG_DELETED.equals(user.getDeleteFlag())) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证原密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, String newPassword) {
        // 检查用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (UserConstants.DELETE_FLAG_DELETED.equals(user.getDeleteFlag())) {
            throw new BusinessException("用户不存在");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLoginInfo(Long userId, String loginIp) {
        // 检查用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (UserConstants.DELETE_FLAG_DELETED.equals(user.getDeleteFlag())) {
            throw new BusinessException("用户不存在");
        }
        
        // 更新登录信息
        // 这里可以添加登录IP、登录时间等字段
        // user.setLastLoginTime(LocalDateTime.now());
        // user.setLastLoginIp(loginIp);
        userRepository.save(user);
    }
    
    // ================================= 检查方法 =================================
    
    @Override
    public boolean checkUsernameExists(String username) {
        return userRepository.existsByUsernameAndDeleteFlag(username, UserConstants.DELETE_FLAG_NORMAL);
    }
    
    @Override
    public boolean checkEmailExists(String email) {
        return userRepository.existsByEmailAndDeleteFlag(email, UserConstants.DELETE_FLAG_NORMAL);
    }
    
    @Override
    public boolean checkPhoneExists(String phone) {
        return userRepository.existsByPhoneAndDeleteFlag(phone, UserConstants.DELETE_FLAG_NORMAL);
    }
    
    @Override
    public boolean checkIdCardNoExists(String idCardNo) {
        return userRepository.existsByIdCardAndDeleteFlag(idCardNo, UserConstants.DELETE_FLAG_NORMAL);
    }
    
    // ================================= 辅助方法 =================================
    
    @Override
    public UserVO findByUsername(String username) {
        User user = getUserByUsername(username);
        if (user == null || !UserConstants.STATUS_NORMAL.equals(user.getStatus())) {
            return null;
        }
        return convertToUserVO(user);
    }

    @Override
    public UserVO findByPhone(String phone) {
        User user = userRepository.findByPhoneAndStatusAndDeleteFlag(
                phone, UserConstants.STATUS_NORMAL, UserConstants.DELETE_FLAG_NORMAL);
        if (user == null) {
            return null;
        }
        return convertToUserVO(user);
    }

    @Override
    public UserVO findByEmail(String email) {
        User user = userRepository.findByEmailAndStatusAndDeleteFlag(
                email, UserConstants.STATUS_NORMAL, UserConstants.DELETE_FLAG_NORMAL);
        if (user == null) {
            return null;
        }
        return convertToUserVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(RegisterDTO registerDTO) {
        // 检查用户名是否存在
        if (userRepository.existsByUsernameAndDeleteFlag(registerDTO.getUsername(), UserConstants.DELETE_FLAG_NORMAL)) {
            throw new BusinessException("用户名已存在");
        }

        // 检查手机号是否存在
        if (StringUtils.hasText(registerDTO.getPhone()) &&
                userRepository.existsByPhoneAndDeleteFlag(registerDTO.getPhone(), UserConstants.DELETE_FLAG_NORMAL)) {
            throw new BusinessException("手机号已被使用");
        }

        // 检查邮箱是否存在
        if (StringUtils.hasText(registerDTO.getEmail()) &&
                userRepository.existsByEmailAndDeleteFlag(registerDTO.getEmail(), UserConstants.DELETE_FLAG_NORMAL)) {
            throw new BusinessException("邮箱已被使用");
        }

        // 检查身份证号是否存在
        if (StringUtils.hasText(registerDTO.getIdCard()) &&
                userRepository.existsByIdCardAndDeleteFlag(registerDTO.getIdCard(), UserConstants.DELETE_FLAG_NORMAL)) {
            throw new BusinessException("身份证号已被使用");
        }

        // 验证身份证号格式
        if (StringUtils.hasText(registerDTO.getIdCard()) && !IdCardUtil.validateIdCard(registerDTO.getIdCard())) {
            throw new BusinessException("身份证号格式不正确");
        }

        // 创建用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setRealName(registerDTO.getRealName());
        user.setIdCard(registerDTO.getIdCard());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setStatus(UserConstants.STATUS_NORMAL);
        user.setIdCardVerified(UserConstants.ID_CARD_NOT_VERIFIED);
        user.setFaceVerified(UserConstants.FACE_NOT_VERIFIED);
        user.setDeleteFlag(UserConstants.DELETE_FLAG_NORMAL);

        user = userRepository.save(user);

        // 分配默认角色
        Role role = roleService.getByRoleCode(UserConstants.ROLE_USER);
        if (role != null) {
            roleService.assignRoleToUser(user.getId(), role.getId());
        }

        return convertToUserVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changePassword(ChangePasswordDTO changePasswordDTO) {
        // 获取当前用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> userOptional = userRepository.findByUsername(username);
        User currentUser = userOptional.orElseThrow(() -> new BusinessException("用户不存在"));

        // 验证原密码
        if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), currentUser.getPassword())) {
            throw new BusinessException("原密码不正确");
        }

        // 更新密码
        currentUser.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userRepository.save(currentUser);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(ResetPasswordDTO resetPasswordDTO) {
        Optional<User> userOptional = userRepository.findByUsername(resetPasswordDTO.getUsername());
        User user = userOptional.orElseThrow(() -> new BusinessException("用户不存在"));

        user.setPassword(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
        userRepository.save(user);
        return true;
    }

    @Override
    public Page<UserVO> getUserList(Pageable pageable, String keyword) {
        Page<User> userPage;
        if (StringUtils.hasText(keyword)) {
            userPage = userRepository.findByKeywordAndDeleteFlag(
                    keyword, UserConstants.DELETE_FLAG_NORMAL, pageable);
        } else {
            userPage = userRepository.findByDeleteFlagOrderByCreateTimeDesc(
                    UserConstants.DELETE_FLAG_NORMAL, pageable);
        }
        return userPage.map(this::convertToUserVO);
    }

    /**
     * 转换User为UserVO
     */
    private UserVO convertToUserVO(User user) {
        if (user == null) {
            return null;
        }
        
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        
        // 查询用户角色
        List<String> roles = roleService.getRolesByUserId(user.getId());
        userVO.setRoles(roles);
        
        // 不返回敏感信息
        userVO.setPassword(null);
        
        return userVO;
    }
}

