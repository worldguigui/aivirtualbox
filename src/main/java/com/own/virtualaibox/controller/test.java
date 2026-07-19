package com.own.virtualaibox.controller;

import com.own.virtualaibox.core.WorldEngine;
import com.own.virtualaibox.domain.agent.Agent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class test {
    @Autowired
    private WorldEngine worldEngine;

    @GetMapping("/test")
    public String test() {
        return "Virtual AI Box is running!";
    }

    @GetMapping("/step")
    public Map<String, Object> step() {
        worldEngine.step();
        
        Map<String, Object> result = new HashMap<>();
        result.put("tick", worldEngine.getCurrentTick());
        result.put("agents", worldEngine.getWorld().getAgents().stream().map(this::agentToMap).toList());
        
        return result;
    }

    @GetMapping("/state")
    public Map<String, Object> state() {
        Map<String, Object> result = new HashMap<>();
        result.put("tick", worldEngine.getCurrentTick());
        result.put("agents", worldEngine.getWorld().getAgents().stream().map(this::agentToMap).toList());
        
        return result;
    }

    private Map<String, Object> agentToMap(Agent agent) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", agent.getId());
        map.put("name", agent.getName());
        map.put("x", agent.getState().getX());
        map.put("y", agent.getState().getY());
        return map;
    }
}
