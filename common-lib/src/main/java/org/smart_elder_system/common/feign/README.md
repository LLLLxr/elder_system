# Feign Client跨服务通信指南

## 定义Feign Client接口

```java
@FeignClient(name = "iam-service", path = "/users")
public interface UserFeignClient {
    
    @GetMapping("/{userId}")
    UserDto getUser(@PathVariable Long userId);
    
    @GetMapping("/batch")
    List<UserDto> batchGetUsers(@RequestParam List<Long> userIds);
}
```

## 在Service中使用

```java
@Service
@RequiredArgsConstructor
public class AdmissionService {
    
    private final UserFeignClient userFeignClient;
    
    public void processApplication(Long applicationId) {
        // 跨服务调用获取用户信息
        UserDto user = userFeignClient.getUser(userId);
        // 业务逻辑
    }
}
```

## 启用Feign Client

```java
@SpringBootApplication
@EnableFeignClients(basePackages = "org.smart_elder_system")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

## 注意事项

1. 只通过Feign Client调用其他服务
2. 不直接依赖其他服务的Po/Repository
3. 定义清晰的DTO接口契约
4. 考虑服务降级和熔断
