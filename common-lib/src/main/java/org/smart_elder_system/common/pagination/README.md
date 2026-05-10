# 分页查询使用指南

## Repository层

```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

Page<ServiceApplicationPo> findByStatus(String status, Pageable pageable);
```

## Service层

```java
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
Page<ServiceApplicationPo> poPage = repository.findByStatus(status, pageable);
```

## Controller层

```java
@GetMapping("/applications")
public ApiResponse<Page<ServiceApplicationDto>> list(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.success(service.list(page, size));
}
```
