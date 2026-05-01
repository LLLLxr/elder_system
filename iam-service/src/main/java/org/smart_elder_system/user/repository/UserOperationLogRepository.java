package org.smart_elder_system.user.repository;

import org.smart_elder_system.user.po.UserOperationLogPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户操作日志JPA仓库接口
 */
@Repository
public interface UserOperationLogRepository extends JpaRepository<UserOperationLogPo, Long> {

    /**
     * 根据用户ID分页查询操作日志
     */
    @Query("SELECT l FROM UserOperationLogPo l WHERE l.userId = :userId ORDER BY l.operationTime DESC")
    Page<UserOperationLogPo> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 根据操作类型分页查询操作日志
     */
    @Query("SELECT l FROM UserOperationLogPo l WHERE l.operationType = :operationType ORDER BY l.operationTime DESC")
    Page<UserOperationLogPo> findByOperationType(@Param("operationType") String operationType, Pageable pageable);

    /**
     * 根据时间范围查询操作日志
     */
    @Query("SELECT l FROM UserOperationLogPo l WHERE l.operationTime BETWEEN :startTime AND :endTime ORDER BY l.operationTime DESC")
    Page<UserOperationLogPo> findByOperationTimeBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    /**
     * 查询用户最近的操作日志
     */
    @Query("SELECT l FROM UserOperationLogPo l WHERE l.userId = :userId ORDER BY l.operationTime DESC")
    List<UserOperationLogPo> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);
}
