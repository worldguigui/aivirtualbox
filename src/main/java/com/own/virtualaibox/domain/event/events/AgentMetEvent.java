package com.own.virtualaibox.domain.event.events;

import com.own.virtualaibox.domain.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent相遇事件
 * 当两个Agent移动到相同或邻近位置时发布
 * 可用于：社交互动、战斗、交易等
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentMetEvent extends DomainEvent {
    
    /** 相遇的第一个Agent ID */
    private String agentId1;
    
    /** 第一个Agent的名称 */
    private String agentName1;
    
    /** 相遇的第二个Agent ID */
    private String agentId2;
    
    /** 第二个Agent的名称 */
    private String agentName2;
    
    /** 相遇位置X */
    private int meetX;
    
    /** 相遇位置Y */
    private int meetY;
    
    /** 相遇距离 */
    private double distance;
    
    @Override
    public String getEventType() {
        return "agent.met";
    }
    
    @Override
    public String getDescription() {
        return String.format("Agent %s (id: %s) met Agent %s (id: %s) at (%d, %d), distance: %.2f", 
                agentName1, agentId1, agentName2, agentId2, meetX, meetY, distance);
    }
}
