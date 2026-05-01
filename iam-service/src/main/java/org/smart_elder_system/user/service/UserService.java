package org.smart_elder_system.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.smart_elder_system.user.constant.UserConstants;
import org.smart_elder_system.user.dto.ChangePasswordDTO;
import org.smart_elder_system.user.dto.RegisterDTO;
import org.smart_elder_system.user.dto.ResetPasswordDTO;
import org.smart_elder_system.user.dto.UserAnalyticsOverviewDTO;
import org.smart_elder_system.user.dto.UserDTO;
import org.smart_elder_system.user.exception.BusinessException;
import org.smart_elder_system.user.po.RolePo;
import org.smart_elder_system.user.po.UserPo;
import org.smart_elder_system.user.repository.UserRepository;
import org.smart_elder_system.user.util.IdCardUtil;
import org.smart_elder_system.user.vo.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    public UserPo getUserByUsername(String username) {
        Optional<UserPo> userOptional = userRepository.findByUsername(username);
        return userOptional.filter(user -> UserConstants.DELETE_FLAG_NORMAL.equals(user.getDeleteFlag()))
                .orElse(null);
    }

    public UserPo getUserByEmail(String email) {
        Optional<UserPo> userOptional = userRepository.findByEmail(email);
        return userOptional.filter(user -> UserConstants.DELETE_FLAG_NORMAL.equals(user.getDeleteFlag()))
                .orElse(null);
    }

    public UserPo getUserByPhone(String phone) {
        Optional<UserPo> userOptional = userRepository.findByPhone(phone);
        return userOptional.filter(user -> UserConstants.DELETE_FLAG_NORMAL.equals(user.getDeleteFlag()))
                .orElse(null);
    }

    public UserPo getUserByIdCardNo(String idCardNo) {
        Optional<UserPo> userOptional = userRepository.findByIdCard(idCardNo);
        return userOptional.filter(user -> UserConstants.DELETE_FLAG_NORMAL.equals(user.getDeleteFlag()))
                .orElse(null);
    }

    @Transactional(rollbackFor = Exception.class)
    public UserPo createUser(UserDTO userDTO) {
        if (checkUsernameExists(userDTO.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        if (StringUtils.hasText(userDTO.getEmail()) && checkEmailExists(userDTO.getEmail())) {
            throw new BusinessException("邮箱已被使用");
        }
        if (StringUtils.hasText(userDTO.getPhone()) && checkPhoneExists(userDTO.getPhone())) {
            throw new BusinessException("手机号已被使用");
        }
        if (StringUtils.hasText(userDTO.getIdCard()) && checkIdCardNoExists(userDTO.getIdCard())) {
            throw new BusinessException("身份证号已被使用");
        }
        if (StringUtils.hasText(userDTO.getIdCard()) && !IdCardUtil.validateIdCard(userDTO.getIdCard())) {
            throw new BusinessException("身份证号格式不正确");
        }

        UserPo user = new UserPo();
        user.setUsername(userDTO.getUsername());
        user.setRealName(userDTO.getRealName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setAvatar(userDTO.getAvatar());
        user.setIdCard(userDTO.getIdCard());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setStatus(UserConstants.STATUS_NORMAL);
        user.setIdCardVerified(UserConstants.ID_CARD_NOT_VERIFIED);
        user.setFaceVerified(UserConstants.FACE_NOT_VERIFIED);
        user.setDeleteFlag(UserConstants.DELETE_FLAG_NORMAL);

        user = userRepository.save(user);

        RolePo role = roleService.getByRoleCode(UserConstants.ROLE_USER);
        if (role != null) {
            roleService.assignRoleToUser(user.getId(), role.getId());
        }
        return user;
    }

    @Transactional(rollbackFor = Exception.class)
    public UserPo updateUser(UserDTO userDTO) {
        UserPo existingUser = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (UserConstants.DELETE_FLAG_DELETED.equals(existingUser.getDeleteFlag())) {
            throw new BusinessException("用户不存在");
        }
        if (StringUtils.hasText(userDTO.getEmail())
                && !userDTO.getEmail().equals(existingUser.getEmail())
                && checkEmailExists(userDTO.getEmail())) {
            throw new BusinessException("邮箱已被使用");
        }
        if (StringUtils.hasText(userDTO.getPhone())
                && !userDTO.getPhone().equals(existingUser.getPhone())
                && checkPhoneExists(userDTO.getPhone())) {
            throw new BusinessException("手机号已被使用");
        }
        if (StringUtils.hasText(userDTO.getIdCard())
                && !userDTO.getIdCard().equals(existingUser.getIdCard())
                && checkIdCardNoExists(userDTO.getIdCard())) {
            throw new BusinessException("身份证号已被使用");
        }
        if (StringUtils.hasText(userDTO.getIdCard()) && !IdCardUtil.validateIdCard(userDTO.getIdCard())) {
            throw new BusinessException("身份证号格式不正确");
        }

        existingUser.setRealName(userDTO.getRealName());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setPhone(userDTO.getPhone());
        existingUser.setAvatar(userDTO.getAvatar());
        existingUser.setIdCard(userDTO.getIdCard());
        if (userDTO.getStatus() != null) {
            existingUser.setStatus(userDTO.getStatus());
        }
        return userRepository.save(existingUser);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        UserPo user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (UserConstants.DELETE_FLAG_DELETED.equals(user.getDeleteFlag())) {
            throw new BusinessException("用户不存在");
        }
        user.setDeleteFlag(UserConstants.DELETE_FLAG_DELETED);
        userRepository.save(user);
        return true;
    }

    public Page<User> getUserPage(Pageable pageable, UserDTO userDTO) {
        Page<UserPo> userPage;
        if ((userDTO.getUsername() != null && StringUtils.hasText(userDTO.getUsername()))
                || (userDTO.getRealName() != null && StringUtils.hasText(userDTO.getRealName()))
                || (userDTO.getPhone() != null && StringUtils.hasText(userDTO.getPhone()))
                || userDTO.getStatus() != null) {
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
            userPage = userRepository.findByDeleteFlagOrderByCreatedDateTimeUtcDesc(UserConstants.DELETE_FLAG_NORMAL, pageable);
        }
        return userPage.map(this::convertToUserVO);
    }

    public User getUserDetail(Long userId) {
        UserPo user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (UserConstants.DELETE_FLAG_DELETED.equals(user.getDeleteFlag())) {
            throw new BusinessException("用户不存在");
        }
        return convertToUserVO(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserStatus(Long userId, Integer status) {
        UserPo user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (UserConstants.DELETE_FLAG_DELETED.equals(user.getDeleteFlag())) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        userRepository.save(user);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        UserPo user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (UserConstants.DELETE_FLAG_DELETED.equals(user.getDeleteFlag())) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, String newPassword) {
        UserPo user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (UserConstants.DELETE_FLAG_DELETED.equals(user.getDeleteFlag())) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateLoginInfo(Long userId, String loginIp) {
        UserPo user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (UserConstants.DELETE_FLAG_DELETED.equals(user.getDeleteFlag())) {
            throw new BusinessException("用户不存在");
        }
        userRepository.save(user);
    }

    public boolean checkUsernameExists(String username) {
        return userRepository.existsByUsernameAndDeleteFlag(username, UserConstants.DELETE_FLAG_NORMAL);
    }

    public boolean checkEmailExists(String email) {
        return userRepository.existsByEmailAndDeleteFlag(email, UserConstants.DELETE_FLAG_NORMAL);
    }

    public boolean checkPhoneExists(String phone) {
        return userRepository.existsByPhoneAndDeleteFlag(phone, UserConstants.DELETE_FLAG_NORMAL);
    }

    public boolean checkIdCardNoExists(String idCardNo) {
        return userRepository.existsByIdCardAndDeleteFlag(idCardNo, UserConstants.DELETE_FLAG_NORMAL);
    }

    public User findByUsername(String username) {
        UserPo user = getUserByUsername(username);
        if (user == null || !UserConstants.STATUS_NORMAL.equals(user.getStatus())) {
            return null;
        }
        return convertToUserVO(user);
    }

    public User findByPhone(String phone) {
        UserPo user = userRepository.findByPhoneAndStatusAndDeleteFlag(
                phone, UserConstants.STATUS_NORMAL, UserConstants.DELETE_FLAG_NORMAL);
        if (user == null) {
            return null;
        }
        return convertToUserVO(user);
    }

    public User findByEmail(String email) {
        UserPo user = userRepository.findByEmailAndStatusAndDeleteFlag(
                email, UserConstants.STATUS_NORMAL, UserConstants.DELETE_FLAG_NORMAL);
        if (user == null) {
            return null;
        }
        return convertToUserVO(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public User register(RegisterDTO registerDTO) {
        if (userRepository.existsByUsernameAndDeleteFlag(registerDTO.getUsername(), UserConstants.DELETE_FLAG_NORMAL)) {
            throw new BusinessException("用户名已存在");
        }
        if (StringUtils.hasText(registerDTO.getPhone())
                && userRepository.existsByPhoneAndDeleteFlag(registerDTO.getPhone(), UserConstants.DELETE_FLAG_NORMAL)) {
            throw new BusinessException("手机号已被使用");
        }
        if (StringUtils.hasText(registerDTO.getEmail())
                && userRepository.existsByEmailAndDeleteFlag(registerDTO.getEmail(), UserConstants.DELETE_FLAG_NORMAL)) {
            throw new BusinessException("邮箱已被使用");
        }
        if (StringUtils.hasText(registerDTO.getIdCard())
                && userRepository.existsByIdCardAndDeleteFlag(registerDTO.getIdCard(), UserConstants.DELETE_FLAG_NORMAL)) {
            throw new BusinessException("身份证号已被使用");
        }
        if (StringUtils.hasText(registerDTO.getIdCard()) && !IdCardUtil.validateIdCard(registerDTO.getIdCard())) {
            throw new BusinessException("身份证号格式不正确");
        }

        UserPo user = new UserPo();
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

        RolePo role = roleService.getByRoleCode(UserConstants.ROLE_USER);
        if (role != null) {
            roleService.assignRoleToUser(user.getId(), role.getId());
        }
        return convertToUserVO(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean changePassword(ChangePasswordDTO changePasswordDTO) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<UserPo> userOptional = userRepository.findByUsername(username);
        UserPo currentUser = userOptional.orElseThrow(() -> new BusinessException("用户不存在"));
        if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), currentUser.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        currentUser.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userRepository.save(currentUser);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(ResetPasswordDTO resetPasswordDTO) {
        Optional<UserPo> userOptional = userRepository.findByUsername(resetPasswordDTO.getUsername());
        UserPo user = userOptional.orElseThrow(() -> new BusinessException("用户不存在"));
        user.setPassword(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
        userRepository.save(user);
        return true;
    }

    public Page<User> getUserList(Pageable pageable, String keyword) {
        Page<UserPo> userPage;
        if (StringUtils.hasText(keyword)) {
            userPage = userRepository.findByKeywordAndDeleteFlag(
                    keyword, UserConstants.DELETE_FLAG_NORMAL, pageable);
        } else {
            userPage = userRepository.findByDeleteFlagOrderByCreatedDateTimeUtcDesc(
                    UserConstants.DELETE_FLAG_NORMAL, pageable);
        }
        return userPage.map(this::convertToUserVO);
    }

    public UserAnalyticsOverviewDTO getUserAnalyticsOverview() {
        int totalUsers = (int) userRepository.countByDeleteFlag(UserConstants.DELETE_FLAG_NORMAL);
        int activeUsers = (int) userRepository.countByDeleteFlagAndStatus(
                UserConstants.DELETE_FLAG_NORMAL, UserConstants.STATUS_NORMAL);
        int disabledUsers = (int) userRepository.countByDeleteFlagAndStatus(
                UserConstants.DELETE_FLAG_NORMAL, UserConstants.STATUS_DISABLED);

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        int newUsers30Days = (int) userRepository.countByDeleteFlagAndCreatedDateTimeUtcGreaterThanEqual(
                UserConstants.DELETE_FLAG_NORMAL, thirtyDaysAgo);

        LocalDate now = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(now);
        YearMonth startMonth = currentMonth.minusMonths(5);
        LocalDateTime startTime = startMonth.atDay(1).atStartOfDay();

        Map<String, Integer> monthCountMap = new LinkedHashMap<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("M月");

        YearMonth cursor = startMonth;
        while (!cursor.isAfter(currentMonth)) {
            monthCountMap.put(cursor.format(monthFormatter), 0);
            cursor = cursor.plusMonths(1);
        }

        List<Object[]> rows = userRepository.countMonthlyGrowth(UserConstants.DELETE_FLAG_NORMAL, startTime);
        for (Object[] row : rows) {
            String month = (String) row[0];
            Number count = (Number) row[1];
            if (monthCountMap.containsKey(month)) {
                monthCountMap.put(month, count.intValue());
            }
        }

        List<UserAnalyticsOverviewDTO.TrendPoint> growthTrend = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : monthCountMap.entrySet()) {
            UserAnalyticsOverviewDTO.TrendPoint point = new UserAnalyticsOverviewDTO.TrendPoint();
            YearMonth ym = YearMonth.parse(entry.getKey(), monthFormatter);
            point.setLabel(ym.format(labelFormatter));
            point.setValue(entry.getValue());
            growthTrend.add(point);
        }

        UserAnalyticsOverviewDTO dto = new UserAnalyticsOverviewDTO();
        dto.setTotalUsers(totalUsers);
        dto.setActiveUsers(activeUsers);
        dto.setDisabledUsers(disabledUsers);
        dto.setNewUsers30Days(newUsers30Days);
        dto.setGrowthTrend(growthTrend);
        return dto;
    }

    private User convertToUserVO(UserPo user) {
        if (user == null) {
            return null;
        }

        User userVO = new User();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setRealName(user.getRealName());
        userVO.setEmail(user.getEmail());
        userVO.setPhone(user.getPhone());
        userVO.setAvatar(user.getAvatar());
        userVO.setIdCardNo(user.getIdCard());
        userVO.setIdCardVerified(user.getIdCardVerified());
        userVO.setFaceVerified(user.getFaceVerified());
        userVO.setStatus(user.getStatus());
        userVO.setLastLoginTime(user.getLastLoginTime());
        userVO.setCreateTime(user.getCreatedDateTimeUtc());
        List<String> roles = roleService.getRolesByUserId(user.getId());
        userVO.setRoles(roles);
        userVO.setPassword(null);
        return userVO;
    }
}
