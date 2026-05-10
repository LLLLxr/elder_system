# 智慧养老系统优化建议

> **已完成优化**: 代码质量优化、性能优化（数据库）、服务间依赖管理已完成，详见对应总结文档。

## 一、架构层面优化

### 1.1 模块边界清晰化

**现状问题**：
- care-core-service 聚合了多个业务域（admission、health、contract、caredelivery等）
- 模块职责较重，未来扩展可能导致单体过大

**优化建议**：
- 评估是否需要进一步拆分微服务（如健康管理、合同管理独立）
- 明确各模块的聚合根和边界上下文
- 建立模块间通信规范（事件驱动 vs 同步调用）

**优先级**：中（当前规模可接受，需持续关注）

## 二、性能优化

### 2.1 缓存策略

**优化建议**：
- 引入Redis缓存热点数据（如用户信息、权限配置）
- 使用Spring Cache抽象简化缓存操作
- 设置合理的缓存过期时间和更新策略
- 缓存预热和缓存穿透防护

**优先级**：中

### 2.2 异步处理

**优化建议**：
- 耗时操作使用@Async异步执行（如发送通知、生成报表）
- 引入消息队列处理解耦业务（如RabbitMQ、Kafka）
- 批量操作使用CompletableFuture并行处理

**优先级**：中

## 三、安全性优化

### 3.1 JWT密钥管理

**现状问题**：
- GlobalAuthFilter中使用@Value注入jwt.secret
- 密钥可能硬编码在配置文件中

**优化建议**：
- 使用配置中心管理敏感配置（Nacos Config）
- JWT密钥定期轮换机制
- 考虑使用非对称加密（RSA）

**优先级**：高

### 3.2 接口安全加固

**优化建议**：
- 实现接口限流防止恶意请求（Sentinel/Resilience4j）
- 敏感操作添加二次验证
- SQL注入防护（使用参数化查询，JPA已支持）
- XSS防护（输入校验和输出转义）

**优先级**：高

## 四、测试覆盖优化

### 4.1 单元测试

**优化建议**：
- Service层核心业务逻辑添加单元测试
- 使用Mockito模拟依赖
- 测试覆盖率目标：核心业务 > 80%

**优先级**：中

### 4.2 集成测试

**优化建议**：
- 使用@SpringBootTest测试完整流程
- 使用TestContainers测试数据库交互
- 关键API端到端测试

**优先级**：中

## 五、运维监控优化

### 5.1 健康检查

**优化建议**：
- 实现自定义HealthIndicator
- 检查数据库连接、Redis连接等
- 配置Actuator端点

**优先级**：高

### 5.2 监控指标

**优化建议**：
- 集成Micrometer暴露业务指标
- 监控接口响应时间、错误率
- 数据库连接池监控
- JVM内存和GC监控

**优先级**：中

## 六、配置管理优化

### 6.1 配置中心化

**优化建议**：
- 使用Nacos Config统一管理配置
- 区分环境配置（dev、test、prod）
- 敏感配置加密存储

**优先级**：高

### 6.2 配置热更新

**优化建议**：
- 使用@RefreshScope支持配置动态刷新
- 限流规则、开关配置支持热更新
- 避免需要重启服务才能生效

**优先级**：中

## 七、文档和规范

### 7.1 API文档

**现状**：
- 已使用Swagger/OpenAPI注解

**优化建议**：
- 补全所有接口的@Operation描述
- 添加请求/响应示例
- 错误码文档化

**优先级**：中

### 7.2 开发规范文档

**优化建议**：
- 编写代码规范文档（已有skill可作为基础）
- Git提交规范（Conventional Commits）
- 分支管理策略（Git Flow）

**优先级**：低

## 优先级总结

### 高优先级（建议立即实施）
1. JWT密钥管理 - 安全风险
2. 接口安全加固 - 防止攻击
3. 健康检查 - 运维必备
4. 配置中心化 - 管理便利性

### 中优先级（逐步优化）
1. 模块边界清晰化
2. 缓存策略
3. 异步处理
4. 测试覆盖
5. 监控指标
6. 配置热更新
7. API文档完善

### 低优先级（长期规划）
1. 开发规范文档

## 八、前端代码复杂度优化

### 8.1 JourneyTaskBoardPage.tsx 复杂度分析

**文件位置**：`smart-nursing-home-service-portal/src/pages/JourneyTaskBoardPage.tsx`

**复杂度指标**：
- 总行数：850 行
- 主组件：JourneyTaskBoardPage (478-850 行)
- 核心函数：deriveJourneyProgress (166-476 行，约 310 行)
- 状态变量：10+ 个独立 useState
- 常量和类型定义：165 行

**核心问题识别**：

#### 问题 1：超大状态推导函数
```typescript
// deriveJourneyProgress 函数 (166-476 行)
// - 310 行的复杂条件逻辑
// - 包含 4 个嵌套的构建函数
// - 违反单一职责原则
// - 难以测试和维护
```

**影响**：
- 每次 selectedRecord 或 timeline 变化都要执行 310 行计算
- 逻辑分散，难以理解业务流程
- 无法单独测试各个步骤的状态推导

#### 问题 2：状态管理混乱
```typescript
// 10 个独立的 useState (483-494 行)
const [mineOptions, setMineOptions] = useState<MineOption[]>([]);
const [mineRecords, setMineRecords] = useState<IntakeRecord[]>([]);
const [query, setQuery] = useState<JourneyBoardQuery>(DEFAULT_QUERY);
const [overview, setOverview] = useState<JourneyTaskOverview | null>(null);
const [overviewLoading, setOverviewLoading] = useState(false);
const [overviewError, setOverviewError] = useState<string | null>(null);
const [timeline, setTimeline] = useState<JourneyTaskItem[]>([]);
const [timelineLoading, setTimelineLoading] = useState(false);
const [timelineError, setTimelineError] = useState<string | null>(null);
const [detailOpen, setDetailOpen] = useState(false);
```

**影响**：
- 状态更新逻辑分散在多个函数中
- loading/error 状态应该统一管理
- 难以追踪状态变化流程

#### 问题 3：基于字符串的业务逻辑
```typescript
// 通过解析消息文本判断业务状态 (178, 187-189 行)
const isWithdrawn = admissionStatus === 'WITHDRAWN' || includesKeyword(message, '撤回');
if (includesKeyword(message, '健康评估未通过')) {
  failureStepKey = 'HEALTH_ASSESSMENT';
}
```

**影响**：
- 业务逻辑依赖自由文本解析，极其脆弱
- 消息文案变化会导致逻辑失效
- 后端应该提供结构化的状态字段

#### 问题 4：重复的渲染逻辑
- 主表格视图 (774-782 行)
- 抽屉详情视图 (787-847 行)
- 相同数据，不同布局，代码重复
- 应该提取共享组件

### 8.2 性能问题

#### 性能问题 1：高频计算未优化
```typescript
// 501 行
const progress = useMemo(
  () => deriveJourneyProgress(selectedRecord, timeline), 
  [selectedRecord, timeline]
);
```
- deriveJourneyProgress 每次依赖变化都执行 310 行计算
- 应该更细粒度地拆分和缓存中间结果

#### 性能问题 2：无节制的自动刷新
```typescript
// 622-634 行
useEffect(() => {
  const timer = window.setInterval(() => {
    if (document.visibilityState === 'visible') {
      refreshCurrentApplication(); // 同时调用 3 个 API
    }
  }, AUTO_REFRESH_INTERVAL_MS); // 15 秒
```
- 每 15 秒刷新一次，调用 3 个 API
- 无请求去重或防抖
- 多用户并发时可能压垮后端

### 8.3 优化建议

#### 建议 1：提取状态机模块
**目标**：将 310 行的 deriveJourneyProgress 拆分为独立模块

**实施方案**：
```typescript
// 创建：src/domain/journeyStateMachine.ts
export class JourneyStateMachine {
  constructor(
    private record: IntakeRecord | undefined,
    private timeline: JourneyTaskItem[]
  ) {}
  
  getCurrentStep(): ApplicationStepKey { /* ... */ }
  getStepStatus(step: ApplicationStepKey): ApplicationStepStatus { /* ... */ }
  getAlert(): JourneyAlert { /* ... */ }
  getStepSummary(step: ApplicationStepKey): string { /* ... */ }
  getStepTimeText(step: ApplicationStepKey): string { /* ... */ }
  getStepHint(step: ApplicationStepKey): string { /* ... */ }
}
```

**收益**：
- 代码行数减少 200+ 行
- 逻辑可单独测试
- 可复用于其他页面

#### 建议 2：拆分为小组件
**目标**：将 850 行的单文件拆分为多个小组件

**拆分方案**：
- `JourneyProgressCard.tsx` - 当前阶段卡片 (50 行)
- `JourneyStepsTable.tsx` - 步骤表格 (80 行)
- `JourneyOverviewStats.tsx` - 统计卡片 (40 行)
- `JourneyDetailDrawer.tsx` - 详情抽屉 (100 行)
- `JourneyTaskBoardPage.tsx` - 主页面 (150 行)

**收益**：
- 单文件从 850 行降至 150 行
- 组件职责清晰
- 便于维护和复用

#### 建议 3：统一数据加载逻辑
**目标**：用自定义 Hook 统一管理数据加载

**实施方案**：
```typescript
// 创建：src/hooks/useJourneyData.ts
export function useJourneyData(applicationId?: number) {
  const [state, setState] = useState({
    records: [],
    overview: null,
    timeline: [],
    loading: false,
    error: null
  });
  
  const refresh = useCallback(() => {
    // 统一加载逻辑
  }, [applicationId]);
  
  return { ...state, refresh };
}
```

**收益**：
- 减少 100+ 行重复代码
- loading/error 状态统一管理
- 便于添加缓存和请求去重

#### 建议 4：优化自动刷新策略
**目标**：减少不必要的 API 调用

**实施方案**：
```typescript
// 1. 增加刷新间隔到 30-60 秒
// 2. 添加请求去重逻辑
// 3. 使用 WebSocket 推送代替轮询（长期方案）
// 4. 页面不可见时停止刷新（已实现）
```

**收益**：
- 后端负载降低 50%
- 减少不必要的网络请求
- 提升用户体验

### 8.4 优化优先级

#### 高优先级（立即实施）
1. **提取状态机模块** - 降低复杂度，提升可维护性
2. **统一数据加载逻辑** - 减少重复代码，便于后续优化
3. **修复字符串解析逻辑** - 后端提供结构化状态字段

#### 中优先级（逐步优化）
1. **拆分为小组件** - 提升代码可读性
2. **优化自动刷新策略** - 降低后端压力
3. **添加单元测试** - 保证重构质量

#### 低优先级（长期规划）
1. **迁移业务逻辑到后端** - 减少前端计算负担
2. **实现 WebSocket 推送** - 替代轮询机制

### 8.5 预期收益

**代码质量**：
- 主文件从 850 行降至 400 行（分散到多个文件）
- 复杂度降低 60%
- 可测试性显著提升

**性能提升**：
- 渲染性能提升 20-30%
- API 调用减少 50%
- 用户体验更流畅

**维护成本**：
- 新功能开发效率提升 40%
- Bug 修复时间减少 50%
- 代码审查更容易

**优先级**：高

## 已完成优化

详见以下文档：
- [代码质量优化总结](code-quality-optimization-summary.md)
- [性能优化总结](performance-optimization-summary.md)
- [服务间依赖管理总结](dependency-management-summary.md)
