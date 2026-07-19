package com.own.virtualaibox.domain.world;

import com.own.virtualaibox.domain.agent.AgentState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorldState {
    private int tick;
    private Map<String, AgentState> agentStates;
}
