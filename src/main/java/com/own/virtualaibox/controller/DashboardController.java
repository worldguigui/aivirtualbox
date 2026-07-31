package com.own.virtualaibox.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final WorldEngine worldEngine;

    public DashboardController(WorldEngine worldEngine) {
        this.worldEngine = worldEngine;
    }

    @GetMapping
    public Map<String, Object> dashboard(@RequestParam(defaultValue = "30") int eventLimit,
                                         @RequestParam(defaultValue = "8") int memoryLimit) {
        Map<String, Object> result = new HashMap<>();
        result.put("tick", worldEngine.getCurrentTick());
        result.put("world", buildWorldInfo());
        result.put("agents", buildAgents(memoryLimit));
        result.put("events", buildEvents(eventLimit));
        result.put("metrics", buildMetrics());
        result.put("capabilities", List.of(
                "事件驱动架构",
                "Agent记忆",
                "多Agent交互",
                "LLM决策",
                "未来可扩展到经营/社交/战斗系统"
        ));
        return result;
    }

    @GetMapping("/state")
    public Map<String, Object> state() {
        Map<String, Object> result = new HashMap<>();
        result.put("tick", worldEngine.getCurrentTick());
        result.put("world", buildWorldInfo());
        result.put("agents", buildAgents(3));
        return result;
    }

    @GetMapping("/events")
    public List<Map<String, Object>> events(@RequestParam(defaultValue = "50") int limit) {
        return buildEvents(limit);
    }

    @GetMapping("/agents")
    public List<Map<String, Object>> agents(@RequestParam(defaultValue = "8") int memoryLimit) {
        return buildAgents(memoryLimit);
    }

    @GetMapping("/metrics")
    public Map<String, Object> metrics() {
        return buildMetrics();
    }

    private Map<String, Object> buildWorldInfo() {
        Map<String, Object> world = new HashMap<>();
        world.put("width", worldEngine.getWorld().getWidth());
        world.put("height", worldEngine.getWorld().getHeight());
        world.put("agentCount", worldEngine.getWorld().getAgents().size());
        return world;
    }

    private List<Map<String, Object>> buildAgents(int memoryLimit) {
        return worldEngine.getWorld().getAgents().stream()
                .map(agent -> agentToMap(agent, memoryLimit))
                .toList();
    }

    private Map<String, Object> agentToMap(Agent agent, int memoryLimit) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", agent.getId());
        map.put("name", agent.getName());
        map.put("x", agent.getState().getX());
        map.put("y", agent.getState().getY());
        map.put("active", agent.isActive());
        map.put("eventHistorySize", agent.getEventHistory() == null ? 0 : agent.getEventHistory().size());

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

    private List<Map<String, Object>> buildEvents(int limit) {
        List<DomainEvent> history = worldEngine.getEventBus().getEventHistory(limit);
        List<Map<String, Object>> events = new ArrayList<>();
        for (DomainEvent event : history) {
            events.add(eventToMap(event));
        }
        return events;
    }

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
