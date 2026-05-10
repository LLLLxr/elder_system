package org.smart_elder_system.user.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.smart_elder_system.common.dto.elder.ElderProfileDto;
import org.smart_elder_system.user.constant.UserConstants;
import org.smart_elder_system.user.dto.ElderBindingDto;
import org.smart_elder_system.user.dto.ElderBindingRequestDto;
import org.smart_elder_system.user.dto.ElderBindingReviewDto;
import org.smart_elder_system.user.dto.FamilyElderBindingRequestCreateDto;
import org.smart_elder_system.user.exception.BusinessException;
import org.smart_elder_system.user.feign.CareCoreElderProfileClient;
import org.smart_elder_system.user.po.RolePo;
import org.smart_elder_system.user.po.UserElderBindingPo;
import org.smart_elder_system.user.po.UserElderBindingRequestPo;
import org.smart_elder_system.user.po.UserPo;
import org.smart_elder_system.user.po.UserRolePo;
import org.smart_elder_system.user.repository.RoleRepository;
import org.smart_elder_system.user.repository.UserElderBindingRepository;
import org.smart_elder_system.user.repository.UserElderBindingRequestRepository;
import org.smart_elder_system.user.repository.UserRepository;
import org.smart_elder_system.user.repository.UserRoleRepository;
import org.smart_elder_system.user.util.IdCardUtil;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ElderBindingService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserElderBindingRepository userElderBindingRepository;
    private final UserElderBindingRequestRepository userElderBindingRequestRepository;
    private final CareCoreElderProfileClient careCoreElderProfileClient;

    public List<ElderBindingDto> listMyBindings(String username) {
        UserPo currentUser = requireActiveUser(username);
        return listBindingsByUserId(currentUser.getId());
    }

    public List<ElderBindingDto> listBindingsByUserId(Long userId) {
        return userElderBindingRepository.findByUserIdOrderByCreatedDateTimeUtcDesc(userId).stream()
                .map(this::toBindingDto)
                .toList();
    }

    public List<ElderBindingRequestDto> listMyBindingRequests(String username) {
        UserPo currentUser = requireActiveUser(username);
        return userElderBindingRequestRepository.findByApplicantUserIdOrderByCreatedDateTimeUtcDesc(currentUser.getId()).stream()
                .map(this::toRequestDto)
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public ElderBindingRequestDto submitFamilyBindingRequest(String username, FamilyElderBindingRequestCreateDto dto) {
        UserPo currentUser = requireActiveUser(username);
        String elderIdCard = normalizeIdCard(dto.getElderIdCard());
        if (!IdCardUtil.validateIdCard(elderIdCard)) {
            throw new BusinessException("老人身份证号格式不正确");
        }
        if (userElderBindingRequestRepository.existsByApplicantUserIdAndElderIdCardAndStatus(
                currentUser.getId(), elderIdCard, UserConstants.ELDER_BINDING_REQUEST_STATUS_PENDING)) {
            throw new BusinessException("已存在待审核的绑定申请，请勿重复提交");
        }
        if (!StringUtils.hasText(dto.getRelationToElder())) {
            throw new BusinessException("与老人关系不能为空");
        }
        String relationToElder = dto.getRelationToElder().trim();
        if (UserConstants.ELDER_BINDING_RELATION_SELF.equals(relationToElder)) {
            throw new BusinessException("家属绑定申请中的与老人关系不能填写本人，请改用老人本人绑定");
        }

        LocalDateTime now = LocalDateTime.now();
        UserElderBindingRequestPo request = new UserElderBindingRequestPo();
        request.setApplicantUserId(currentUser.getId());
        request.setElderName(dto.getElderName().trim());
        request.setElderIdCard(elderIdCard);
        request.setElderPhone(normalizeBlank(dto.getElderPhone()));
        request.setRelationToElder(relationToElder);
        request.setStatus(UserConstants.ELDER_BINDING_REQUEST_STATUS_PENDING);
        request.setCreatedBy(username);
        request.setLastModifiedBy(username);
        request.setCreatedDateTimeUtc(now);
        request.setLastModifiedDateTimeUtc(now);
        return toRequestDto(userElderBindingRequestRepository.save(request));
    }

    @Transactional(rollbackFor = Exception.class)
    public ElderBindingDto createSelfBinding(String username) {
        UserPo currentUser = requireActiveUser(username);
        if (!StringUtils.hasText(currentUser.getRealName())) {
            throw new BusinessException("当前账号未完善真实姓名，无法绑定本人老人档案");
        }
        if (!StringUtils.hasText(currentUser.getIdCard()) || !IdCardUtil.validateIdCard(currentUser.getIdCard())) {
            throw new BusinessException("当前账号未完善有效身份证号，无法绑定本人老人档案");
        }

        ElderProfileDto elderProfile = ensureElderProfile(
                currentUser.getRealName(),
                normalizeIdCard(currentUser.getIdCard()),
                currentUser.getPhone());

        UserElderBindingPo existingSelfBinding = userElderBindingRepository
                .findTopByElderIdAndBindingType(elderProfile.getElderId(), UserConstants.ELDER_BINDING_TYPE_SELF)
                .orElse(null);
        if (existingSelfBinding != null && !existingSelfBinding.getUserId().equals(currentUser.getId())) {
            throw new BusinessException("该老人已被其他账号以本人身份绑定");
        }

        LocalDateTime now = LocalDateTime.now();
        UserElderBindingPo binding = userElderBindingRepository
                .findByUserIdAndElderIdAndBindingType(currentUser.getId(), elderProfile.getElderId(), UserConstants.ELDER_BINDING_TYPE_SELF)
                .orElseGet(() -> {
                    UserElderBindingPo po = new UserElderBindingPo();
                    po.setUserId(currentUser.getId());
                    po.setElderId(elderProfile.getElderId());
                    po.setBindingType(UserConstants.ELDER_BINDING_TYPE_SELF);
                    po.setRelationToElder(null);
                    po.setCreatedBy(username);
                    po.setLastModifiedBy(username);
                    po.setCreatedDateTimeUtc(now);
                    po.setLastModifiedDateTimeUtc(now);
                    return userElderBindingRepository.save(po);
                });

        ensureRoleAssigned(currentUser.getId(), "ELDER", username);
        return toBindingDto(binding, elderProfile);
    }

    public List<ElderBindingRequestDto> listBindingRequests(String status) {
        List<UserElderBindingRequestPo> requests = StringUtils.hasText(status)
                ? userElderBindingRequestRepository.findByStatusOrderByCreatedDateTimeUtcDesc(status)
                : userElderBindingRequestRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDateTimeUtc"));
        return requests.stream().map(this::toRequestDto).toList();
    }

    public ElderBindingRequestDto getBindingRequestDetail(Long requestId) {
        UserElderBindingRequestPo request = userElderBindingRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("绑定申请不存在"));
        return toRequestDto(request);
    }

    @Transactional(rollbackFor = Exception.class)
    public ElderBindingRequestDto approveBindingRequest(Long requestId, ElderBindingReviewDto dto, String reviewerUsername) {
        UserPo reviewer = requireActiveUser(reviewerUsername);
        UserElderBindingRequestPo request = userElderBindingRequestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new BusinessException("绑定申请不存在"));
        ensurePending(request);

        ElderProfileDto elderProfile = ensureElderProfile(request.getElderName(), request.getElderIdCard(), request.getElderPhone());
        LocalDateTime now = LocalDateTime.now();
        userElderBindingRepository.findByUserIdAndElderIdAndBindingType(
                        request.getApplicantUserId(), elderProfile.getElderId(), UserConstants.ELDER_BINDING_TYPE_FAMILY)
                .orElseGet(() -> {
                    UserElderBindingPo po = new UserElderBindingPo();
                    po.setUserId(request.getApplicantUserId());
                    po.setElderId(elderProfile.getElderId());
                    po.setBindingType(UserConstants.ELDER_BINDING_TYPE_FAMILY);
                    po.setRelationToElder(request.getRelationToElder());
                    po.setCreatedBy(reviewerUsername);
                    po.setLastModifiedBy(reviewerUsername);
                    po.setCreatedDateTimeUtc(now);
                    po.setLastModifiedDateTimeUtc(now);
                    return userElderBindingRepository.save(po);
                });

        ensureRoleAssigned(request.getApplicantUserId(), "FAMILY", reviewerUsername);

        request.setElderId(elderProfile.getElderId());
        request.setStatus(UserConstants.ELDER_BINDING_REQUEST_STATUS_APPROVED);
        request.setReviewedBy(reviewer.getUsername());
        request.setReviewComment(normalizeBlank(dto == null ? null : dto.getReviewComment()));
        request.setReviewedAt(now);
        request.setLastModifiedBy(reviewerUsername);
        request.setLastModifiedDateTimeUtc(now);
        return toRequestDto(userElderBindingRequestRepository.save(request));
    }

    @Transactional(rollbackFor = Exception.class)
    public ElderBindingRequestDto rejectBindingRequest(Long requestId, ElderBindingReviewDto dto, String reviewerUsername) {
        UserPo reviewer = requireActiveUser(reviewerUsername);
        UserElderBindingRequestPo request = userElderBindingRequestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new BusinessException("绑定申请不存在"));
        ensurePending(request);
        if (dto == null || !StringUtils.hasText(dto.getReviewComment())) {
            throw new BusinessException("驳回申请时必须填写审核意见");
        }

        LocalDateTime now = LocalDateTime.now();
        request.setStatus(UserConstants.ELDER_BINDING_REQUEST_STATUS_REJECTED);
        request.setReviewedBy(reviewer.getUsername());
        request.setReviewComment(dto.getReviewComment().trim());
        request.setReviewedAt(now);
        request.setLastModifiedBy(reviewerUsername);
        request.setLastModifiedDateTimeUtc(now);
        return toRequestDto(userElderBindingRequestRepository.save(request));
    }

    private UserPo requireActiveUser(String username) {
        UserPo user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (UserConstants.DELETE_FLAG_DELETED.equals(user.getDeleteFlag())) {
            throw new BusinessException("用户不存在");
        }
        if (!UserConstants.STATUS_NORMAL.equals(user.getStatus())) {
            throw new BusinessException("用户已被禁用");
        }
        return user;
    }

    private void ensurePending(UserElderBindingRequestPo request) {
        if (!UserConstants.ELDER_BINDING_REQUEST_STATUS_PENDING.equals(request.getStatus())) {
            throw new BusinessException("该绑定申请已完成审核");
        }
    }

    private ElderProfileDto ensureElderProfile(String elderName, String idCard, String phone) {
        try {
            return careCoreElderProfileClient.getByIdCard(idCard);
        } catch (FeignException.NotFound notFound) {
            ElderProfileDto dto = new ElderProfileDto();
            dto.setElderName(elderName);
            dto.setIdCard(idCard);
            dto.setPhone(normalizeBlank(phone));
            dto.setBirthDate(IdCardUtil.getBirthdayFromIdCard(idCard));
            dto.setGender(IdCardUtil.getGenderFromIdCard(idCard));
            dto.setStatus("ACTIVE");
            return careCoreElderProfileClient.createIfAbsent(dto);
        }
    }

    private void ensureRoleAssigned(Long userId, String roleCode, String operatorUsername) {
        RolePo role = roleRepository.findByRoleCodeAndStatusAndDeleteFlag(
                roleCode, UserConstants.STATUS_NORMAL, UserConstants.DELETE_FLAG_NORMAL);
        if (role == null) {
            throw new BusinessException("角色不存在: " + roleCode);
        }
        if (roleRepository.countUserRoles(userId, role.getId()) > 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        UserRolePo userRole = new UserRolePo();
        userRole.setUserId(userId);
        userRole.setRoleId(role.getId());
        userRole.setCreatedBy(operatorUsername);
        userRole.setLastModifiedBy(operatorUsername);
        userRole.setCreatedDateTimeUtc(now);
        userRole.setLastModifiedDateTimeUtc(now);
        userRoleRepository.save(userRole);
    }

    private ElderBindingDto toBindingDto(UserElderBindingPo binding) {
        ElderProfileDto elderProfile;
        try {
            elderProfile = careCoreElderProfileClient.getByElderId(binding.getElderId());
        } catch (FeignException exception) {
            throw new BusinessException("未找到关联老人档案");
        }
        return toBindingDto(binding, elderProfile);
    }

    private ElderBindingDto toBindingDto(UserElderBindingPo binding, ElderProfileDto elderProfile) {
        ElderBindingDto dto = new ElderBindingDto();
        dto.setBindingId(binding.getId());
        dto.setUserId(binding.getUserId());
        dto.setElderId(binding.getElderId());
        dto.setBindingType(binding.getBindingType());
        dto.setRelationToElder(binding.getRelationToElder());
        dto.setCreatedAt(binding.getCreatedDateTimeUtc());
        dto.setElderName(elderProfile.getElderName());
        dto.setElderIdCard(elderProfile.getIdCard());
        dto.setElderPhone(elderProfile.getPhone());
        if (UserConstants.ELDER_BINDING_TYPE_SELF.equals(binding.getBindingType())) {
            dto.setRelationToElder(null);
        }
        dto.setGender(elderProfile.getGender());
        dto.setBirthDate(elderProfile.getBirthDate());
        dto.setElderStatus(elderProfile.getStatus());
        return dto;
    }

    private ElderBindingRequestDto toRequestDto(UserElderBindingRequestPo request) {
        ElderBindingRequestDto dto = new ElderBindingRequestDto();
        dto.setRequestId(request.getId());
        dto.setApplicantUserId(request.getApplicantUserId());
        dto.setElderId(request.getElderId());
        dto.setElderName(request.getElderName());
        dto.setElderIdCard(request.getElderIdCard());
        dto.setElderPhone(request.getElderPhone());
        dto.setBindingType(UserConstants.ELDER_BINDING_TYPE_FAMILY);
        dto.setRelationToElder(request.getRelationToElder());
        dto.setStatus(request.getStatus());
        dto.setReviewedBy(request.getReviewedBy());
        dto.setReviewComment(request.getReviewComment());
        dto.setReviewedAt(request.getReviewedAt());
        dto.setCreatedAt(request.getCreatedDateTimeUtc());
        return dto;
    }

    private String normalizeIdCard(String idCard) {
        return idCard == null ? null : idCard.trim().toUpperCase();
    }

    private String normalizeBlank(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
