# Virtual AI Box - 版本更新日志

## 一.vab1.0
这是一个AI沙盒项目的最小实现版本，包含简单、完整的世界模拟系统。

采用全量加载，将所有世界状态提交给大模型，然后生成状态更新和动作。

### 1.核心组件
1. **WorldEngine** - 世界引擎，驱动整个流程
2. **VirtualClock** - 虚拟时钟，管理时间刻度
3. **TickSchedule** - 时间调度器，协调AI决策和动作执行
4. **LLMBrain** - AI大脑，负责决策（当前使用随机策略）
5. **ActionExecutor** - 动作执行器，执行移动动作

### 2.数据模型
- **World** - 世界容器，包含所有Agent
- **Agent** - AI智能体
- **AgentState** - Agent状态（位置等）
- **WorldState** - 世界状态快照
- **MoveAction** - 移动动作

### 3.启动应用
```bash
mvn.cmd spring-boot:run

访问localhost:8080/index.html进行可视化调试
```

### 4.API端点

1. **GET /test** - 测试服务是否运行
   - 返回: "Virtual AI Box is running!"

2. **GET /state** - 查看当前世界状态
   - 返回: 当前tick和所有Agent的位置

3. **GET /step** - 执行一个时间步
   - 触发WorldEngine.step()
   - 时钟tick增加
   - 所有Agent通过LLMBrain决策
   - 执行移动动作
   - 返回: 更新后的世界状态

### 5.工作流程

```
WorldEngine.step()
    ↓
VirtualClock.stepForward()  (tick增加)
    ↓
TickSchedule.processTick()
    ↓
对每个Agent:
    LLMBrain.decideAction()  (决策)
    ActionExecutor.executeMoveAction()  (执行)
```
