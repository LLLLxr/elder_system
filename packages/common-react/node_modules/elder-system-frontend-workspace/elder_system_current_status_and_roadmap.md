# elder_system 当前完成情况与后续路线

> 本文用于替代以下两份旧文档：
>
> - `elder_system_vs_real_hospital_project_analysis.md`
> - `elder_system_executable_task_list.md`
>
> 写法从“纯分析 + 纯任务清单”改为“当前实际完成情况 + 未完成缺口 + 下一阶段路线图”，避免旧文档和代码状态脱节。

---

## 一、当前结论

当前仓库已经不再只是“最初的流程演示 demo”，在**服务旅程主链路、权限收口、前后端任务看板分层、旅程日志、评估页交互统一**这几块已经有了比较明确的落地。

但如果目标是继续向真实医院 / 医养项目靠近，真正还缺的重点已经不再是页面数量，而是以下四类能力：

1. **后端统一状态机与严格迁移规则**
2. **完整的岗位 / 操作 / 数据范围权限闭环**
3. **表单版本化、字典治理、审计预警等运营合规能力**
4. **外部系统集成边界与适配层**

所以，旧文档里的很多方向并不是错了，而是需要按“已完成 / 部分完成 / 未开始”重新归档。

---

## 二、已经完成的事项

### 1. 旅程主链路已进一步打通，并修复了关键阻塞问题

这部分已经不是停留在分析阶段，而是有代码修复落地：

- 健康评估完成后，健康体检表查询失败的问题已修复，查询逻辑增加了多级兜底。
- 服务协议签署时，JPA 审计字段因“重建实体”丢失的问题已修复，改为更新已加载实体。
- 旅程继续推进、签约、后续服务衔接相关流程已在编排服务中继续收口。

**代码位置**
- `care-core-service/src/main/java/org/smart_elder_system/health/service/HealthService.java`
- `care-core-service/src/main/java/org/smart_elder_system/contract/service/ContractService.java`
- `care-core-service/src/main/java/org/smart_elder_system/careorchestration/service/CareOrchestrationService.java`

**判断**
- 这项属于 **已完成的缺陷修复与主链路加固**。
- 但它还不等于“完整状态机建设完成”。

---

### 2. 前端权限控制已经从“按钮显隐”升级到“页面路由拦截”

这块已经明显落地，不再只是建议：

- 登录后会把用户权限、角色同步到前端状态。
- 审查 / 评估相关页面新增了路由级权限守卫。
- 菜单、按钮、结果页入口也同步按权限控制。
- 没有相关权限的用户，不仅看不到入口，也不能直接靠 URL 进入页面。

**代码位置**
- `smart-nursing-home-service-portal/src/stores/userStore.ts`
- `smart-nursing-home-service-portal/src/api/authApi.ts`
- `smart-nursing-home-service-portal/src/App.tsx`
- `smart-nursing-home-service-portal/src/components/JourneyLayout.tsx`
- `smart-nursing-home-service-portal/src/pages/JourneyOverviewPage.tsx`
- `smart-nursing-home-service-portal/src/pages/JourneyResultPage.tsx`

**判断**
- 这项属于 **已完成**。
- 但当前主要是“页面 / 能力级权限”，还不是完整的数据范围权限、字段级权限体系。

---

### 3. IAM 权限种子数据已经补齐一批旅程关键权限

旧清单里提到“角色-操作矩阵”，目前虽然还没有完整矩阵文档化，但至少关键权限编码和基础角色授权已经补了一批：

- 需求评估通过 / 驳回
- 健康评估通过 / 驳回
- 退回需求评估 / 健康评估
- 申请撤回
- 相关角色初始化授权

**代码位置**
- `iam-service/src/main/resources/sql/data.sql`
- `iam-service/src/main/resources/data-h2-user.sql`
- `iam-service/src/main/java/org/smart_elder_system/user/config/DataInitializer.java`
- `iam-service/src/main/java/org/smart_elder_system/user/service/AuthService.java`

**判断**
- 这项属于 **已完成一部分基础建设**。
- 还没有达到“岗位-操作-数据范围”三层完全闭环。

---

### 4. 后台完整版任务看板与用户侧“我的任务”已经完成拆分

这是本轮最明确的产品结构调整之一，已经落地：

- 后台管理端拥有完整版的服务旅程任务看板。
- 用户端不再使用“运营视角全量任务板”，而改成受限的“我的任务”。
- 用户端只围绕自己的申请，查看当前旅程进度与下一步提示。

**代码位置**
- 后台管理端：`smart-nursing-home-demo/src/pages/admin/JourneyTaskBoardPage.tsx`
- 后台路由：`smart-nursing-home-demo/src/App.tsx`
- 后台菜单：`smart-nursing-home-demo/src/components/AdminLayout.tsx`
- 用户端页面：`smart-nursing-home-service-portal/src/pages/JourneyTaskBoardPage.tsx`

**判断**
- 这项属于 **已完成**。
- 也是旧分析文档里“用户侧应受限、运营侧应完整”的直接实现。

---

### 5. 用户侧“我的任务”已改为按单个申请展示固定旅程步骤

这部分不仅做了结构调整，还做了多轮交互修正：

当前用户侧任务板已经具备这些能力：

- 以单个申请为中心查看旅程，而不是查看后台风格的任务列表。
- 旅程步骤统一为：
  - 提交申请
  - 需求评估
  - 健康评估
  - 签约
  - 服务中
- “等待需求评估”已经回到用户可见步骤里。
- 需求评估和健康评估被纳入同一个申请旅程里展示，而不是拆成两套割裂视图。
- 支持自动刷新与手动刷新，解决健康评估完成后必须整页刷新的问题。
- “我的第 N 次申请”编号已按提交时间顺序修正。
- 查询 / 重置按钮已移除，改为切换申请即加载最新数据。

**代码位置**
- `smart-nursing-home-service-portal/src/pages/JourneyTaskBoardPage.tsx`

**判断**
- 这项属于 **已完成**。
- 旧文档里关于“前端状态展示统一”和“用户视角任务板限制”的核心点，这里已经有实际成果。

---

### 6. 旅程流转日志与后台退回能力已经有基础实现

旧任务清单里“建立旅程事件日志”这一项，并不是完全没做。

目前已经可以确认有以下基础能力：

- 后端存在旅程流转日志模型与服务。
- 可以按申请、按协议查询旅程流转日志。
- 后台任务看板中已经接入日志查看。
- 后台已经具备旅程退回某一步的操作能力。

**代码位置**
- `care-core-service/src/main/java/org/smart_elder_system/careorchestration/po/ServiceJourneyTransitionLogPo.java`
- `care-core-service/src/main/java/org/smart_elder_system/careorchestration/service/ServiceJourneyTransitionLogService.java`
- `care-core-service/src/main/java/org/smart_elder_system/careorchestration/controller/CareOrchestrationController.java`
- `care-core-service/src/main/java/org/smart_elder_system/careorchestration/service/CareOrchestrationService.java`
- `smart-nursing-home-demo/src/api/careOrchestrationApi.ts`
- `smart-nursing-home-demo/src/pages/admin/JourneyTaskBoardPage.tsx`

**判断**
- 这项属于 **部分完成，而且已经有可用基础**。
- 但它目前更准确地说是“旅程流转日志”，还不是“覆盖全部关键操作的统一业务事件中心 + 运营审计中心”。

---

### 7. 后台需求评估页与健康评估页的操作体验已经统一

这项是最近已经完成并构建通过的前端交互收口：

- 两个页面都改成通过上方待处理列表单选决定当前操作对象。
- 列表左侧都有单选圆圈，且支持点击整行高亮选中。
- `申请单ID` 不再作为表单输入项展示，也不能手填。
- `评估人` 默认显示当前登录管理员，且为只读展示。
- 表单提交统一依赖内部选中状态，而不是让操作员自己输入申请单号。

**代码位置**
- `smart-nursing-home-demo/src/pages/admin/NeedsAssessmentPage.tsx`
- `smart-nursing-home-demo/src/pages/admin/HealthAssessmentPage.tsx`
- `smart-nursing-home-demo/src/api/authApi.ts`

**判断**
- 这项属于 **已完成**。
- 也是前端体验统一化里一个比较实的落地点。

---

## 三、部分完成但不能算彻底完成的事项

### 1. “统一状态机 / 流程编排”已有编排中心，但还不是严格状态机体系

当前不是完全没有后端流程中枢：

- 已有 `CareOrchestrationService` 作为旅程编排核心。
- 已有旅程流转日志、任务时间线、退回能力。
- 已有一批流程推进与结果处理逻辑。

但和旧文档想表达的“真实项目化状态机”相比，仍然缺少这些特征：

- 一份明确、可维护的状态迁移矩阵
- 每条迁移的统一前置条件模型
- 更强的非法迁移集中校验
- 更清晰的 side effects 显式定义
- 更系统的幂等 / 并发保护

**判断**
- 应归类为 **部分完成**，不能归类为“状态机建设完成”。

---

### 2. 角色权限已有编码和前端守卫，但还不是完整角色-操作矩阵

当前已经有：

- 关键旅程权限编码
- 前端权限持久化
- 页面入口守卫
- 菜单 / 按钮显隐控制

但还没有完全落地：

- 岗位级职责梳理文档
- 完整的角色-操作矩阵
- 数据范围权限
- 字段级权限
- 跨院区 / 跨机构的数据隔离策略

**判断**
- 应归类为 **部分完成**。

---

### 3. 审计能力已有“流转日志”，但还不是完整合规运营体系

当前已经有：

- JPA 审计基类
- 旅程流转日志
- 一部分关键操作留痕基础

但距离旧文档设想的完整能力还差：

- 高风险操作审计（导出、打印、敏感查看）
- SLA 超时规则
- 预警列表 / 催办列表
- 数据脱敏
- 统一错误监控
- 面向运营 / 监管的审计页面

**判断**
- 应归类为 **部分完成**。

---

## 四、尚未完成、仍然有效的缺口

下面这些内容，在旧分析文档和旧任务清单里提出后，到目前为止仍然可以视为**未完成或基本未开始**。

### 1. 表单版本化

包括但不限于：

- 健康体检表模板版本
- 需求评估 / 健康评估模板版本
- 历史数据按旧版本回显
- 新旧版本共存与兼容统计

**当前判断**
- 还没有形成完整版本化模型。

---

### 2. 字典中心

包括：

- 服务场景
- 终止原因
- 评估结论
- 照护等级
- 其他业务枚举

**当前判断**
- 目前仍以分散字面量 / 枚举为主，尚未形成统一字典中心。

---

### 3. 老人主档统一治理

虽然系统中已经有 `elderId`，但这和“老人主档治理”不是一回事。

真正未完成的部分包括：

- 跨申请 / 评估 / 签约 / 服务 / 评价的一致主档规则
- 外部系统映射编号
- 主数据 owner 定义
- 历史快照策略

**当前判断**
- 仍未完成。

---

### 4. 幂等与并发保护体系

旧任务清单里提到：

- 同一老人不能并行存在多条进行中旅程
- 重复提交不产生脏数据
- 关键推进动作需要并发保护

**当前判断**
- 尚未看到系统级完整落地。
- 这项仍应保留为后续优先任务。

---

### 5. 外部系统集成边界与 adapter / gateway 层

包括：

- HIS / EMR / LIS / 统一身份 / 消息平台的边界设计
- 外部字段来源 owner
- DTO 映射层
- adapter / gateway 目录结构与规范

**当前判断**
- 还没有形成明确的集成边界方案与隔离层。

---

### 6. 统一消息 / 通知通道

包括：

- 站内消息
- 短信 / 微信提醒
- 节点通知模板
- 同步 / 异步消息投递策略

**当前判断**
- 仍未完成。

---

### 7. 更深入的前端平台化能力

包括：

- 旅程状态 schema
- 表格列 schema / 复用层
- 页面 loading / empty / error / no-permission 统一组件
- 更完整的权限驱动 UI
- 表单 schema 化

**当前判断**
- 当前已有局部统一，但离平台化还有距离。

---

## 五、对旧两份文档的重新归类

### 旧文档《差距分析与优化建议》

这份文档里最有价值的部分仍然成立：

- 系统成熟度短板主要不在 UI，而在流程、权限、数据治理、集成、合规。
- 推荐优先级 P0 / P1 / P2 / P3 的方向没有错。

但需要修正的是：

- 文档写作时，很多项还是“建议”；现在其中一部分已经进入代码实现。
- 尤其是：权限守卫、任务看板拆分、旅程流转日志、评估页交互统一，已经不能再写成“待思考”。

### 旧文档《可执行任务清单》

这份文档的问题不是内容错误，而是粒度过于“规划态”。

现在更适合保留的，不是原来的 18 条完整大清单，而是：

- 已经完成的项，从清单中移除
- 部分完成的项，重新定义为“补强任务”
- 未完成但优先级仍高的项，收缩为下一阶段的少量核心任务

---

## 六、建议保留的下一阶段任务

如果从当前代码状态继续推进，建议不要再回到旧文档那种“大而全 18 条并列清单”，而是收缩为下面 6 项。

### P0：先把旅程后端规则真正做硬

1. **把 `CareOrchestrationService` 升级为更明确的状态机规则中心**
   - 产出状态迁移矩阵
   - 明确 allowed roles / required data / side effects
   - 非法迁移统一拒绝

2. **补幂等与并发保护**
   - 限制同一老人同时存在多条进行中旅程
   - 关键推进动作加版本控制或业务锁
   - 重复请求返回可读提示

3. **把“权限编码”升级成“角色-操作-数据范围”完整矩阵**
   - 不只控制页面入口
   - 还要控制接口、按钮、数据范围

### P1：补数据治理与合规

4. **优先做健康体检表 + 评估表版本化**
   - 先做最常变的表单
   - 让历史数据能按原版本回显

5. **建设操作审计与关键节点预警**
   - 在现有旅程流转日志基础上继续扩展
   - 区分流程事件日志与高风险操作审计

### P2：为真实项目接入做边界准备

6. **先定义外部系统集成边界与 adapter 层**
   - 不急着一次性接完 HIS / EMR / LIS
   - 先明确哪些字段来自外部、哪些由本系统维护

---

## 七、当前推荐优先级

### 已完成，可从旧计划中移除

- 权限页面守卫基础版
- 用户端“我的任务”重构
- 后台完整版任务看板落地
- 旅程流转日志基础能力
- 评估页交互统一与申请单选择收口
- 若干主链路阻塞 bug 修复

### 下一步最值得优先做的

1. 后端状态机规则中心
2. 幂等与并发保护
3. 完整权限矩阵
4. 表单版本化
5. 审计与预警
6. 外部集成边界

---

## 八、一句话最终结论

当前 `elder_system` 已经完成了从“单纯演示页面串流程”向“有编排、有权限收口、有前后端分层任务板”的第一阶段升级；接下来最该投入的，不是继续堆页面，而是把**状态机、权限矩阵、表单版本化、审计预警、外部集成边界**做成真正可长期演进的基础设施。
