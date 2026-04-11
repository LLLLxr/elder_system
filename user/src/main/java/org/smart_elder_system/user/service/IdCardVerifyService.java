package org.smart_elder_system.user.service;

import org.smart_elder_system.user.dto.IdCardVerifyDTO;
import org.smart_elder_system.user.vo.VerifyResultVO;

/**
 * 身份证验证服务接口
 */
public interface IdCardVerifyService {

    /**
     * 验证身份证信息
     *
     * @param idCardVerifyDTO 身份证验证信息
     * @return 验证结果
     */
    VerifyResultVO verifyIdCard(IdCardVerifyDTO idCardVerifyDTO);

    /**
     * 查询身份证验证记录
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 身份证验证记录
     */
    Object getVerifyRecords(Long userId, int page, int size);
}