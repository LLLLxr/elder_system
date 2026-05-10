# DTO投影使用指南

## 什么是DTO投影

DTO投影允许查询只返回需要的字段，而不是完整的实体对象，从而提升查询性能。

## 使用方式

### 1. 接口投影

```java
public interface ServiceApplicationSummary {
    Long getId();
    String getApplicantName();
    String getStatus();
    LocalDateTime getSubmittedAt();
}

// Repository
List<ServiceApplicationSummary> findByStatus(String status);
```

### 2. 类投影（DTO）

```java
@Query("SELECT new org.smart_elder_system.common.dto.ServiceApplicationSummaryDto(a.id, a.applicantName, a.status) FROM ServiceApplicationPo a WHERE a.status = :status")
List<ServiceApplicationSummaryDto> findSummaryByStatus(@Param("status") String status);
```

### 3. 原生查询投影

```java
@Query(value = "SELECT id, applicant_name, status FROM care_service_application WHERE status = ?1", nativeQuery = true)
List<Object[]> findRawByStatus(String status);
```

## 使用场景

- 列表查询（只需要部分字段）
- 统计查询（聚合结果）
- 大数据量查询（减少内存占用）
