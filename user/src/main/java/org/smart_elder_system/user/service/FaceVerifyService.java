package org.smart_elder_system.user.service;

import org.smart_elder_system.user.dto.FaceVerifyDTO;
import org.smart_elder_system.user.vo.VerifyResultVO;

/**
 * 人脸验证服务接口
 */
public interface FaceVerifyService {

    /**
     * 验证人脸信息
     *
     * @param faceVerifyDTO 人脸验证信息
     * @return 验证结果
     */
    VerifyResultVO verifyFace(FaceVerifyDTO faceVerifyDTO);

    /**
     * 查询人脸验证记录
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 人脸验证记录
     */
    Object getVerifyRecords(Long userId, int page, int size);
}