# VirtualAIBox

> 以 **SECD 为行为执行内核、以 World/Event 为运行环境、以 Memory 为认知状态、以 Effect 为副作用边界、以 LLM 为非确定性 Oracle** 的多智能体沙盒实验系统（AI Sandbox / AI Town 类型）。
>
> 项目定位与边界见 [`docs/vision.md`](docs/vision.md)；当前架构 SSOT 见 [`docs/architecture.md`](docs/architecture.md)。

## 技术栈

- **后端**：Java 21 · Spring Boot 3.5.14 · Maven（项目自带 `mvnw.cmd`）
- **行为内核**：自研 SECD 抽象机（`com.own.virtualaibox.secd`）
- **DSL**：ANTLR 4.13.2（`BehaviorDSL.g4` → `.lambda` 行为程序）
- **LLM**：langchain4j（`secrets.yml` 配置 api-key，文件不入库）
- **前端**：React + TypeScript + Vite（`frontend/`，Maven 构建时自动 build 并拷贝到 `target/classes/static`）

## 快速启动

```bash
# 1. 准备密钥：复制/创建 src/main/resources/secrets.yml（被 .gitignore 排除）
#    参考 application.yml 中的占位符，填入 LLM 平台 api-key
secrets:
  apikey:
    deepseek: <你的 key>

# 2. 启动（自动构建前端）
.\mvnw.cmd spring-boot:run

# 3. 访问
http://localhost:8080
```

> 注意：项目要求 **Java 21**（pom 配置 release 21）。命令行环境若为 JDK 17 会编译失败，请使用 JDK 21 或在 IDE 中配置对应 SDK。

## 使用

- 打开首页 dashboard：观察 37×37 网格世界、每 Agent 的 SECD 心智（S/E/C/D 四寄存器卡 + 状态徽标）、记忆、事件流与收敛指示器。
- 行为 DSL：编辑 [`src/main/resources/agents/default.lambda`](src/main/resources/agents/default.lambda)，重启后生效（按 Agent 名/id/`default` 匹配加载）。语法与契约见 [`docs/spec/behavior-dsl.md`](docs/spec/behavior-dsl.md)。

## API（REST）

| 端点 | 说明 |
|---|---|
| `GET /api/dashboard` | dashboard 聚合：tick、world、agents（含每 Agent 的 `mind`/`memoryStats`/`recentMemories` 字段）、events、metrics、convergence |
| `GET /api/dashboard/state` | 世界快照（tick、world、agents） |
| `GET /api/dashboard/events?limit=50` | 事件历史 |
| `GET /api/dashboard/agents?memoryLimit=8` | Agent 列表（含 SECD 心智摘要与记忆） |
| `GET /api/dashboard/metrics` | 指标（订阅者数、事件历史大小、Agent 数、当前 tick） |
| `GET /api/dashboard/step` | 手动推进一个 tick |
| `POST /api/dashboard/addAgent` | 添加 Agent（**接口未完成**，见下） |

> ⚠ `addAgent` 前后端接口未调通（`DashboardController.addAgent()` 未实现），世界 Agent 初始化入口当前被注释。已知问题详见 [`docs/consistency-audit.md`](docs/consistency-audit.md) §3.3。

## 核心概念速览

| 概念 | 一句话 | 权威文档 |
|---|---|---|
| SECD | 确定性行为执行内核（S 值栈 / E 环境 / C 控制栈 / D 续体） | `docs/spec/agent-runtime.md`、ADR-0001 |
| Effect | SECD 只产出"意图"，EffectExecutor 才执行副作用 | `docs/spec/effect.md`、ADR-0003 |
| LLM Oracle | 只在计划编译期定方向 + ask-llm 原语，不直接改世界 | ADR-0002 |
| Perception | 只读感知通道（范围 5、null→#f、同 tick 快照） | `docs/spec/perception.md` |
| Behavior DSL | `.lambda` 行为程序（def/plan/onMeet） | `docs/spec/behavior-dsl.md` |
| D 栈中断 | Interrupt → Handle → Resume，事件驱动精确恢复 | ADR-0004 |

## 测试

```bash
.\mvnw.cmd test
```

> ⚠ 2026-08-22 曾出现 `AgentRuntimeTest` 3 例失败与 `contextLoads` 失败记录（其后代码有修改、未重跑测试）；当前测试状态未复核，见 `docs/consistency-audit.md` §3.4。

## 文档地图

```text
docs/
├── vision.md              # 为什么做、是什么、边界
├── architecture.md        # 当前架构唯一总览 / SSOT
├── spec/                  # 系统契约（agent-runtime / behavior-dsl / perception / effect）
├── decisions/             # ADR-0001~0004（架构决策记录）
├── changelog.md           # P0-P6 历史演进
├── consistency-audit.md   # 文档-代码一致性审计记录
└── archive/               # 历史文档（只读）：design-v0.3-frozen.md、logs.md
```
