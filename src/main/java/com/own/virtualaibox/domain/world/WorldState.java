package com.own.virtualaibox.domain.world;

import com.own.virtualaibox.domain.agent.AgentState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
/** 保存世界刻度及各 Agent 的状态快照。 */
public class WorldState {
    private int tick;
    private Map<String, AgentState> agentStates;
}
