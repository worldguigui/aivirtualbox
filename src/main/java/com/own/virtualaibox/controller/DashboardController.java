package com.own.virtualaibox.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.own.virtualaibox.core.WorldEngine;
import com.own.virtualaibox.domain.agent.Agent;
import com.own.virtualaibox.domain.event.DomainEvent;
import com.own.virtualaibox.domain.event.events.AgentDecidedEvent;
import com.own.virtualaibox.domain.event.events.AgentMetEvent;
import com.own.virtualaibox.domain.event.events.AgentMovedEvent;
import com.own.virtualaibox.domain.event.events.TickEndedEvent;
import com.own.virtualaibox.domain.event.events.TickStartedEvent;
import com.own.virtualaibox.domain.memory.AgentMemory;
import com.own.virtualaibox.domain.memory.MemoryEntry;
import com.own.virtualaibox.mind.AgentRuntime;
import com.own.virtualaibox.mind.MindController;
import com.own.virtualaibox.monitor.ConvergenceMonitor;

@RestController
@RequestMapping("/api/dashboard")
/** 提供世界、Agent、事件和运行指标的看板查询接口。 */
public class DashboardController {

    private final WorldEngine worldEngine;
    private final MindController mindController;
    private final ConvergenceMonitor convergenceMonitor;

    /** 创建看板控制器并注入运行时查询依赖。 */
    /**
     * @param worldEngine 世界运行引擎
     * @param mindController Agent 心智运行时控制器
     * @param convergenceMonitor 收敛监控器
     */
    public DashboardController(WorldEngine worldEngine, MindController mindController,
                               ConvergenceMonitor convergenceMonitor) {
        this.worldEngine = worldEngine;
        this.mindController = mindController;
        this.convergenceMonitor = convergenceMonitor;
    }

    /** 汇总看板首页所需的当前运行数据。 */
    /**
     * @param eventLimit 返回的事件数量上限
     * @param memoryLimit 每个 Agent 返回的记忆数量上限
     * @return 看板汇总数据
     */
    @GetMapping
    public Map<String, Object> dashboard(@RequestParam(defaultValue = "30") int eventLimit,
                                         @RequestParam(defaultValue = "8") int memoryLimit) {
        Map<String, Object> result = new HashMap<>();
        result.put("tick", worldEngine.getCurrentTick());
        result.put("world", buildWorldInfo());
        result.put("agents", buildAgents(memoryLimit));
        result.put("events", buildEvents(eventLimit));
        result.put("metrics", buildMetrics());
        result.put("convergence", convergenceMonitor.summary());
        result.put("capabilities", List.of(
                "事件驱动架构",
                "Agent记忆",
                "多Agent交互",
                "LLM决策",
                "未来可扩展到经营/社交/战斗系统"
        ));
        return result;
    }

    /** 处理新增 Agent 请求。 */
    /**
     * @return 新增 Agent 的处理结果
     */
    @PostMapping("/addAgent")
    public Map<String, Object> addAgent() {
        Map<String, Object> result = new HashMap<>();
    }


    /** 推进世界一步并返回最新 Agent 状态。 */
    /**
     * @return 推进一步后的世界状态
     */
    @GetMapping("/step")
    public Map<String, Object> step() {
        worldEngine.step();

        Map<String, Object> result = new HashMap<>();
        result.put("tick", worldEngine.getCurrentTick());
        result.put("agents", worldEngine.getWorld().getAgents().stream().map(this::agentToMap).toList());

        return result;
    }

    /** 返回当前世界和 Agent 的简要状态。 */
    /**
     * @return 当前世界状态
     */
    @GetMapping("/state")
    public Map<String, Object> state() {
        Map<String, Object> result = new HashMap<>();
        result.put("tick", worldEngine.getCurrentTick());
        result.put("world", buildWorldInfo());
        result.put("agents", buildAgents(3));
        return result;
    }

    /** 查询最近发生的领域事件。 */
    /**
     * @param limit 返回的事件数量上限
     * @return 事件数据列表
     */
    @GetMapping("/events")
    public List<Map<String, Object>> events(@RequestParam(defaultValue = "50") int limit) {
        return buildEvents(limit);
    }

    /** 查询 Agent 当前状态及记忆信息。 */
    /**
     * @param memoryLimit 每个 Agent 返回的记忆数量上限
     * @return Agent 数据列表
     */
    @GetMapping("/agents")
    public List<Map<String, Object>> agents(@RequestParam(defaultValue = "8") int memoryLimit) {
        return buildAgents(memoryLimit);
    }

    /** 查询当前运行指标。 */
    /**
     * @return 运行指标数据
     */
    @GetMapping("/metrics")
    public Map<String, Object> metrics() {
        return buildMetrics();
    }

    /** 构建世界尺寸和 Agent 数量信息。 */
    /**
     * @return 世界信息
     */
    private Map<String, Object> buildWorldInfo() {
        Map<String, Object> world = new HashMap<>();
        world.put("width", worldEngine.getWorld().getWidth());
        world.put("height", worldEngine.getWorld().getHeight());
        world.put("agentCount", worldEngine.getWorld().getAgents().size());
        return world;
    }

    /** 构建所有 Agent 的详细信息。 */
    /**
     * @param memoryLimit 每个 Agent 返回的记忆数量上限
     * @return Agent 数据列表
     */
    private List<Map<String, Object>> buildAgents(int memoryLimit) {
        return worldEngine.getWorld().getAgents().stream()
                .map(agent -> agentToMap(agent, memoryLimit))
                .toList();
    }

    /** 将 Agent 转换为位置摘要数据。 */
    /**
     * @param agent 待转换的 Agent
     * @return Agent 位置摘要
     */
    private Map<String, Object> agentToMap(Agent agent) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", agent.getId());
        map.put("name", agent.getName());
        map.put("x", agent.getState().getX());
        map.put("y", agent.getState().getY());
        return map;
    }

    /** 将 Agent 转换为包含心智和记忆信息的详细数据。 */
    /**
     * @param agent 待转换的 Agent
     * @param memoryLimit 返回的记忆数量上限
     * @return Agent 详细数据
     */
    private Map<String, Object> agentToMap(Agent agent, int memoryLimit) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", agent.getId());
        map.put("name", agent.getName());
        map.put("x", agent.getState().getX());
        map.put("y", agent.getState().getY());
        map.put("active", agent.isActive());
        map.put("eventHistorySize", agent.getEventHistory() == null ? 0 : agent.getEventHistory().size());

        // 返回该 Agent 的 SECD 运行时状态（无运行时则为 null）
        AgentRuntime runtime = mindController.getRuntime(agent.getId());
        map.put("mind", runtime == null ? null : runtime.mindSummary());

        if (agent.getMemory() != null) {
            AgentMemory memory = agent.getMemory();
            map.put("memoryStats", memory.getMemoryStats());
            map.put("memorySummary", memory.summarizeMemoriesForLLM(worldEngine.getCurrentTick()));
            map.put("recentMemories", memory.getShortTermMemory(worldEngine.getCurrentTick()).stream()
                    .sorted(Comparator.comparingInt(MemoryEntry::getTick).reversed())
                    .limit(memoryLimit)
                    .map(this::memoryToMap)
                    .toList());
        } else {
            map.put("memoryStats", null);
            map.put("memorySummary", "");
            map.put("recentMemories", List.of());
        }

        return map;
    }

    /** 将领域事件历史转换为接口数据。 */
    /**
     * @param limit 返回的事件数量上限
     * @return 事件数据列表
     */
    private List<Map<String, Object>> buildEvents(int limit) {
        List<DomainEvent> history = worldEngine.getEventBus().getEventHistory(limit);
        List<Map<String, Object>> events = new ArrayList<>();
        for (DomainEvent event : history) {
            events.add(eventToMap(event));
        }
        return events;
    }

    /** 将单个领域事件转换为接口数据并展开事件详情。 */
    /**
     * @param event 待转换的领域事件
     * @return 事件数据
     */
    private Map<String, Object> eventToMap(DomainEvent event) {
        Map<String, Object> map = new HashMap<>();
        map.put("eventId", event.getEventId());
        map.put("eventType", event.getEventType());
        map.put("description", event.getDescription());
        map.put("tick", event.getTick());
        map.put("timestamp", event.getTimestamp());
        map.put("priority", event.getPriority());
        map.put("sourceSystem", event.getSourceSystem());
        map.put("processed", event.isProcessed());

        if (event instanceof AgentMovedEvent movedEvent) {
            map.put("detail", Map.of(
                    "agentId", movedEvent.getAgentId(),
                    "fromX", movedEvent.getFromX(),
                    "fromY", movedEvent.getFromY(),
                    "toX", movedEvent.getToX(),
                    "toY", movedEvent.getToY(),
                    "distance", movedEvent.getDistance(),
                    "reason", movedEvent.getReason()
            ));
        } else if (event instanceof AgentMetEvent metEvent) {
            map.put("detail", Map.of(
                    "agentId1", metEvent.getAgentId1(),
                    "agentName1", metEvent.getAgentName1(),
                    "agentId2", metEvent.getAgentId2(),
                    "agentName2", metEvent.getAgentName2(),
                    "meetX", metEvent.getMeetX(),
                    "meetY", metEvent.getMeetY(),
                    "distance", metEvent.getDistance()
            ));
        } else if (event instanceof AgentDecidedEvent decidedEvent) {
            map.put("detail", Map.of(
                    "agentId", decidedEvent.getAgentId(),
                    "agentName", decidedEvent.getAgentName(),
                    "actionType", decidedEvent.getActionType(),
                    "actionDetails", decidedEvent.getActionDetails(),
                    "reasoning", decidedEvent.getReasoning(),
                    "llmDecision", decidedEvent.isLlmDecision()
            ));
        } else if (event instanceof TickStartedEvent) {
            map.put("detail", Map.of("stage", "started"));
        } else if (event instanceof TickEndedEvent endedEvent) {
            map.put("detail", Map.of(
                    "eventCount", endedEvent.getEventCount(),
                    "executionTime", endedEvent.getExecutionTime()
            ));
        }

        return map;
    }

    /** 将记忆条目转换为接口数据。 */
    /**
     * @param entry 待转换的记忆条目
     * @return 记忆数据
     */
    private Map<String, Object> memoryToMap(MemoryEntry entry) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", entry.getId());
        map.put("type", entry.getType());
        map.put("content", entry.getContent());
        map.put("tags", entry.getTags());
        map.put("importance", entry.getImportance());
        map.put("createdAt", entry.getCreatedAt());
        map.put("lastAccessedAt", entry.getLastAccessedAt());
        map.put("accessCount", entry.getAccessCount());
        map.put("tick", entry.getTick());
        map.put("relatedAgentId", entry.getRelatedAgentId());
        return map;
    }

    /** 构建事件、Agent 和服务端时间等运行指标。 */
    /**
     * @return 运行指标数据
     */
    private Map<String, Object> buildMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("eventSubscriberCount", worldEngine.getEventBus().getSubscriberCount());
        metrics.put("eventHistorySize", worldEngine.getEventBus().getEventHistory(10000).size());
        metrics.put("agentCount", worldEngine.getWorld().getAgents().size());
        metrics.put("currentTick", worldEngine.getCurrentTick());
        metrics.put("serverTime", Instant.now());
        return metrics;
    }
}
