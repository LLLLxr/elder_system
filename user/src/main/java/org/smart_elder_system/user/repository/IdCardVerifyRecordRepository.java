package org.smart_elder_system.user.repository;

import org.smart_elder_system.user.entity.IdCardVerifyRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 身份证验证记录JPA仓库接口
 */
@Repository
public interface IdCardVerifyRecordRepository extends JpaRepository<IdCardVerifyRecord, Long> {

    /**
     * 根据用户ID查找身份证验证记录
     */
    @Query("SELECT r FROM IdCardVerifyRecord r WHERE r.user.id = :userId ORDER BY r.createTime DESC")
    Page<IdCardVerifyRecord> findByUserIdPaged(@Param("userId") Long userId, Pageable pageable);

    /**
     * 根据用户ID查找身份证验证记录
     */
    @Query("SELECT r FROM IdCardVerifyRecord r WHERE r.user.id = :userId ORDER BY r.createTime DESC")
    List<IdCardVerifyRecord> findByUserIdList(@Param("userId") Long userId);

    /**
     * 根据身份证号查找身份证验证记录
     *
     * @param idCard 身份证号
     * @return 身份证验证记录
     */
    List<IdCardVerifyRecord> findByIdCard(String idCard);
    
    /**
     * 根据验证状态查找身份证验证记录
     *
     * @param verifyStatus 验证状态
     * @param pageable 分页参数
     * @return 身份证验证记录
     */
    Page<IdCardVerifyRecord> findByVerifyStatus(Integer verifyStatus, Pageable pageable);
    
    /**
     * 查找最近的身份证验证记录
     *
     * @param userId 用户ID
     * @return 身份证验证记录
     */
    @Query("SELECT r FROM IdCardVerifyRecord r WHERE r.user.id = :userId " +
           "ORDER BY r.createTime DESC")
    List<IdCardVerifyRecord> findLatestByUserId(@Param("userId") Long userId);
    
    /**
     * 统计用户的身份证验证记录
     *
     * @param userId 用户ID
     * @param verifyStatus 验证状态
     * @return 记录数量
     */
    @Query("SELECT COUNT(r) FROM IdCardVerifyRecord r WHERE r.user.id = :userId " +
           "AND (:verifyStatus IS NULL OR r.verifyStatus = :verifyStatus)")
    long countByUserIdAndVerifyStatus(@Param("userId") Long userId, @Param("verifyStatus") Integer verifyStatus);
}