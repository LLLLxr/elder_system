package org.smart_elder_system.user.repository;

import org.smart_elder_system.user.entity.UserOperationLog;
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
public interface UserOperationLogRepository extends JpaRepository<UserOperationLog, Long> {

    /**
     * 根据用户ID分页查询操作日志
     */
    @Query("SELECT l FROM UserOperationLog l WHERE l.userId = :userId ORDER BY l.operationTime DESC")
    Page<UserOperationLog> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 根据操作类型分页查询操作日志
     */
    @Query("SELECT l FROM UserOperationLog l WHERE l.operationType = :operationType ORDER BY l.operationTime DESC")
    Page<UserOperationLog> findByOperationType(@Param("operationType") String operationType, Pageable pageable);

    /**
     * 根据时间范围查询操作日志
     */
    @Query("SELECT l FROM UserOperationLog l WHERE l.operationTime BETWEEN :startTime AND :endTime ORDER BY l.operationTime DESC")
    Page<UserOperationLog> findByOperationTimeBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    /**
     * 查询用户最近的操作日志
     */
    @Query("SELECT l FROM UserOperationLog l WHERE l.userId = :userId ORDER BY l.operationTime DESC")
    List<UserOperationLog> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);
}

