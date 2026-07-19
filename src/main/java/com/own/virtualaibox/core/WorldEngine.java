package com.own.virtualaibox.core;

import com.own.virtualaibox.domain.agent.Agent;
import com.own.virtualaibox.domain.agent.AgentState;
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
    private final World world;

    public WorldEngine(VirtualClock virtualClock, TickSchedule tickSchedule) {
        this.virtualClock = virtualClock;
        this.tickSchedule = tickSchedule;
        this.world = new World();
    }

    @PostConstruct
    public void init() {
        log.info("WorldEngine: Initializing...");
        
        Agent agent1 = createAgent("Alice", 0, 6);
        Agent agent2 = createAgent("Bob", 30, 30);
        
        world.addAgent(agent1);
        world.addAgent(agent2);
        
        log.info("WorldEngine: Initialized with {} agents", world.getAgents().size());
    }

    public void step() {
        log.info("WorldEngine: Starting new tick...");
        
        virtualClock.stepForward();
        int currentTick = getCurrentTick();
        
        tickSchedule.processTick(currentTick, world);
        
        log.info("WorldEngine: Tick {} completed", currentTick);
    }

    private Agent createAgent(String name, int x, int y) {
        AgentState state = new AgentState(x, y, name);
        return new Agent(UUID.randomUUID().toString(), name, state);
    }

    public int getCurrentTick() {
        return virtualClock.getTick();
    }

    public World getWorld() {
        return world;
    }
}
