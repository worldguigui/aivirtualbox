package com.own.virtualaibox.domain.world;

import com.own.virtualaibox.domain.agent.Agent;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
/** 保存世界边界及其中的 Agent 集合。 */
public class World {
    private final int width = 37;
    private final int height = 37;
    private List<Agent> agents = new ArrayList<>();

    /** 将 Agent 加入世界。 */
    /**
     * @param agent 待加入世界的 Agent
     */
    public void addAgent(Agent agent) {
        agents.add(agent);
    }
}
