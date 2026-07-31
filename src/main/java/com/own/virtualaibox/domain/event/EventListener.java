package com.own.virtualaibox.domain.event;

/**
 * 事件监听者接口
 * 所有想要处理事件的组件应实现此接口
 * 支持多订阅者模式
 */
@FunctionalInterface
public interface EventListener {
    
    /**
     * 处理事件
     * @param event 接收到的事件
     */
    void onEvent(DomainEvent event);
}
