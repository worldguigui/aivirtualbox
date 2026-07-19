package com.own.virtualaibox.domain.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Agent {
    private String id;
    private String name;
    private AgentState state;
}
