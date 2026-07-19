package com.own.virtualaibox.domain.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentState {
    private int x;
    private int y;
    private String name;
}
