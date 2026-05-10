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
import org.smart_elder_system.user.dto.ChangePasswordDto;
import org.smart_elder_system.user.dto.RegisterDto;
import org.smart_elder_system.user.dto.ResetPasswordDto;
import org.smart_elder_system.user.dto.UserAnalyticsOverviewDto;
import org.smart_elder_system.user.dto.UserDto;
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
    private final ElderBindingService elderBindingService;

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
    public UserPo createUser(UserDto userDto) {
        validateUniqueFieldsForCreate(
                userDto.getUsername(),
                userDto.getEmail(),
                userDto.getPhone(),
                userDto.getIdCard());

        UserPo user = new UserPo();
        user.setUsername(userDto.getUsername());
        user.setRealName(userDto.getRealName());
        user.setEmail(userDto.getEmail());
        user.setPhone(userDto.getPhone());
        user.setAvatar(userDto.getAvatar());
        user.setIdCard(userDto.getIdCard());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
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
    public UserPo updateUser(UserDto userDto) {
        UserPo existingUser = userRepository.findById(userDto.getId())
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (UserConstants.DELETE_FLAG_DELETED.equals(existingUser.getDeleteFlag())) {
            throw new BusinessException("用户不存在");
        }

        validateUniqueFieldsForUpdate(existingUser, userDto.getEmail(), userDto.getPhone(), userDto.getIdCard());

        existingUser.setRealName(userDto.getRealName());
        existingUser.setEmail(userDto.getEmail());
        existingUser.setPhone(userDto.getPhone());
        existingUser.setAvatar(userDto.getAvatar());
        existingUser.setIdCard(userDto.getIdCard());
        if (userDto.getStatus() != null) {
            existingUser.setStatus(userDto.getStatus());
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

    public Page<User> getUserPage(Pageable pageable, UserDto userDto) {
        Page<UserPo> userPage;
        if ((userDto.getUsername() != null && StringUtils.hasText(userDto.getUsername()))
                || (userDto.getRealName() != null && StringUtils.hasText(userDto.getRealName()))
                || (userDto.getPhone() != null && StringUtils.hasText(userDto.getPhone()))
                || userDto.getStatus() != null) {
            String keyword = "";
            if (StringUtils.hasText(userDto.getUsername())) {
                keyword = userDto.getUsername();
            } else if (StringUtils.hasText(userDto.getRealName())) {
                keyword = userDto.getRealName();
            } else if (StringUtils.hasText(userDto.getPhone())) {
                keyword = userDto.getPhone();
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

    private void validateUniqueFieldsForCreate(String username, String email, String phone, String idCard) {
        if (checkUsernameExists(username)) {
            throw new BusinessException("用户名已存在");
        }
        validateUniqueContactFields(email, phone, idCard);
    }

    private void validateUniqueFieldsForUpdate(UserPo existingUser, String email, String phone, String idCard) {
        validateIdCardFormat(idCard);
        if (StringUtils.hasText(email)
                && !email.equals(existingUser.getEmail())
                && checkEmailExists(email)) {
            throw new BusinessException("邮箱已被使用");
        }
        if (StringUtils.hasText(phone)
                && !phone.equals(existingUser.getPhone())
                && checkPhoneExists(phone)) {
            throw new BusinessException("手机号已被使用");
        }
        if (StringUtils.hasText(idCard)
                && !idCard.equals(existingUser.getIdCard())
                && checkIdCardNoExists(idCard)) {
            throw new BusinessException("身份证号已被使用");
        }
    }

    private void validateUniqueContactFields(String email, String phone, String idCard) {
        if (StringUtils.hasText(email) && checkEmailExists(email)) {
            throw new BusinessException("邮箱已被使用");
        }
        if (StringUtils.hasText(phone) && checkPhoneExists(phone)) {
            throw new BusinessException("手机号已被使用");
        }
        if (StringUtils.hasText(idCard) && checkIdCardNoExists(idCard)) {
            throw new BusinessException("身份证号已被使用");
        }
        validateIdCardFormat(idCard);
    }

    private void validateIdCardFormat(String idCard) {
        if (StringUtils.hasText(idCard) && !IdCardUtil.validateIdCard(idCard)) {
            throw new BusinessException("身份证号格式不正确");
        }
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
    public User register(RegisterDto registerDto) {
        validateUniqueFieldsForCreate(
                registerDto.getUsername(),
                registerDto.getEmail(),
                registerDto.getPhone(),
                registerDto.getIdCard());

        UserPo user = new UserPo();
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setRealName(registerDto.getRealName());
        user.setIdCard(registerDto.getIdCard());
        user.setPhone(registerDto.getPhone());
        user.setEmail(registerDto.getEmail());
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
    public boolean changePassword(ChangePasswordDto changePasswordDto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<UserPo> userOptional = userRepository.findByUsername(username);
        UserPo currentUser = userOptional.orElseThrow(() -> new BusinessException("用户不存在"));
        if (!passwordEncoder.matches(changePasswordDto.getOldPassword(), currentUser.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        currentUser.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        userRepository.save(currentUser);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(ResetPasswordDto resetPasswordDto) {
        Optional<UserPo> userOptional = userRepository.findByUsername(resetPasswordDto.getUsername());
        UserPo user = userOptional.orElseThrow(() -> new BusinessException("用户不存在"));
        user.setPassword(passwordEncoder.encode(resetPasswordDto.getNewPassword()));
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

    public UserAnalyticsOverviewDto getUserAnalyticsOverview() {
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

        List<UserAnalyticsOverviewDto.TrendPoint> growthTrend = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : monthCountMap.entrySet()) {
            UserAnalyticsOverviewDto.TrendPoint point = new UserAnalyticsOverviewDto.TrendPoint();
            YearMonth ym = YearMonth.parse(entry.getKey(), monthFormatter);
            point.setLabel(ym.format(labelFormatter));
            point.setValue(entry.getValue());
            growthTrend.add(point);
        }

        UserAnalyticsOverviewDto dto = new UserAnalyticsOverviewDto();
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
        userVO.setElderBindings(elderBindingService.listBindingsByUserId(user.getId()));
        userVO.setPassword(null);
        return userVO;
    }
}
