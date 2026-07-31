package com.own.virtualaibox.core;

import com.own.virtualaibox.domain.agent.Agent;
import com.own.virtualaibox.domain.agent.AgentState;
import com.own.virtualaibox.domain.event.EventBus;
import com.own.virtualaibox.domain.memory.AgentMemory;
import com.own.virtualaibox.domain.memory.MemoryStore;
import com.own.virtualaibox.domain.world.World;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class WorldEngine {

    private final VirtualClock virtualClock;
    private final TickSchedule tickSchedule;
    private final EventBus eventBus;
    private final MemoryStore memoryStore;
    private final World world;

    public WorldEngine(VirtualClock virtualClock, TickSchedule tickSchedule, 
                       EventBus eventBus, MemoryStore memoryStore) {
        this.virtualClock = virtualClock;
        this.tickSchedule = tickSchedule;
        this.eventBus = eventBus;
        this.memoryStore = memoryStore;
        this.world = new World();
    }

    @PostConstruct
    public void init() {
        log.info("WorldEngine: Initializing...");
        
        Agent agent1 = createAgent("Alice", 0, 6);
        Agent agent2 = createAgent("Bob", 30, 30);
        
        world.addAgent(agent1);
        world.addAgent(agent2);
        
        // 注册Agent为事件监听者
        eventBus.subscribeGlobal(agent1);
        eventBus.subscribeGlobal(agent2);
        
        log.info("WorldEngine: Initialized with {} agents", world.getAgents().size());
        log.info("WorldEngine: Event listeners registered: {}", eventBus.getSubscriberCount());
    }

    public void step() {
        log.info("WorldEngine: Starting new tick...");
        
        virtualClock.stepForward();
        int currentTick = getCurrentTick();
        
        tickSchedule.processTick(currentTick, world);
        
        log.info("WorldEngine: Tick {} completed", currentTick);
    }

    private Agent createAgent(String name, int x, int y) {
        String agentId = UUID.randomUUID().toString();
        AgentState state = new AgentState(x, y, name);
        
        // 为Agent创建记忆管理器
        AgentMemory memory = new AgentMemory(agentId, memoryStore);
        
        Agent agent = new Agent(agentId, name, state, memory, null, true);
        agent.setMemory(memory);
        
        log.info("WorldEngine: Created agent {} with id {}", name, agentId);
        
        return agent;
    }

    public int getCurrentTick() {
        return virtualClock.getTick();
    }

    public World getWorld() {
        return world;
    }
    
    /**
     * 获取事件总线
     */
    public EventBus getEventBus() {
        return eventBus;
    }
}

