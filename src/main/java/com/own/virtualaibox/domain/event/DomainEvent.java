package com.own.virtualaibox.domain.event;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 领域事件基类
 * 所有系统事件必须继承此类
 * 支持扩展为：游戏系统、商业系统、社交系统等
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class DomainEvent {
    
    /** 事件唯一ID */
    private String eventId;
    
    /** 事件发生的时间tick */
    private int tick;
    
    /** 事件发生的真实时间 */
    private Instant timestamp;
    
    /** 事件优先级 (0=最低, 10=最高) */
    private int priority = 5;
    
    /** 事件来源系统标识 */
    private String sourceSystem;
    
    /** 事件是否已处理 */
    private boolean processed;
    
    /**
     * 获取事件类型
     */
    public abstract String getEventType();
    
    /**
     * 获取事件描述，用于日志和追踪
     */
    public abstract String getDescription();
}
