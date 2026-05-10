# 服务间依赖管理规范

## 核心原则

### 1. 禁止跨模块直接依赖

**禁止**：
- ❌ 模块A直接依赖模块B的Po类
- ❌ 模块A直接依赖模块B的Repository
- ❌ 模块A直接依赖模块B的Service实现类

**允许**：
- ✅ 通过Feign Client调用其他服务
- ✅ 依赖common-lib中的公共DTO
- ✅ 依赖common-lib中的工具类

### 2. 分层依赖规则

```
Controller → Service → Repository → Po
    ↓          ↓
   DTO        VO
```

**规则**：
- Controller只能调用Service，不能直接访问Repository或Po
- Service可以调用Repository和操作Po
- Repository只操作Po
- Controller和Service使用DTO/VO与外部交互

### 3. 模块内部依赖

同一服务内的不同业务模块：
- 可以通过Service层相互调用
- 不能跨模块直接访问Repository或Po
- 优先通过领域事件解耦

## ArchUnit架构测试

### 运行测试

```bash
mvn test -Dtest=*ArchitectureTest
```

### 测试覆盖

1. **LayerDependencyRulesTest** - 分层依赖检查
   - Controller不能访问Repository
   - Controller不能访问Po
   - 分层架构完整性

2. **ModuleDependencyRulesTest** - 跨模块依赖检查
   - 禁止跨模块访问Po
   - 禁止跨模块访问Repository

## 跨服务通信方式

### 1. 同步调用 - Feign Client

**适用场景**：
- 需要立即获取返回结果
- 查询操作
- 轻量级数据交互

**示例**：
```java
@FeignClient(name = "iam-service")
public interface UserFeignClient {
    @GetMapping("/users/{userId}")
    UserDto getUser(@PathVariable Long userId);
}
```

### 2. 异步通信 - 消息队列

**适用场景**：
- 不需要立即返回结果
- 事件通知
- 解耦业务流程

**示例**：
```java
// 发布事件
eventPublisher.publishEvent(new ApplicationSubmittedEvent(applicationId));

// 监听事件
@EventListener
public void handleApplicationSubmitted(ApplicationSubmittedEvent event) {
    // 处理逻辑
}
```

## 违规示例与修正

### 违规示例1：Controller直接访问Repository

```java
// ❌ 错误
@RestController
public class AdmissionController {
    @Autowired
    private ServiceApplicationRepository repository;
    
    @GetMapping("/applications")
    public List<ServiceApplicationPo> list() {
        return repository.findAll();
    }
}
```

**修正**：
```java
// ✅ 正确
@RestController
public class AdmissionController {
    @Autowired
    private AdmissionService service;
    
    @GetMapping("/applications")
    public ApiResponse<List<ServiceApplicationDto>> list() {
        return ApiResponse.success(service.listApplications());
    }
}
```

### 违规示例2：跨模块直接访问Repository

```java
// ❌ 错误
@Service
public class AdmissionService {
    @Autowired
    private ElderProfileRepository elderRepository; // 跨模块依赖
}
```

**修正**：
```java
// ✅ 正确
@Service
public class AdmissionService {
    @Autowired
    private ElderFeignClient elderClient; // 通过Feign Client
}
```

## 持续集成

将ArchUnit测试加入CI流程：

```yaml
# .github/workflows/ci.yml
- name: Run Architecture Tests
  run: mvn test -Dtest=*ArchitectureTest
```

架构测试失败将阻止代码合并，确保架构规范得到执行。
