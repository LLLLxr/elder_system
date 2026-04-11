package org.smart_elder_system.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.smart_elder_system.user.constant.UserConstants;
import org.smart_elder_system.user.dto.FaceVerifyDTO;
import org.smart_elder_system.user.entity.FaceVerifyRecord;
import org.smart_elder_system.user.entity.User;
import org.smart_elder_system.user.exception.BusinessException;
import org.smart_elder_system.user.repository.FaceVerifyRecordRepository;
import org.smart_elder_system.user.repository.UserRepository;
import org.smart_elder_system.user.service.FaceVerifyService;
import org.smart_elder_system.user.vo.VerifyResultVO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * 人脸验证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FaceVerifyServiceImpl implements FaceVerifyService {

    private final FaceVerifyRecordRepository faceVerifyRecordRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VerifyResultVO verifyFace(FaceVerifyDTO faceVerifyDTO) {
        // NOTE: In a real implementation, userId would come from the security context
        // For now, this is a simplified version

        // 1. 创建验证记录 (user would be set from security context)
        FaceVerifyRecord record = new FaceVerifyRecord();
        record.setFaceImageUrl(faceVerifyDTO.getFaceImageUrl());
        record.setVerifyStatus(UserConstants.VERIFY_STATUS_PROCESSING);
        record.setCreateTime(LocalDateTime.now());
        record = faceVerifyRecordRepository.save(record);

        // 2. 调用第三方人脸验证服务（模拟）
        VerifyResult verifyResult = callThirdPartyVerifyService(faceVerifyDTO);
        
        // 3. 更新验证记录
        return updateVerifyResult(record.getId(), verifyResult);
    }

    @Override
    public Page<FaceVerifyRecord> getVerifyRecords(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return faceVerifyRecordRepository.findByUserIdPaged(userId, pageable);
    }

    /**
     * 调用第三方人脸验证服务（模拟）
     */
    private VerifyResult callThirdPartyVerifyService(FaceVerifyDTO faceVerifyDTO) {
        try {
            log.info("调用第三方人脸验证服务");
            Thread.sleep(800);
            
            Random random = new Random();
            boolean isValid = random.nextDouble() > 0.2;
            BigDecimal similarity = isValid ?
                    new BigDecimal(85 + random.nextDouble() * 10).setScale(2, RoundingMode.HALF_UP) :
                    new BigDecimal(60 + random.nextDouble() * 15).setScale(2, RoundingMode.HALF_UP);

            log.info("人脸验证结果: {}, 相似度: {}", isValid ? "验证通过" : "验证失败", similarity);
            
            return new VerifyResult(isValid, similarity);
        } catch (Exception e) {
            log.error("调用第三方人脸验证服务失败", e);
            throw new BusinessException("人脸验证服务暂时不可用，请稍后再试");
        }
    }

    /**
     * 更新验证结果
     */
    private VerifyResultVO updateVerifyResult(Long recordId, VerifyResult verifyResult) {
        FaceVerifyRecord record = faceVerifyRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException("验证记录不存在"));

        record.setVerifyStatus(verifyResult.isValid() ?
                UserConstants.VERIFY_STATUS_SUCCESS : UserConstants.VERIFY_STATUS_FAILED);
        record.setVerifyTime(LocalDateTime.now());
        record.setSimilarity(verifyResult.getSimilarity());
        record.setVerifyResult(verifyResult.getSimilarity().compareTo(new BigDecimal("80")) >= 0 ? 
                "验证通过" : "验证失败");
        
        faceVerifyRecordRepository.save(record);

        // 如果验证成功且有关联用户，更新用户信息
        if (verifyResult.isValid() && record.getUser() != null &&
                verifyResult.getSimilarity().compareTo(new BigDecimal("80")) >= 0) {
            User user = record.getUser();
            user.setFaceVerified(UserConstants.FACE_VERIFIED);
            userRepository.save(user);
        }

        // 返回验证结果
        VerifyResultVO result = new VerifyResultVO();
        result.setSuccess(verifyResult.isValid());
        result.setScore(verifyResult.getSimilarity());
        result.setMessage(record.getVerifyResult());
        result.setVerifyTime(record.getVerifyTime());

        return result;
    }
    
    /**
     * 验证结果内部类
     */
    private static class VerifyResult {
        private final boolean valid;
        private final BigDecimal similarity;

        public VerifyResult(boolean valid, BigDecimal similarity) {
            this.valid = valid;
            this.similarity = similarity;
        }

        public boolean isValid() {
            return valid;
        }

        public BigDecimal getSimilarity() {
            return similarity;
        }
    }
}