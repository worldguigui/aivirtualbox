package com.own.virtualaibox.executor;

import com.own.virtualaibox.domain.action.MoveAction;
import com.own.virtualaibox.domain.agent.Agent;
import com.own.virtualaibox.domain.world.World;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ActionExecutor {

    public void executeMoveAction(MoveAction action, World world) {
        log.info("ActionExecutor: Executing move action for agent: {}", action.getAgentId());
        
        Agent agent = world.getAgents().stream()
                .filter(a -> a.getId().equals(action.getAgentId()))
                .findFirst()
                .orElse(null);
        
        if (agent == null) {
            log.warn("ActionExecutor: Agent not found: {}", action.getAgentId());
            return;
        }
        
        int newX = agent.getState().getX() + action.getDeltaX();
        int newY = agent.getState().getY() + action.getDeltaY();
        
        newX = Math.max(0, Math.min(world.getWidth() - 1, newX));
        newY = Math.max(0, Math.min(world.getHeight() - 1, newY));
        
        agent.getState().setX(newX);
        agent.getState().setY(newY);
        
        log.info("ActionExecutor: Agent {} moved to ({}, {})", agent.getName(), newX, newY);
    }
}
