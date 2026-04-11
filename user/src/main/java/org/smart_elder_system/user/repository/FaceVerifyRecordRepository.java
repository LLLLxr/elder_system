package org.smart_elder_system.user.repository;

import org.smart_elder_system.user.entity.FaceVerifyRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 人脸验证记录JPA仓库接口
 */
@Repository
public interface FaceVerifyRecordRepository extends JpaRepository<FaceVerifyRecord, Long> {

    /**
     * 根据用户ID查找人脸验证记录
     */
    @Query("SELECT r FROM FaceVerifyRecord r WHERE r.user.id = :userId ORDER BY r.createTime DESC")
    Page<FaceVerifyRecord> findByUserIdPaged(@Param("userId") Long userId, Pageable pageable);

    /**
     * 根据用户ID查找人脸验证记录
     */
    @Query("SELECT r FROM FaceVerifyRecord r WHERE r.user.id = :userId ORDER BY r.createTime DESC")
    List<FaceVerifyRecord> findByUserIdList(@Param("userId") Long userId);

    /**
     * 根据验证状态查找人脸验证记录
     *
     * @param verifyStatus 验证状态
     * @param pageable 分页参数
     * @return 人脸验证记录
     */
    Page<FaceVerifyRecord> findByVerifyStatus(Integer verifyStatus, Pageable pageable);
    
    /**
     * 查找最近的人脸验证记录
     *
     * @param userId 用户ID
     * @return 人脸验证记录
     */
    @Query("SELECT r FROM FaceVerifyRecord r WHERE r.user.id = :userId " +
           "ORDER BY r.createTime DESC")
    List<FaceVerifyRecord> findLatestByUserId(@Param("userId") Long userId);
    
    /**
     * 查找用户最高相似度的人脸验证记录
     *
     * @param userId 用户ID
     * @return 人脸验证记录
     */
    @Query("SELECT r FROM FaceVerifyRecord r WHERE r.user.id = :userId " +
           "AND r.verifyStatus = 1 ORDER BY r.similarity DESC")
    List<FaceVerifyRecord> findHighestSimilarityByUserId(@Param("userId") Long userId);
    
    /**
     * 统计用户的人脸验证记录
     *
     * @param userId 用户ID
     * @param verifyStatus 验证状态
     * @return 记录数量
     */
    @Query("SELECT COUNT(r) FROM FaceVerifyRecord r WHERE r.user.id = :userId " +
           "AND (:verifyStatus IS NULL OR r.verifyStatus = :verifyStatus)")
    long countByUserIdAndVerifyStatus(@Param("userId") Long userId, @Param("verifyStatus") Integer verifyStatus);
    
    /**
     * 计算用户的平均人脸相似度
     *
     * @param userId 用户ID
     * @return 平均相似度
     */
    @Query("SELECT AVG(r.similarity) FROM FaceVerifyRecord r WHERE r.user.id = :userId " +
           "AND r.verifyStatus = 1 AND r.similarity IS NOT NULL")
    Double averageSimilarityByUserId(@Param("userId") Long userId);
}