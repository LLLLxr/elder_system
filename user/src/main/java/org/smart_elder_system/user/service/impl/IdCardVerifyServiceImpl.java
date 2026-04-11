package org.smart_elder_system.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.smart_elder_system.user.util.IdCardUtil;
import org.smart_elder_system.user.constant.UserConstants;
import org.smart_elder_system.user.dto.IdCardVerifyDTO;
import org.smart_elder_system.user.entity.IdCardVerifyRecord;
import org.smart_elder_system.user.entity.User;
import org.smart_elder_system.user.exception.BusinessException;
import org.smart_elder_system.user.repository.IdCardVerifyRecordRepository;
import org.smart_elder_system.user.repository.UserRepository;
import org.smart_elder_system.user.service.IdCardVerifyService;
import org.smart_elder_system.user.vo.VerifyResultVO;

import java.time.LocalDateTime;

/**
 * 身份证验证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdCardVerifyServiceImpl implements IdCardVerifyService {

    private final IdCardVerifyRecordRepository idCardVerifyRecordRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VerifyResultVO verifyIdCard(IdCardVerifyDTO idCardVerifyDTO) {
        // 1. 验证身份证号格式
        if (!IdCardUtil.validateIdCard(idCardVerifyDTO.getIdCardNo())) {
            throw new BusinessException("身份证号格式不正确");
        }

        // 2. 检查身份证号是否已被其他用户使用
        User existUser = userRepository.findByIdCardAndDeleteFlag(
                idCardVerifyDTO.getIdCardNo(), UserConstants.DELETE_FLAG_NORMAL)
                .orElse(null);

        // 3. 创建验证记录
        IdCardVerifyRecord record = new IdCardVerifyRecord();
        record.setRealName(idCardVerifyDTO.getIdCardName());
        record.setIdCard(idCardVerifyDTO.getIdCardNo());
        record.setVerifyStatus(UserConstants.VERIFY_STATUS_PROCESSING);
        record.setCreateTime(LocalDateTime.now());
        record = idCardVerifyRecordRepository.save(record);

        // 4. 调用第三方身份证验证服务（模拟）
        boolean verifyResult = callThirdPartyVerifyService(idCardVerifyDTO);
        
        // 5. 更新验证记录和用户信息
        return updateVerifyResult(record.getId(), verifyResult);
    }

    @Override
    public Page<IdCardVerifyRecord> getVerifyRecords(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return idCardVerifyRecordRepository.findByUserIdPaged(userId, pageable);
    }

    /**
     * 调用第三方身份证验证服务（模拟）
     */
    private boolean callThirdPartyVerifyService(IdCardVerifyDTO idCardVerifyDTO) {
        try {
            log.info("调用第三方身份证验证服务: 姓名={}, 身份证号={}",
                    idCardVerifyDTO.getIdCardName(), idCardVerifyDTO.getIdCardNo());

            Thread.sleep(500);
            
            boolean isValid = IdCardUtil.validateIdCard(idCardVerifyDTO.getIdCardNo())
                    && idCardVerifyDTO.getIdCardName() != null
                    && !idCardVerifyDTO.getIdCardName().trim().isEmpty();

            log.info("身份证验证结果: {}", isValid ? "验证通过" : "验证失败");
            return isValid;
        } catch (Exception e) {
            log.error("调用第三方身份证验证服务失败", e);
            throw new BusinessException("身份证验证服务暂时不可用，请稍后再试");
        }
    }

    /**
     * 更新验证结果
     */
    private VerifyResultVO updateVerifyResult(Long recordId, boolean verifyResult) {
        IdCardVerifyRecord record = idCardVerifyRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException("验证记录不存在"));

        record.setVerifyStatus(verifyResult ? UserConstants.VERIFY_STATUS_SUCCESS : UserConstants.VERIFY_STATUS_FAILED);
        record.setVerifyTime(LocalDateTime.now());
        record.setVerifyResult(verifyResult ? "验证通过" : "验证失败");
        
        idCardVerifyRecordRepository.save(record);

        // 如果验证成功且有关联用户，更新用户信息
        if (verifyResult && record.getUser() != null) {
            User user = record.getUser();
            user.setRealName(record.getRealName());
            user.setIdCard(record.getIdCard());
            user.setIdCardVerified(UserConstants.ID_CARD_VERIFIED);
            userRepository.save(user);
        }

        // 返回验证结果
        VerifyResultVO result = new VerifyResultVO();
        result.setSuccess(verifyResult);
        result.setMessage(verifyResult ? "验证通过" : "验证失败");
        result.setVerifyTime(record.getVerifyTime());

        return result;
    }
}