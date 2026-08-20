package com.own.virtualaibox.domain.event.events;

import com.own.virtualaibox.domain.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 时钟周期开始事件
 * 在每个Tick开始时发布
 * 优先级: HIGHEST，最高，代表着在所有事件执行前执行
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TickStartedEvent extends DomainEvent {
    
    // 当前时间戳（秒）
    private Instant timestamp;
    
    @Override
    public String getEventType() {
        return "tick.started";
    }
    
    @Override
    public String getDescription() {
        return String.format("Tick %d started", getTick());
    }
}
