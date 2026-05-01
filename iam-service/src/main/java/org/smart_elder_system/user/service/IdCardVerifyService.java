package org.smart_elder_system.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.smart_elder_system.common.dto.IdCardVerifyDTO;
import org.smart_elder_system.user.constant.UserConstants;
import org.smart_elder_system.user.exception.BusinessException;
import org.smart_elder_system.user.po.IdCardVerifyRecordPo;
import org.smart_elder_system.user.po.UserPo;
import org.smart_elder_system.user.repository.IdCardVerifyRecordRepository;
import org.smart_elder_system.user.repository.UserRepository;
import org.smart_elder_system.user.util.IdCardUtil;
import org.smart_elder_system.user.vo.VerifyResult;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdCardVerifyService {

    private final IdCardVerifyRecordRepository idCardVerifyRecordRepository;
    private final UserRepository userRepository;

    @Transactional(rollbackFor = Exception.class)
    public VerifyResult verifyIdCard(IdCardVerifyDTO idCardVerifyDTO) {
        if (!IdCardUtil.validateIdCard(idCardVerifyDTO.getIdCardNo())) {
            throw new BusinessException("身份证号格式不正确");
        }

        userRepository.findByIdCardAndDeleteFlag(
                idCardVerifyDTO.getIdCardNo(), UserConstants.DELETE_FLAG_NORMAL).orElse(null);

        IdCardVerifyRecordPo record = new IdCardVerifyRecordPo();
        record.setRealName(idCardVerifyDTO.getIdCardName());
        record.setIdCard(idCardVerifyDTO.getIdCardNo());
        record.setVerifyStatus(UserConstants.VERIFY_STATUS_PROCESSING);
        record = idCardVerifyRecordRepository.save(record);

        boolean verifyResult = callThirdPartyVerifyService(idCardVerifyDTO);
        return updateVerifyResult(record.getId(), verifyResult);
    }

    public Page<IdCardVerifyRecordPo> getVerifyRecords(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdDateTimeUtc"));
        return idCardVerifyRecordRepository.findByUserIdPaged(userId, pageable);
    }

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

    private VerifyResult updateVerifyResult(Long recordId, boolean verifyResult) {
        IdCardVerifyRecordPo record = idCardVerifyRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException("验证记录不存在"));

        record.setVerifyStatus(verifyResult ? UserConstants.VERIFY_STATUS_SUCCESS : UserConstants.VERIFY_STATUS_FAILED);
        record.setVerifyTime(LocalDateTime.now());
        record.setVerifyResult(verifyResult ? "验证通过" : "验证失败");
        idCardVerifyRecordRepository.save(record);

        if (verifyResult && record.getUser() != null) {
            UserPo user = record.getUser();
            user.setRealName(record.getRealName());
            user.setIdCard(record.getIdCard());
            user.setIdCardVerified(UserConstants.ID_CARD_VERIFIED);
            userRepository.save(user);
        }

        VerifyResult result = new VerifyResult();
        result.setSuccess(verifyResult);
        result.setMessage(verifyResult ? "验证通过" : "验证失败");
        result.setVerifyTime(record.getVerifyTime());
        return result;
    }
}
