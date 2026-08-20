package com.own.virtualaibox.domain.event.events;

import com.own.virtualaibox.domain.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent决策事件
 * 当LLMBrain做出决策时发布
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentDecidedEvent extends DomainEvent {
    
    // 做出决策的Agent ID
    private String agentId;
    
    // Agent名称
    private String agentName;
    
    // 决策类型
    private String actionType;  // e.g., "move", "interact", "wait"
    
    // 决策详情（JSON字符串）
    private String actionDetails;
    
    // 决策理由
    private String reasoning;
    
    // 是否由LLM决策（true）还是降级到随机决策（false）
    private boolean llmDecision;
    
    @Override
    public String getEventType() {
        return "agent.decided";
    }
    
    @Override
    public String getDescription() {
        return String.format("Agent %s (id: %s) decided action: %s, reasoning: %s", 
                agentName, agentId, actionType, reasoning);
    }
}
