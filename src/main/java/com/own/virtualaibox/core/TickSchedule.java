package com.own.virtualaibox.core;

import com.own.virtualaibox.brain.LLMBrain;
import com.own.virtualaibox.domain.action.MoveAction;
import com.own.virtualaibox.domain.agent.Agent;
import com.own.virtualaibox.domain.agent.AgentState;
import com.own.virtualaibox.domain.world.World;
import com.own.virtualaibox.domain.world.WorldState;
import com.own.virtualaibox.executor.ActionExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class TickSchedule {

    private final LLMBrain llmBrain;
    private final ActionExecutor actionExecutor;

    public TickSchedule(LLMBrain llmBrain, ActionExecutor actionExecutor) {
        this.llmBrain = llmBrain;
        this.actionExecutor = actionExecutor;
    }

    public void processTick(int tick, World world) {
        log.info("TickSchedule: Processing tick {}", tick);
        
        WorldState worldState = buildWorldState(tick, world);
        
        List<Agent> agents = world.getAgents();
        for (Agent agent : agents) {
            MoveAction action = llmBrain.decideAction(agent, worldState);
            actionExecutor.executeMoveAction(action, world);
        }
        
        log.info("TickSchedule: Tick {} completed", tick);
    }

    private WorldState buildWorldState(int tick, World world) {
        Map<String, AgentState> agentStates = new HashMap<>();
        for (Agent agent : world.getAgents()) {
            agentStates.put(agent.getId(), agent.getState());
        }
        return new WorldState(tick, agentStates);
    }
}
