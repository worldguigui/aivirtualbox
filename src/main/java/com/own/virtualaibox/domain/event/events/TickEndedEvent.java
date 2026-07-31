package com.own.virtualaibox.domain.event.events;

import com.own.virtualaibox.domain.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 时钟周期结束事件
 * 在每个Tick结束时发布
 * 优先级: LOW
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TickEndedEvent extends DomainEvent {
    
    /** 本Tick处理的事件数量 */
    private int eventCount;
    
    /** 本Tick的执行时间（毫秒） */
    private long executionTime;
    
    @Override
    public String getEventType() {
        return "tick.ended";
    }
    
    @Override
    public String getDescription() {
        return String.format("Tick %d ended with %d events in %dms", 
                getTick(), eventCount, executionTime);
    }
}
