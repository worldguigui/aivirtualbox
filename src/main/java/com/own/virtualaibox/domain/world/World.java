package com.own.virtualaibox.domain.world;

import com.own.virtualaibox.domain.agent.Agent;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class World {
    private final int width = 37;
    private final int height = 37;
    private List<Agent> agents = new ArrayList<>();

    public void addAgent(Agent agent) {
        agents.add(agent);
    }
}
