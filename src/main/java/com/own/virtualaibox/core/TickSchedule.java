package com.own.virtualaibox.core;

import com.own.virtualaibox.brain.LLMBrain;
import com.own.virtualaibox.domain.action.MoveAction;
import com.own.virtualaibox.domain.agent.Agent;
import com.own.virtualaibox.domain.event.EventBus;
import com.own.virtualaibox.domain.event.events.AgentDecidedEvent;
import com.own.virtualaibox.domain.event.events.AgentMetEvent;
import com.own.virtualaibox.domain.event.events.TickEndedEvent;
import com.own.virtualaibox.domain.event.events.TickStartedEvent;
import com.own.virtualaibox.domain.world.World;
import com.own.virtualaibox.domain.world.WorldState;
import com.own.virtualaibox.executor.ActionExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Slf4j
public class TickSchedule {

    private final LLMBrain llmBrain;
    private final ActionExecutor actionExecutor;
    private final EventBus eventBus;

    public TickSchedule(LLMBrain llmBrain, ActionExecutor actionExecutor, EventBus eventBus) {
        this.llmBrain = llmBrain;
        this.actionExecutor = actionExecutor;
        this.eventBus = eventBus;
    }

    public void processTick(int tick, World world) {
        long startTime = System.currentTimeMillis();
        log.info("TickSchedule: Processing tick {}", tick);
        
        try {
            // Phase 1: 发布Tick开始事件
            publishTickStarted(tick);
            
            // Phase 2: 决策阶段 - 所有Agent进行决策
            Map<String, MoveAction> decisions = decisionPhase(tick, world);
            
            // Phase 3: 执行阶段 - 执行所有动作
            executionPhase(tick, decisions, world);
            
            // Phase 4: 交互检测阶段 - 检测Agent相遇
            interactionPhase(tick, world);
            
            // Phase 5: 发布Tick结束事件
            long executionTime = System.currentTimeMillis() - startTime;
            publishTickEnded(tick, executionTime);
            
            log.info("TickSchedule: Tick {} completed in {}ms", tick, executionTime);
            
        } catch (Exception e) {
            log.error("TickSchedule: Error processing tick {}", tick, e);
        }
    }
    
    /**
     * Phase 1: 发布Tick开始事件
     */
    private void publishTickStarted(int tick) {
        TickStartedEvent event = new TickStartedEvent();
        event.setEventId("tick_start_" + tick);
        event.setTick(tick);
        event.setTimestamp(Instant.now());
        event.setSourceSystem("tick-schedule");
        event.setPriority(10);  // 最高优先级
        
        eventBus.publish(event);
    }
    
    /**
     * Phase 2: 决策阶段
     */
    private Map<String, MoveAction> decisionPhase(int tick, World world) {
        log.info("TickSchedule: Entering decision phase, current tick: {}", tick);
        
        WorldState worldState = buildWorldState(tick, world);
        Map<String, MoveAction> decisions = new HashMap<>();
        
        for (Agent agent : world.getAgents()) {
            MoveAction action = llmBrain.decideAction(agent, worldState);
            decisions.put(agent.getId(), action);
            
            // 发布决策事件
            publishAgentDecided(tick, agent, action);
        }
        
        return decisions;
    }
    
    /**
     * Phase 3: 执行阶段
     */
    private void executionPhase(int tick, Map<String, MoveAction> decisions, World world) {
        log.info("TickSchedule: Entering execution phase, current tick: {}", tick);
        
        for (MoveAction action : decisions.values()) {
            actionExecutor.executeMoveAction(action, world);
        }
    }
    
    /**
     * Phase 4: 交互检测阶段
     */
    private void interactionPhase(int tick, World world) {
        log.info("TickSchedule: Entering interaction detection phase, current tick: {}", tick);
        
        List<Agent> agents = world.getAgents();
        
        // 检测所有Agent对的相遇
        for (int i = 0; i < agents.size(); i++) {
            for (int j = i + 1; j < agents.size(); j++) {
                Agent agent1 = agents.get(i);
                Agent agent2 = agents.get(j);
                
                double distance = calculateDistance(agent1, agent2);
                
                // 如果距离 <= 1.5 格，视为相遇
                if (distance <= 1.5) {
                    publishAgentMet(tick, agent1, agent2, distance);
                }
            }
        }
    }
    
    /**
     * Phase 5: 发布Tick结束事件
     */
    private void publishTickEnded(int tick, long executionTime) {
        TickEndedEvent event = new TickEndedEvent();
        event.setEventId("tick_end_" + tick);
        event.setTick(tick);
        event.setTimestamp(Instant.now());
        event.setSourceSystem("tick-schedule");
        event.setPriority(1);  // 最低优先级
        
        // 获取事件历史中本Tick的事件数
        int eventCount = (int) eventBus.getEventHistory(1000).stream()
                .filter(e -> e.getTick() == tick)
                .count();
        
        event.setEventCount(eventCount);
        event.setExecutionTime(executionTime);
        
        eventBus.publish(event);
    }

    private void publishAgentDecided(int tick, Agent agent, MoveAction action) {
        AgentDecidedEvent event = new AgentDecidedEvent();
        event.setEventId(agent.getId() + "_decided_" + tick);
        event.setTick(tick);
        event.setTimestamp(Instant.now());
        event.setSourceSystem("llm-brain");
        event.setPriority(6);
        event.setAgentId(agent.getId());
        event.setAgentName(agent.getName());
        event.setActionType("move");
        event.setActionDetails(String.format("{\"deltaX\":%d,\"deltaY\":%d}", 
                action.getDeltaX(), action.getDeltaY()));
        event.setReasoning(action.getReason());
        event.setLlmDecision(true);  // TODO: 需要从LLMBrain传递此信息
        
        eventBus.publish(event);
    }
    
    private void publishAgentMet(int tick, Agent agent1, Agent agent2, double distance) {
        AgentMetEvent event = new AgentMetEvent();
        event.setEventId(agent1.getId() + "_met_" + agent2.getId() + "_" + tick);
        event.setTick(tick);
        event.setTimestamp(Instant.now());
        event.setSourceSystem("interaction-detector");
        event.setPriority(8);  // 高优先级
        event.setAgentId1(agent1.getId());
        event.setAgentName1(agent1.getName());
        event.setAgentId2(agent2.getId());
        event.setAgentName2(agent2.getName());
        event.setMeetX((agent1.getState().getX() + agent2.getState().getX()) / 2);
        event.setMeetY((agent1.getState().getY() + agent2.getState().getY()) / 2);
        event.setDistance(distance);
        
        eventBus.publish(event);
        log.info("TickSchedule: Detected agents meeting - {} and {}", agent1.getName(), agent2.getName());
    }

    private double calculateDistance(Agent agent1, Agent agent2) {
        int dx = agent1.getState().getX() - agent2.getState().getX();
        int dy = agent1.getState().getY() - agent2.getState().getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private WorldState buildWorldState(int tick, World world) {
        Map<String, com.own.virtualaibox.domain.agent.AgentState> agentStates = new HashMap<>();
        for (Agent agent : world.getAgents()) {
            agentStates.put(agent.getId(), agent.getState());
        }
        return new WorldState(tick, agentStates);
    }
}
