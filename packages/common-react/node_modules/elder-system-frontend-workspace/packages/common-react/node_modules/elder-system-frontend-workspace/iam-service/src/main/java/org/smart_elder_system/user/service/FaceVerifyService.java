package org.smart_elder_system.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.smart_elder_system.common.dto.FaceVerifyDTO;
import org.smart_elder_system.user.constant.UserConstants;
import org.smart_elder_system.user.exception.BusinessException;
import org.smart_elder_system.user.po.FaceVerifyRecordPo;
import org.smart_elder_system.user.po.UserPo;
import org.smart_elder_system.user.repository.FaceVerifyRecordRepository;
import org.smart_elder_system.user.repository.UserRepository;
import org.smart_elder_system.user.vo.VerifyResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class FaceVerifyService {

    private final FaceVerifyRecordRepository faceVerifyRecordRepository;
    private final UserRepository userRepository;

    @Transactional(rollbackFor = Exception.class)
    public VerifyResult verifyFace(FaceVerifyDTO faceVerifyDTO) {
        FaceVerifyRecordPo record = new FaceVerifyRecordPo();
        record.setFaceImageUrl(faceVerifyDTO.getFaceImageUrl());
        record.setVerifyStatus(UserConstants.VERIFY_STATUS_PROCESSING);
        record = faceVerifyRecordRepository.save(record);

        FaceLocalResult localResult = callThirdPartyVerifyService(faceVerifyDTO);
        return updateVerifyResult(record.getId(), localResult);
    }

    public Page<FaceVerifyRecordPo> getVerifyRecords(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdDateTimeUtc"));
        return faceVerifyRecordRepository.findByUserIdPaged(userId, pageable);
    }

    private FaceLocalResult callThirdPartyVerifyService(FaceVerifyDTO faceVerifyDTO) {
        try {
            log.info("调用第三方人脸验证服务");
            Thread.sleep(800);

            Random random = new Random();
            boolean isValid = random.nextDouble() > 0.2;
            BigDecimal similarity = isValid
                    ? new BigDecimal(85 + random.nextDouble() * 10).setScale(2, RoundingMode.HALF_UP)
                    : new BigDecimal(60 + random.nextDouble() * 15).setScale(2, RoundingMode.HALF_UP);

            log.info("人脸验证结果: {}, 相似度: {}", isValid ? "验证通过" : "验证失败", similarity);
            return new FaceLocalResult(isValid, similarity);
        } catch (Exception e) {
            log.error("调用第三方人脸验证服务失败", e);
            throw new BusinessException("人脸验证服务暂时不可用，请稍后再试");
        }
    }

    private VerifyResult updateVerifyResult(Long recordId, FaceLocalResult localResult) {
        FaceVerifyRecordPo record = faceVerifyRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException("验证记录不存在"));

        record.setVerifyStatus(localResult.isValid()
                ? UserConstants.VERIFY_STATUS_SUCCESS
                : UserConstants.VERIFY_STATUS_FAILED);
        record.setVerifyTime(LocalDateTime.now());
        record.setSimilarity(localResult.getSimilarity());
        record.setVerifyResult(localResult.getSimilarity().compareTo(new BigDecimal("80")) >= 0
                ? "验证通过"
                : "验证失败");

        faceVerifyRecordRepository.save(record);

        if (localResult.isValid() && record.getUser() != null
                && localResult.getSimilarity().compareTo(new BigDecimal("80")) >= 0) {
            UserPo user = record.getUser();
            user.setFaceVerified(UserConstants.FACE_VERIFIED);
            userRepository.save(user);
        }

        VerifyResult result = new VerifyResult();
        result.setSuccess(localResult.isValid());
        result.setScore(localResult.getSimilarity());
        result.setMessage(record.getVerifyResult());
        result.setVerifyTime(record.getVerifyTime());
        return result;
    }

    private static class FaceLocalResult {
        private final boolean valid;
        private final BigDecimal similarity;

        private FaceLocalResult(boolean valid, BigDecimal similarity) {
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
