package com.own.virtualaibox.domain.action;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveAction {
    private String agentId;
    private int deltaX;
    private int deltaY;
    private String reason;
}
